.PHONY: build # Build the project
build: pom.xml
	mvn install

.PHONY: clean # Clean the project
clean:
	rm -r target

.PHONY: distclean # Also clean generated project files
distclean: clean
	rm pom.xml .project .classpath
	rm -r .settings

#PHONY: pom.xml # Generate pom.xml
pom.xml: pom.scala
	mvn -Pgen-pom-xml initialize

.PHONY: eclipse # Generate eclipse project files
eclipse: pom.xml
	mvn initialize de.tototec:de.tobiasroeser.eclipse-maven-plugin:0.1.1:eclipse

.PHONY: help # List of targets with descriptions
help:
	@grep '^[#.]PHONY: .* #' Makefile | sed 's/[#.]PHONY: \(.*\) # \(.*\)/\1\t\2/' | expand -t20
