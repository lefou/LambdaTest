import de.tototec.sbuild._

@version("0.7.0")
class SBuild(implicit _project: Project) {

  Target("phony:compileCp") dependsOn "mvn:org.testng:testng:6.8.8"

}
