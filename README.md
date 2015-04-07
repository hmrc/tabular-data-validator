Data Validation Engine
===================
The aim of this project is to provide a data validation engine that can be used to validate tabular data,
typically sourced from spreadsheets.

The engine allows you to specify a set of custom rules to be applied to columns in the data on a row by row basis.
These rules can check individual column data or check the data from multiple columns where you have interdependencies in
the data.

The rules are supplied as snippets of Java code.

More complex rules can be built up by building a Validation Context object that can be passed to the engine. This context
object can be used to store the more elaborate validation rules and can be called from the rule expr.


Getting Started
=====
Creating a data validation instance.

The Data Validation engine can either take a Config object which will contain your configuration or it will try and load
the configuration from validation-config.conf in your classpath/Play Conf folder.

To use default validator based on config

```
val dataValidator = DataValidator   // will load your config from validation-config.conf  if not found expect an exception?
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
      |  definitions {
      |   script : []
      |  }
      |   group-rules:[
      |  ]
      |   rules: [
      |   ]
      | }
    """.stripMargin

val config: Config = ConfigFactory.parseString(CONFIG_STR)
val datavalidator = DataValidator(config)
```

Data Validation Config
======================

**rules:**
Define generic validation rules. These will be referenced by other areas of the config

```
    rules: [
  {
    id="MANDATORY"
    errorId="002"
    errorMsgTemplate = "'@{cellName}' must have an entry."
    expr="notEmpty(data)"
  }
  {
    id="rulename"
    errorId="005"
    errorMsg = "Title is wrong"
    expr="data != null && data.length() > 0"
  }
  {
    id="length"
    errorId="006"
    errorMsgTemplate = "'@{cellName}' must be between @{min} and @{max} characters long."
    exprTemplate="data.length() >= @{min} && data.length() <= @{max}"
  }

]
```

Here we have defined 3 rules... You must always have the MANDATORY rule as it is a 'special rule'. If a cell is mandatory
and there is no data, any other rules defined on the cell will not be ran as they will fail due to the mandatory violation.

The structure of the rule is a ID, errorID (can be anything), there errorMsgTemplate (provides ability to create contextual
error messages) and expr (the actual 'rule implementation').

  * expr - The library used MVL to execute code expressions. These rules can access anything supplied in a Context Object
  (explained later), rules defined in definitions section or one of the data variables supplied by the library.
  Some supplied variables are...

  Name | What you get
  -----|-------------
  column | The column as defined in fieldInfo
  cellName | The cells Name as defined in fieldInfo
  data | The contents of the cell

  * errorMsgTemplate - This can access the same variables as Expr.  To substitute in the values use the the Scala string
substitution syntax... i.e @{cellName}  to get cell name in the string "@{cellName} is the name for column @{column}"

  * exprTemplate - This is similar to expr but it uses Scala String substitution for replace values in the expr before evaluation.
These values are defined in a ruleRef parameters section of the rules.



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
      expr="data.matches(\"^[A-Za-z0-9 ~!\\\"@#$%&'()*+,-\\\\[\\\\].:;<=>?^_{}£\\\\\\\\€]*$\")"
      errorId="001"
    } ]

    ruleRef: [
    {
      id="rulename"
      errorMsg = "'Worker title' must have an entry."
    }
    {
      id="length"
      parameters {
        min=1
        max=120
      }
    }
    ]
  }
```

  * column: The Alpha numeric column header as it would appear in Excel. Cell positions are mapped to these. First cell = A, second = B etc.

  * cellName: The human name for the column. As might appear in a column header.

  * mandatory: Is the field mandatory. If true, you must specify the MANDATORY rule in the rules section.

  * error:
   Column specific validations. Note: errorMsg does not use Scala String replacement.
   The expr has the same visibility of data as in rule section. Can access data/functions in the helper object.

  * ruleRef:
  This is where you can access common validator rules. id must match a rule defined in the rule section. The 'length' rule
  is an example of a passing parameters to a rule. min and max are passed in and used in the the exprTemplate.


**group-rules:**
This section is used to define rules that need to be applied to multiple columns.

```
group-rules:[
    {
      id="mandatoryD"
      errorId="G01"
      columns:["D", "F"]
      expr="mandatoryD(dataD, dataF)"
      columnErrors: {
        "D":  {errorMsgTemplate = "'@{cellNameD}' must have an entry if '@{cellNameF}' is blank."}
      }
    }
```
  * columns: used to identify what columns are to be used in the rule... i.e. ["D", "F"]

  * expr: another expression evaluation. The data it has access is slightly different.
  The cellName and data vars have the column appended.... i.e cellNameD, cellNameF and
  dataD , dataF.  In the example it calls a mandatoryD(..) function. This is defined in the helper object.

  * columnErrors: used to take the error against a specific column(s). Error message supplied via errorMsgTemplate which
  used the Scala String substitution syntax and again has access to the same vars as expr.


**definitions**
I would not recommend putting anything in this section! Expect it to be deprecated and removed in the future.

It's original intent was to allow custom code snippets to be written and available to the rules. You could create a method
in here that would perform some form of validation. You could then reference it in the expr sections of your rules.

However, this is what the context object is ideally suited for.

Another issue is that the methods defined in this section are compiled by MVL every time a row/cell is validated!
This became a performance drain.

For completeness, here is an example.

```
  definitions {
    script:[
    "def notEmpty(data) {"
    "!(data == null || data.trim().isEmpty())"
    "}"

    "def length(min, max, data) {"
    "data.length() >= min && data.length() <= max"
    "}"
    ]
  }
```

The recommended approach is to create a simple context object and pass it to the validation methods. This can be a singleton
object but be careful to ensure it is tread safe if you choose to do that.

Data Validator Methods
======================

Once a DataValidator has been created there are a number of ways to use it.

1) Supply the data set as List[List[String]] and call one of the 3 variants of validateRows. These do all the orchestration
and will return a list of all the errors in the Data set.  Note: the ability to pass in the starting row number in 2 of the
variants means that it can be used to process chunks of data and the row numbers in the errors will be correct.
The buffered version of validate rows allows the caller to supply a ListBuffer to collate errors between calls. This
better supports the ability to process chunks of data.

```
 /**
   *
   * @param rows              - the data to validate
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param firstRowNum       - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows   - shall we ignore completely blank rows?
   * @return                  - None or Some List of Validation errors
   */
  def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], firstRowNum : Int, ignoreBlankRows : Boolean): Option[List[ValidationError]]

 /**
   *
   * @param rows              - the data to validate
   * @param errorBuffer       - a ListBuffer you wish to collate your errors in
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param firstRowNum       - all errors contain the row number... specify the staring number here.
   * @param ignoreBlankRows   - shall we ignore completely blank rows?
   */
 override def validateRowsBuffered(rows: List[List[String]], errorBuffer: ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef], firstRowNum: Int, ignoreBlankRows: Boolean)

 /**
   *
   * @param rows              - the data to validate
   * @param contextObjectOpt  - a helper object to support the validation rules
   * @param zeroBased         - will the first row be treated as 0 or 1?
   * @return                  - None or Some List of Validation errors
   */
 def validateRows(rows: List[List[String]], contextObjectOpt: Option[AnyRef], zeroBased: Boolean = true) : Option[List[ValidationError]]
```

2) Validate the rows individually, if you wish to have more control (perhaps you only load subsets of rows at a time)

There are 2 variants... buffered and unbuffered.  The buffered version allows the caller to manage a ListBuffer with the errors to
allow concatenation between calls in a buffer. The unbuffered version returns a List of Validation Errors. The caller will need to collate
these between calls.

```
  def validateRowBuffered(row: Row, errors : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] = None) : Unit
  def validateRow(row: Row, contextObjectOpt: Option[AnyRef] = None): Option[List[ValidationError]]
```

3) Validate the cells individually, if you wish to have more control (perhaps you only load subsets of cells at a time)

Again, there are 2 variants... buffered and unbuffered.  The buffered version allows the caller to manage a ListBuffer with the errors to
allow concatenation between calls in a buffer. The unbuffered version returns a List of Validation Errors. The caller will need to collate
these between calls.

```
  def validateCellBuffered(cell: Cell, errors : ListBuffer[ValidationError], contextObjectOpt: Option[AnyRef] = None) : Unit
  def validateCell(cell: Cell, contextObjectOpt: Option[AnyRef] = None): Option[List[ValidationError]]
```

Using the Data Validator
========================

**Example 1** - Validate a set of data rows with a Validation Context object, default rules from validation-config.conf

```
 class ValidationContext  {
   def notEmpty(data: String): Boolean = {
     !(data == null || data.trim.isEmpty)
   }

   def length(min: Int, max: Int, data: String): Boolean = {
     data.length >= min && data.length <= max
   }

   }

   .....

   val rows = List(List("Data A", "Data B", "Data C"))

    val dataValidator = DataValidator()
    val contextObject = new ValidationContext()

    val errors : Option[List[ValidationError]]= dataValidator.validateRows(rows, Some(contextObjectOpt), firstRowNum = 1, ignoreBlankRows = false)
```

**Example 2** - Validate a set of data rows without a Validation Context object, using custom-validation-config conf file

```
   val rows = List(List("Data A", "Data B", "Data C"))

    val config = ConfigFactory.load.getConfig("custom-validation-config")
    val dataValidator = DataValidator(config)

    val errors : Option[List[ValidationError]] = dataValidator.validateRows(rows, None, firstRowNum = 1, ignoreBlankRows = false)
```

**Example 3** - Validate a set of data rows without a Validation Context object and pass in a ListBuffer to collate errors.

```
   val errorBuffer : ListBuffer[ValidationError] = new ListBuffer()
   val rows = List(List("Data A", "Data B", "Data C"))

    val dataValidator = DataValidator()

    dataValidator.validateRows(rows, errorBuffer,  None, firstRowNum = 1, ignoreBlankRows = false)

    if (errorBuffer.size > 0) {
    // have errors
    }
```

**Example 3** - Validate a set of data rows with a Validation Context object and pass in a ListBuffer to collate errors.

```
 class ValidationContext  {
   def notEmpty(data: String): Boolean = {
     !(data == null || data.trim.isEmpty)
   }

   def length(min: Int, max: Int, data: String): Boolean = {
     data.length >= min && data.length <= max
   }

   }

   .....
   val errorBuffer : ListBuffer[ValidationError] = new ListBuffer()
   val rows = List(List("Data A", "Data B", "Data C"))

    val dataValidator = DataValidator()
    val contextObject = new ValidationContext()

    dataValidator.validateRows(rows, errorBuffer,  Some(contextObject), firstRowNum = 1, ignoreBlankRows = false)

    if (errorBuffer.size > 0) {
    // have errors
    }
```

## License ##
 
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
