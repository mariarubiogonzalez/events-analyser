lazy val akkaHttpVersion = "10.2.4"
lazy val akkaVersion    = "2.6.15"
lazy val logbackVersion = "1.2.3"
lazy val scalaTestVersion = "3.1.4"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "mariarubiogonzalez.analyser",
      scalaVersion    := "2.13.4"
    )),
    name := "events-analyser",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"                % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json"     % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-actor-typed"         % akkaVersion,
      "com.typesafe.akka" %% "akka-stream"              % akkaVersion,
      "ch.qos.logback"    % "logback-classic"           % logbackVersion,

      "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"                % scalaTestVersion         % Test
    )
  )
