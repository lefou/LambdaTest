#!/bin/sh

if [ ! -f mvn-deploy-settings.xml ]; then
	echo "Creating mvn-deploy-settings.xml"
	cat > mvn-deploy-settings.xml <<EOF
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

echo "Please review / edit mvn-deploy-settings.xml with propper connection details. Press Enter"
read

#echo "Preparing Repository"
#mvn -s mvn-settings.xml clean
#mvn -s mvn-settings.xml package test source:jar javadoc:jar

echo "Cleaning, building, signing and uploading. Press Enter"
read

REPO=https://oss.sonatype.org/service/local/staging/deploy/maven2/

mvn\
 -s ./mvn-deploy-settings.xml\
 clean package source:jar javadoc:jar gpg:sign deploy:deploy\
 -DaltDeploymentRepository=ossrh::default::${REPO}

echo "Don't forget to delete sensitive information from mvn-deploy-settings.xml"
