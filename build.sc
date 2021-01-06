// build.sc for mill
import java.io.File

import mill._
import mill.scalalib._
import mill.scalalib.publish._
import $ivy.`de.tototec::de.tobiasroeser.mill.osgi_mill0.9:0.3.2`
import coursier.Repository
import de.tobiasroeser.mill.osgi._
import mill.define.{Target, Task}
import mill.modules.Jvm
import os.Path

object Deps {
  val asciiDoclet = ivy"org.asciidoctor:asciidoclet:1.5.4"
  val testng = ivy"org.testng:testng:6.11"
  val junit = ivy"junit:junit:4.12"
  val slf4j = ivy"org.slf4j:slf4j-api:1.7.25"
  val logbackClassic = ivy"ch.qos.logback:logback-classic:1.2.3"
  val java17Signature = ivy"org.codehaus.mojo.signature:java17:1.0"
  val millTestng = ivy"com.lihaoyi:mill-contrib-testng_2.13:${sys.props("MILL_VERSION")}"
}

// TODO cross compile with retrolambda
object lambdatest extends MavenModule with PublishModule with OsgiBundleModule {

  override def osgiBuildMode: OsgiBundleModule.BuildMode = OsgiBundleModule.BuildMode.CalculateManifest

  override def millSourcePath: Path = super.millSourcePath / os.up

  override def publishVersion = "0.6.3-SNAPSHOT"

  override def artifactName = "de.tobiasroeser.lambdatest"
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

  def ivyDeps = Agg(
    Deps.testng,
    Deps.junit,
    Deps.slf4j
  )

  object test extends Tests {
    override def testFrameworks = Seq("mill.testng.TestNGFramework")
    override def ivyDeps = T{ Agg(
      Deps.millTestng,
      Deps.logbackClassic
    )}
  }

  object retro extends Cross[RetroCross]("7", "6")
  class RetroCross(version: String) extends RetrolambdaModule {
    override def bytecodeVersion: T[String] = version
    def baseModule: JavaModule = lambdatest
    override def retrolambdaVersion = T{ "2.5.7" }
  }

}

trait RetrolambdaWorker {
  def retrolambda(inputDir: Path, outputDir: Path, classpath: Seq[Path], bytecodeVersion: String)
}

trait RetrolambdaModule extends CoursierModule {
  def bytecodeVersion: T[String]
  def baseModule: JavaModule
  def compile: T[PathRef] = T{
    val dest = T.dest
    retrolambdaWorker().retrolambda(
      inputDir = baseModule.compile().classes.path,
      outputDir = dest,
      classpath = baseModule.compileClasspath().iterator.to(Seq).map(_.path),
      bytecodeVersion = bytecodeVersion()
    )
    PathRef(dest)
  }

  override def repositoriesTask: Task[Seq[Repository]] = T.task { baseModule.repositoriesTask() }

  def localClasspath: Target[Seq[PathRef]] = T{
    baseModule.resources() ++ Agg(compile())
  }

  def jar: T[PathRef] = T{
    Jvm.createJar(
      localClasspath().map(_.path).filter(os.exists),
      baseModule.manifest()
    )
  }
  def manifest: Target[Jvm.JarManifest] = T { baseModule.manifest() }

  def retrolambdaVersion: T[String]
  def retrolambdaIvyDeps: T[Agg[Dep]] = T{ Agg(ivy"net.orfjackal.retrolambda:retrolambda:${retrolambdaVersion()}".exclude("*" -> "*")) }
  def retrolambdaClasspath: T[Seq[PathRef]] = T{
    resolveDeps(retrolambdaIvyDeps)().iterator.to(Seq)
  }
  def retrolambdaWorker = T.worker {
//    val cl = new URLClassLoader(retrolambdaClasspath().map(_.path.toNIO.toUri().toURL()));
//    val clazz = cl.loadClass("net.orfjackal.retrolambda.Main")
//    val mainMethod = clazz.getMethod("main", Seq(classOf[Array[String]]): _*)
    new RetrolambdaWorker {
      override  def retrolambda(inputDir: Path, outputDir: Path, classpath: Seq[Path], bytecodeVersion: String): Unit = {
//        mainMethod.invoke(null, Seq(args): _*)
        Jvm.runSubprocess(
          mainClass = "net.orfjackal.retrolambda.Main",
          classPath = retrolambdaClasspath().map(_.path),
          jvmArgs = Seq(
            s"-Dretrolambda.inputDir=${inputDir.toIO.getPath()}",
            s"-Dretrolambda.outputDir=${outputDir.toIO.getPath()}",
            s"-Dretrolambda.classpath=${(Seq(inputDir) ++ classpath).map(_.toIO.getPath).mkString(File.pathSeparator)}",
            s"-Dretrolambda.bytecodeVersion=${bytecodeVersion}",
            s"-javaagent:${retrolambdaClasspath().head.path.toIO.getPath()}"
          ),
          workingDir = outputDir
        )
      }
    }
  }
}