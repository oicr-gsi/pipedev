#!/bin/bash
#call this script name with 7 parameters
#./dbpatcher.sh 1 2 3 4 5 6
#The params are in this order:
#DBDUMP, PATCHDIR, DBHOST, PORT, NAME, USER
MINPARAM=6

#checks to make sure that all parameters have arguments
if [[ "$#" -ne $MINPARAM ]]; then
	echo "There isn't a sufficient number of arguments to fill the parameters."
	exit 1
fi


DBDUMP=$1
PATCHDIR=$2
DBHOST=$3
PORT=$4
NAME=$5
USER=$6 #make sure the user is a superuser


#Tests and connects to the DB host and quits immediately after
psql -U ${USER} -h $DBHOST -p $PORT -d postgres -c "\q"
FAIL=$?
 if [[ $FAIL -ne "0" ]]; then
 	#Could not connect to the DB server
 	
 	echo "Could not connect to database host. Please check for mistakes in the database host and port parameters"
 	exit $FAIL
 fi


#Error check to create the database host
DBCREATION="CREATE DATABASE ${NAME} WITH OWNER = ${USER};"

#turns the name into lowercase
NAME=$(echo $NAME| sed 's/.*/\L&/')

psql -U $USER -h $DBHOST -p $PORT -d postgres -c "${DBCREATION}"
FAIL=$?
if [[ $FAIL -ne "0" ]]; then
	echo "Could not create database. Please check the parameters for errors"
	exit $FAIL
fi

#restores the database
gunzip -c $DBDUMP |sed "s#^\(ALTER TABLE .*\)\( OWNER TO \)\(.*\)\(;$\)#\1\2${USER}\4#g" | grep -v "GRANT SELECT ON TABLE"|sed "s#^\(REVOKE ALL ON TABLE .*\)seqware\(;$\)#\1${USER}\2#g"|sed "s#^\(GRANT ALL ON TABLE .*\)seqware\(;$\)#\1${USER}\2#g"| psql -U ${USER} -h $DBHOST -p $PORT -d ${NAME}
FAIL=$?
 if [[ $FAIL -ne "0" ]]; then
 	#Could not access DB dump
 	
 	echo "Could not access the database dump."
 	exit $FAIL
 fi

 #applies the patches. The patch directory must make sure that the only files in it are patches
 for f in ${PATCHDIR}/*; do
 	echo $f
 	psql -U ${USER} -h ${DBHOST} -p ${PORT} ${NAME} < "$f"
 done

 #error check for patches
 FAIL=$?
 if [[ $FAIL -ne "0" ]]; then
 	
 	echo "Failed to patch."
 	exit $FAIL
 fi
 echo "The database has now been updated. If necessary, make sure you run the java migration plugin."
 exit 0