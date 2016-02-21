!#/bin/bash

if [ ! -n "$TERRIER_HOME" ];
then
	read -p "Enter TERRIER_HOME absoulte path directory (default: current dir): " home

	if [ ! -n "$home" ];
	then
		home=`pwd`
	fi
	TERRIER_HOME=$home 
fi

read -p "Enter absoulte path for Topics file: " TOPICS_FILE

if [ ! -n "$TOPICS_FILE" ];
then
	TOPICS_FILE="/Users/santteegt/Clueweb/topics/trec2013-topics.txt"
fi

TERRIER_PROP=$TERRIER_HOME"/etc/terrier.properties"

echo "trec.topics="$TOPICS_FILE >> $TERRIER_PROP
echo "trec.model=org.terrier.matching.models.BM25" >> $TERRIER_PROP

#RUNNING THE RETRIEVAL PROCESS BASED ON TOPIC FILES
$TERRIER_HOME/bin/trec_terrier.sh -r

