#!/bin/bash
#call this script name with 6 parameters
#./dbpatcher.sh 1 2 3 4 5 6 7
#The params are in this order:
#DBDUMP, PATCHDIR, DBHOST, PORT, NAME, USER, PASS
MINPARAM=7

#checks to make sure that all parameters have arguments
if [[ "$#" -ne $MINPARAM ]]; then
	echo "There isn't a sufficient number of arguments to fill the parameters."
	exit 1
fi


DBDUMP=$1 #the database that you wish to restore
PATCHDIR=$2 #directory where patches reside
DBHOST=$3 #database host
PORT=$4 #port number
NAME=$5 #name of the new database
USER=$6 #make sure the user has sufficient rights and privelges, such as createDB.
PASS=$7
export PGPASSWORD=${PASS} #allows user authentication without pgpass
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


#creates the new database
psql -U $USER -h $DBHOST -p $PORT -d postgres -c "${DBCREATION}"
FAIL=$?
if [[ $FAIL -ne "0" ]]; then
	echo "Could not create database. Please check the parameters for errors"
	exit $FAIL
fi

#restores the database
#the command also removes the users that are no longer present for the database
gunzip -c $DBDUMP |sed "s#^\(ALTER TABLE .*\)\( OWNER TO \)\(.*\)\(;$\)#\1\2${USER}\4#g" | grep -v "GRANT SELECT ON TABLE"|sed "s#^\(REVOKE ALL ON TABLE .*\)seqware\(;$\)#\1${USER}\2#g"|sed "s#^\(GRANT ALL ON TABLE .*\)seqware\(;$\)#\1${USER}\2#g"| psql -U ${USER} -h $DBHOST -p $PORT -d ${NAME}
FAIL=$?
 if [[ $FAIL -ne "0" ]]; then
 	#Could not access DB dump
 	
 	echo "Could not access the database dump."
 	exit $FAIL
 fi

 #applies the patches. The patch directory must make sure that the only files in it are patches.
 #NOTE: An error will appear when running the patch 1.0.2_to_1.0.3.sql. You can just ignore it
 for f in ${PATCHDIR}/*.sql; do
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
