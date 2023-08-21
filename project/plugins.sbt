resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
)

addSbtPlugin("uk.gov.hmrc"          % "sbt-auto-build"  % "3.9.0")
addSbtPlugin("org.scoverage"        % "sbt-scoverage"   % "2.0.8")
addSbtPlugin("com.beautiful-scala"  % "sbt-scalastyle"  % "1.5.1")
addSbtPlugin("com.timushev.sbt"     % "sbt-updates"     % "0.6.4")
