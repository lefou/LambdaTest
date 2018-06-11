.PHONY: all clean distclean eclipse

all: pom.xml
	mvn install

clean:
	rm -r target

distclean: clean
	rm pom.xml .project .classpath
	rm -r .settings

pom.xml: pom.scala
	mvn -Pgen-pom-xml initialize

eclipse: pom.xml
	mvn initialize de.tototec:de.tobiasroeser.eclipse-maven-plugin:0.1.1:eclipse
