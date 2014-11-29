#!/bin/sh

if [ ! -f mvn-settings.xml ]; then
	echo "Creating mvn-settings.xml"
	cat > mvn-settings.xml <<EOF
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-username</username>
      <password>your-password</password>
    </server>
  </servers>
</settings>
EOF

fi

echo "Please edit mvn-settings.xml with propper connection details. Press Enter"
read

#echo "Preparing Repository"
#mvn -s mvn-settings.xml clean
#mvn -s mvn-settings.xml package test source:jar javadoc:jar

echo "Signing and uploading. Press Enter"
read

mvn -s ./mvn-settings.xml clean package source:jar javadoc:jar gpg:sign deploy:deploy -DaltDeploymentRepository=ossrh::default::https://oss.sonatype.org/service/local/staging/deploy/maven2/
