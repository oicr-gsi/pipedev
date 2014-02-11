#!/bin/bash
#call this script name with 6 parameters
#./webservicestarter.sh 1 2 3 4 5 6 7 8 
#The params are in this order:
#TOMCATPORT, HOST, PORT, NAME, USER, PASS, WEBDIR, JAVALOCATION
MINPARAM=8

#checks to make sure that all parameters have arguments
if [[ "$#" -ne $MINPARAM ]]; then
	echo "There isn't a sufficient number of arguments to fill the parameters."
	exit 1
fi

TOMCATPORT=$1 #tomcat port number
HOST=$2 #host of the database
PORT=$3 #port of the database host
NAME=$4 #name of db
USER=$5 #username 
PASS=$6 #password
WEBDIR=$7 #where seqware-webservice directory is located
JAVALOCATION=$8
#JAVALOCATION=${JAVALOCATION:0:${#JAVALOCATION}-9}

#this command starts up the webservice
JAVA_HOME=${JAVALOCATION} MAVEN_OPTS="-Xmx4096m -Xms4096m" /.mounts/labs/PDE/apache-maven-3.1.1/bin/mvn -Dmaven.tomcat.port=${TOMCATPORT} -Dseqware_meta_db_host=${HOST} -Dseqware_meta_db_port=${PORT} -Dseqware_meta_db_name=${NAME} -Dseqware_meta_db_user=${USER} -Dseqware_meta_db_password=${PASS} tomcat:run -f ${WEBDIR}/pom.xml

#returns the webservice that the program started
#hostname=$(hostname -f)
#webservice="${hostname}:${TOMCATPORT}/seqware-webservice/"
#echo ${webservice}
