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

#CONFIGURING TERRIER FOR TREC
#$TERRIER_HOME"/bin/trec_setup.sh" $CLUEWEB_DIR
#find $CLUEWEB_DIR -type f | sort | grep -v info > $TERRIER_HOME"/etc/collection.spec"

#SETTING TERRIER PROPERTIES DIR
TERRIER_PROP=$TERRIER_HOME"/etc/terrier.properties"
#rm $TERRIER_PROP


####################################################################################
############################CONFIGURATION FOR COURSEWORK############################
####################################################################################
#initial coursework conf

echo "\n#INDEXING properties" >> $TERRIER_PROP

echo "#Fields to be added to the index" >> $TERRIER_PROP
echo "FieldTags.process=TITLE,H1, H2, H3, H4, H5, H6, ELSE" >> $TERRIER_PROP
echo "#Collection docs type and Parser" >> $TERRIER_PROP
echo "trec.collection.class=SimpleFileCollection" >> $TERRIER_PROP
echo "indexing.simplefilecollection.extensionparsers=txt:TaggedDocument" >> $TERRIER_PROP
echo "#Stop words and term pipelines for parsing stage" >> $TERRIER_PROP
echo "stopwords.filename=stopword-list.txt" >> $TERRIER_PROP
echo "termpipelines=Stopwords ,PorterStemmer" >> $TERRIER_PROP
echo "#Storing the filename into the index" >> $TERRIER_PROP
echo "indexer.meta.forward.keys=filename" >> $TERRIER_PROP
echo "indexer.meta.reverse.keys=filename" >> $TERRIER_PROP
echo "indexer.meta.forward.keylens=64" >> $TERRIER_PROP

echo "#convert document words to lowercase" >> $TERRIER_PROP
echo "lowercase=true" >> $TERRIER_PROP
echo "#Tokeniser class declared explicitly" >> $TERRIER_PROP
echo "tokeniser=org.terrier.indexing.tokenisation.EnglishTokeniser" >> $TERRIER_PROP
echo "#prefix for index filenames" >> $TERRIER_PROP
echo "terrier.index.prefix=terrier_clueweb_index" >> $TERRIER_PROP

echo "#end of coursework conf." >> $TERRIER_PROP

#RUNNING THE INDEXING
$TERRIER_HOME/bin/trec_terrier.sh -i

