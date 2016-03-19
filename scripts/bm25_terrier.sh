#!/bin/bash

if [ ! -n "$TERRIER_HOME" ];
then
	read -p "Enter TERRIER_HOME absoulte path directory (default: current dir): " home

	if [ ! -n "$home" ];
	then
		home=`pwd`
	fi
	TERRIER_HOME=$home 
fi

read -p "Enter absoulte path for adhoc topics file (default: TERRIER_HOME/trec2013-topics.txt): " TOPICS_FILE

if [ ! -n "$TOPICS_FILE" ];
then
	TOPICS_FILE=$TERRIER_HOME"/trec2013-topics.txt"
fi

read -p "Enter absoulte path for adhoc judgement (qrels) file (default: TERRIER_HOME/qrels.adhoc.txt): " QRELS_FILE

if [ ! -n "$QRELS_FILE" ];
then
	QRELS_FILE=$TERRIER_HOME"/qrels.adhoc.txt"
fi



TERRIER_PROP=$TERRIER_HOME"/etc/terrier.properties"

echo "\n#RETRIEVAL properties" >> $TERRIER_PROP
echo "#Adhoc topic parser conf" >> $TERRIER_PROP
echo "trec.topics="$TOPICS_FILE >> $TERRIER_PROP
echo "trec.topics.parser=TRECQuery" >> $TERRIER_PROP
echo "TrecQueryTags.doctag=TOP" >> $TERRIER_PROP
echo "TrecQueryTags.idtag=NUM" >> $TERRIER_PROP
echo "TrecQueryTags.process=TOP,NUM,TITLE" >> $TERRIER_PROP
echo "#Retrieval model conf" >> $TERRIER_PROP
echo "trec.model=org.terrier.matching.models.BM25" >> $TERRIER_PROP
echo "trec.querying.outputformat.docno.meta.key=filename" >> $TERRIER_PROP
echo "#trec.results=PATH_TO_RESULTS_DIR" >> $TERRIER_PROP
echo "#Evaluation conf" >> $TERRIER_PROP
echo "#Relevance judgement files for adhoc evaluation" >> $TERRIER_PROP
echo "trec.qrels="$QRELS_FILE >> $TERRIER_PROP

echo "#end of coursework conf." >> $TERRIER_PROP

#RUNNING THE RETRIEVAL PROCESS BASED ON TOPIC FILES
$TERRIER_HOME/bin/trec_terrier.sh -r

