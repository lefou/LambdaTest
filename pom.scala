import org.sonatype.maven.polyglot.scala.model._
import scala.collection.immutable.Seq

val namespace = "de.tobiasroeser.lambdatest"
val lambdatest = "de.tototec" % namespace % "0.6.1-SNAPSHOT"
val gav = lambdatest
val url = "https://github.com/lefou/LambdaTest"

object Deps {
  val asciiDoclet = "org.asciidoctor" % "asciidoclet" % "1.5.4"
  val testng = "org.testng" % "testng" % "6.11"
  val junit = "junit" % "junit" % "4.12"
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
}

object Plugins {
  val bnd = "biz.aQute.bnd" % "bnd-maven-plugin" % "4.0.0"
  val bundle = "org.apache.felix" % "maven-bundle-plugin" % "3.3.0"
  val clean = "org.apache.maven.plugins" % "maven-clean-plugin" % "3.0.0"
  val jar = "org.apache.maven.plugins" % "maven-jar-plugin" % "2.5"
  val retrolambda = "net.orfjackal.retrolambda" % "retrolambda-maven-plugin" % "1.8.0"
  val reproducibleBuild = "io.github.zlika" % "reproducible-build-maven-plugin" % "0.7"
  val translate = "io.takari.polyglot" % "polyglot-translate-plugin" % "0.3.1"
  val javadoc = "org.apache.maven.plugins" % "maven-javadoc-plugin" % "3.0.1"
}

def bndExecution(id: String, classesDir: String) = Execution(
  id = id,
  goals = Seq("bnd-process"),
  configuration = Config(
    classesDir = classesDir,
    manifestPath = classesDir + "/META-INF/MANIFEST.MF",
    bnd = s"""
      |Bundle-Description: $${project.description}
      |Bundle-URL: $${project.url}
      |Implementation-Version: $${project.version}
      |Export-Package: ${
      Seq(
        namespace,
        namespace + ".generic",
        namespace + ".junit",
        namespace + ".testng",
        namespace + ".proxy"

      ).mkString(",")
    }
      |Import-Package: ${
      Seq(
        """org.testng.*;version="6.8;resolution:=optional""",
        """org.junit.*;resolution:=optional""",
        """*;resolution:=optional"""
      ).mkString(",")
    }
      |""".stripMargin
  )
)

def jarExecution(id: String, classifier: String, classesDir: String) = Execution(
  id = id,
  phase = "package",
  goals = Seq("jar"),
  configuration = Config(
    classifier = classifier,
    classesDirectory = classesDir,
    archive = Config(
      manifestFile = classesDir + "/META-INF/MANIFEST.MF"
    )
  )
)

val genPomXmlProfile = Profile(
  id = "gen-pom-xml",
  build = BuildBase(
    plugins = Seq(
      // Generate pom.xml from pom.scala
      Plugin(
        gav = Plugins.translate,
        executions = Seq(
          Execution(
            id = "pom-scala-to-pom-xml",
            phase = "initialize",
            goals = Seq("translate-project"),
            configuration = Config(
              input = "pom.scala",
              output = "pom.xml"
            )
          )
        )
      ),
      // Clean generated pom.xml
      Plugin(
        gav = Plugins.clean,
        configuration = Config(
          filesets = Config(
            fileset = Config(
              directory = "${basedir}",
              includes = Config(
                include = "pom.xml"
              )
            )
          )
        )
      )
    )
  )
)

Model(
  lambdatest,
  name = "LambdaTest",
  description = "Lambda-enabled functional testing on top of JUnit and TestNG",
  url = "https://github.com/lefou/LambdaTest",
  developers = Seq(
    Developer(
      email = "le.petit.fou@web.de",
      name = "Tobias Roeser"
    )
  ),
  licenses = Seq(
    License(
      name = "Apache License, Version 2.0",
      url = "http://www.apache.org/licenses/LICENSE-2.0.txt",
      distribution = "repo"
    )
  ),
  scm = Scm(
    url = url
  ),
  dependencies = Seq(
    Deps.testng % "provided",
    Deps.junit % "provided",
    Deps.slf4j % "provided",
    Deps.logbackClassic % "test"
  ),
  properties = Map(
    "project.build.sourceEncoding" -> "UTF-8",
    "maven.compiler.source" -> "1.8",
    "maven.compiler.target" -> "1.8"
  ),
  build = Build(
    resources = Seq(
      Resource(
        directory = "src/main/resources"
      ),
      Resource(
        directory = ".",
        includes = Seq("LICENSE.txt", "README.adoc")
      )
    ),
    pluginManagement = PluginManagement(
      plugins = Seq(
        Plugin(
          Plugins.javadoc,
          configuration = Config(
            failOnError = "false"
          )
        )
      )
    ),
    plugins = Seq(
      Plugin(
        Plugins.retrolambda,
        executions = Seq(
          Execution(
            id = "process-java7-classes",
            goals = Seq("process-main"),
            configuration = Config(
              target = "1.7",
              mainOutputDir = "${project.build.directory}/java7-classes"
            )
          ),
          Execution(
            id = "process-java6-classes",
            goals = Seq("process-main"),
            configuration = Config(
              target = "1.6",
              mainOutputDir = "${project.build.directory}/java6-classes"
            )
          )
        )
      ),
      Plugin(
        gav = Plugins.bundle,
        executions = Seq(
          Execution(
            goals = Seq("baseline"),
            configuration = Config(
              failOnError = "false"
            )
          )
        )
      ),
      Plugin(
        gav = Plugins.bnd,
        executions = Seq(
          bndExecution(
            id = "bnd-process-java8",
            classesDir = "${project.build.outputDirectory}"
          ),
          bndExecution(
            id = "bnd-process-java7",
            classesDir = "${project.build.directory}/java7-classes"
          ),
          bndExecution(
            id = "bnd-process-java6",
            classesDir = "${project.build.directory}/java6-classes"
          )
        )
      ),
      Plugin(
        Plugins.jar,
        executions = Seq(
          jarExecution(
            id = "default-jar",
            classifier = "",
            classesDir = "${project.build.outputDirectory}"
          ),
          jarExecution(
            id = "jar-java7",
            classifier = "java7",
            classesDir = "${project.build.directory}/java7-classes"
          ),
          jarExecution(
            id = "jar-java6",
            classifier = "java6",
            classesDir = "${project.build.directory}/java6-classes"
          )
        )
      ),
      // Use Asciidoclet processor instead of standard Javadoc
      Plugin(
        Plugins.javadoc,
        configuration = Config(
          failOnError = "false",
          source = "${maven.compiler.source}",
          doclet = "org.asciidoctor.Asciidoclet",
          docletArtifact = Config(
            groupId = Deps.asciiDoclet.groupId.get,
            artifactId = Deps.asciiDoclet.artifactId,
            version = Deps.asciiDoclet.version.get
          ),
          overview = "README.adoc",
          additionalparam = s"""--base-dir "$${project.basedir}"
            | --attribute "name=${gav.artifactId}"
						| --attribute "version=${gav.version.get}"
						| --attribute "lambdatestversion=${gav.version.get}"
						| --attribute "documentationversion=${gav.version.get}"
						| --attribute "javasuffix=.html"
						| --attribute "title-link=${url}[${gav.artifactId} ${gav.version.get}]"
						| --attribute "env-asciidoclet=true"""".stripMargin
        )
      ),
      Plugin(
        Plugins.reproducibleBuild,
        executions = Seq(
          Execution(
            goals = Seq("strip-jar")
          )
        )
      )
    )
  ),
  profiles = Seq(
    genPomXmlProfile
  ),
  modelVersion = "4.0.0"
)
