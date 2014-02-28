#!/bin/bash 

IDENTIFIER=$2
FILE1=`basename $4`
FILE2=`basename $6`
LIBRARY=$7

qsub -cwd -N Job1-$IDENTIFIER -q default -e log -o log -b y "echo TestTestHello2 > $FILE2.hello2; echo $LIBRARY; sleep 30; exit 0"
qsub -cwd -N Job2-$IDENTIFIER -q default -e log2 -o log2 -b y "echo TestTestHello1 > $FILE1.hello; echo $LIBRARY; sleep 30; exit 0"
