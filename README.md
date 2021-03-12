Data Validation Engine
===================
The aim of this project is to provide a data validation engine that can be used to validate tabular data,
typically sourced from spreadsheets.

The engine allows you to specify a set of custom rules to be applied to columns in the data on a row by row basis.
These rules can check individual column data or check the data from multiple columns where you have interdependencies in
the data.

The rules are supplied as Regex.

Getting Started
=====
Creating a data validation instance.

The Data Validation engine can either take a Config object which will contain your configuration or it will try and load
the configuration from validation-config.conf in your classpath/Play Conf folder.

To use default validator based on config

```
val dataValidator = DataValidator   // will load your config from validation-config.conf, if not found expect an exception is thrown
```

To supply a different config file...

```
val config = ConfigFactory.load.getConfig("custom-validation-config")
val dataValidator = DataValidator(config)
```

or create config on the fly...

```
  val CONFIG_STR: String =
    """
      | {
      |   fieldInfo: []
      |   group-rules: []
      | }
    """.stripMargin

val config: Config = ConfigFactory.parseString(CONFIG_STR)
val datavalidator = DataValidator(config)
```

Data Validation Config
======================

**fieldInfo:**
Defines the field/column configuration

An example
```
  {
    column="A"
    cellName = "Employment intermediary name"
    mandatory = true

    error: [

    {
      id="error.1"
      errorMsg = "'Employment intermediary name' can include letters, numbers, and any symbol except | ` ¬ or ¦."
      validationID="1"
      regex="^[A-Za-z0-9 ~!\\\"@#$%&'()*+,-\\\\[\\\\].:;<=>?^_{}£\\\\\\\\€]*$\"
      errorId="001"
    } ]
  }
```

  * column: The Alpha numeric column header as it would appear in Excel. Cell positions are mapped to these. First cell = A, second = B etc.

  * cellName: The human name for the column. As might appear in a column header.

  * mandatory: Is the field mandatory.

  * error:
   Column specific validations. Note: errorMsg does not use Scala String replacement.
    

**group-rules:**
This section is used to define rules that need to be applied to multiple columns.

```
group-rules:[
    {
      id="mandatoryD"
      errorId="G01"
      expectedValue=".*"
      flags: {
        independent = "F"
        dependent = "D"
      }

      columnErrors: {
        "D":  {errorMsg = "'Cell D Name' must have an entry if 'Cell F Name' is blank."}
      }
    }
```
  
  * expectedValue: if the contents of the independent cell (in this case, F) match this Regex, then the dependent cell (in this case, D) is mandatory.
    In the example, D is mandatory regardless if F has anything in it; if expectedValue was set to 'yes', then column D would only be mandatory if column F had the word 'yes' in it.
    
  * flags: 
    * independent: the independent column
    * dependent: the column which might be mandatory, depending on the state of the independent column
  * columnErrors: used to take the error against a specific column(s). Error message supplied via errorMsg.

Data Validator Methods
======================

1) Validate the rows individually

Pass in a Row object, validated with the config that DataValidator was instantiated with. Outputs an optional list of validation errors - None if no errors were found, Some with a non-empty list otherwise. 
```
  def validateRow(row: Row): Option[List[ValidationError]]
```

2) Validate the cells individually, if you wish to have more control (perhaps you only load subsets of cells at a time)

Pass in a Cell object, validated with the  config that DataValidator was instantiated with. Outputs an optional ValidationError - None if no error was found.
```
  def validateCell(cell: Cell): Option[ValidationError] = {
```

Using the Data Validator
========================

**Example 1** - Validate a row, rules from my-own-config.conf

```
   val row: Row = Row(
     rowNum = 5,
     cells = Seq(
       Cell(column = "A", row = 5, value = "Bobby"),
       Cell(column = "B", row = 5, value = "Tables")
     )
   )

    val dataValidator = new DataValidator(ConfigFactory.load.getConfig("my-own-config"))

    val errors: Option[List[ValidationError]] = dataValidator.validateRow(row)
```

**Example 1** - Validate a cell, rules from my-own-config.conf

```
   val cell: Cell = Cell(column = "A", row = 5, value = "Bobby")

    val dataValidator = new DataValidator(ConfigFactory.load.getConfig("my-own-config"))

    val error: Option[ValidationError] = dataValidator.validateCell(cell)
```

## License ##
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
