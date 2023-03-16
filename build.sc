// build.sc for mill
import $ivy.`de.tototec::de.tobiasroeser.mill.osgi::0.4.0-34-8cac07`

import mill._
import mill.scalalib._
import mill.scalalib.publish._
import coursier.Repository
import de.tobiasroeser.mill.osgi._
import mill.define.{Target, Task}
import mill.modules.Jvm
import os.Path

object Deps {
  val testng = ivy"org.testng:testng:6.11"
  val junit = ivy"junit:junit:4.12"
  val jupiter = ivy"org.junit.jupiter:junit-jupiter:5.9.2"
  val jupiterLauncher = ivy"org.junit.platform:junit-platform-launcher:1.7.0"
  val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
  val logbackClassic = ivy"ch.qos.logback:logback-classic:1.2.3"
  val java17Signature = ivy"org.codehaus.mojo.signature:java17:1.0"
}

object lambdatest extends MavenModule with PublishModule with OsgiBundleModule {

  override def osgiBuildMode: OsgiBundleModule.BuildMode = OsgiBundleModule.BuildMode.CalculateManifest

  override def millSourcePath: Path = super.millSourcePath / os.up

  override def publishVersion = "0.8.1-SNAPSHOT"
  override def versionScheme: Target[Option[VersionScheme]] = Some(VersionScheme.EarlySemVer)

  val namespace = "de.tobiasroeser.lambdatest"
  override def artifactName = namespace
  override def bundleSymbolicName = artifactName

  def pomSettings = PomSettings(
    organization = "de.tototec",
    description = "Lambda-enabled functional testing on top of JUnit and TestNG",
    url = "https://github.com/lefou/LambdaTest",
    developers = Seq(
      Developer("lefou", "Tobias Roeser", "https://github.com/lefou")
    ),
    licenses = Seq(
      License.`Apache-2.0`.copy(url = "http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    versionControl = VersionControl.github("lefou", "LambdaTest")
  )

  override def compileIvyDeps = Agg(
    Deps.testng,
    Deps.junit,
    Deps.jupiter,
    Deps.slf4j
  )

  override def osgiHeaders: T[OsgiHeaders] = super.osgiHeaders().copy(
    `Export-Package` = Seq(
      s"${namespace}",
      s"${namespace}.generic",
      s"${namespace}.junit",
      s"${namespace}.junit5",
      s"${namespace}.testng",
      s"${namespace}.proxy",
    ),
    `Import-Package` = Seq(
      "*;resolution:=optional"
    )
  )

  object test extends Tests with TestModule.TestNg {
    override def ivyDeps = super.ivyDeps() ++ Agg(
      Deps.testng,
      Deps.junit,
      Deps.jupiter,
      Deps.jupiterLauncher,
      Deps.logbackClassic
    )
  }

}
