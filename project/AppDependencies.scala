import sbt.*

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.typesafe"          %  "config"       % "1.4.2",
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"         %% "scalatest"    % "3.2.16",
    "com.vladsch.flexmark"  %  "flexmark-all" % "0.64.8"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
