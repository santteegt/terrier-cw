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

read -p "Enter absoulte path for Clueweb data files (default: current_dir/clueweb12): " CLUEWEB_DIR

if [ ! -n "$CLUEWEB_HOME" ];
then
	CLUEWEB_DIR=`pwd`"/clueweb12"
fi

#CONFIGURING TERRIER FOR TREC
$TERRIER_HOME"/bin/trec_setup.sh" $CLUEWEB_DIR
find $CLUEWEB_DIR -type f | sort | grep -v info > $TERRIER_HOME"/etc/collection.spec"

#SETTING TERRIER PROPERTIES DIR
TERRIER_PROP=$TERRIER_HOME"/etc/terrier.properties"
rm $TERRIER_PROP


####################################################################################
############################ TREC DEFAULT CONFIGURATION ############################
####################################################################################
echo "#default controls for query expansion" >> $TERRIER_PROP
echo "querying.postprocesses.order=QueryExpansion" >> $TERRIER_PROP
echo "querying.postprocesses.controls=qe:QueryExpansion" >> $TERRIER_PROP
echo "#default controls for the web-based interface. SimpleDecorate" >> $TERRIER_PROP
echo "#is the simplest metadata decorator. For more control, see Decorate." >> $TERRIER_PROP
echo "querying.postfilters.order=SimpleDecorate,SiteFilter,Scope" >> $TERRIER_PROP
echo "querying.postfilters.controls=decorate:SimpleDecorate,site:SiteFilter,scope:Scope" >> $TERRIER_PROP

echo "#default and allowed controls" >> $TERRIER_PROP
echo "querying.default.controls=" >> $TERRIER_PROP
echo "querying.allowed.controls=scope,qe,qemodel,start,end,site,scope" >> $TERRIER_PROP

echo "#document tags specification" >> $TERRIER_PROP
echo "#for processing the contents of" >> $TERRIER_PROP
echo "#the documents, ignoring DOCHDR" >> $TERRIER_PROP
echo "TrecDocTags.doctag=DOC" >> $TERRIER_PROP
echo "TrecDocTags.idtag=DOCNO" >> $TERRIER_PROP
echo "#TrecDocTags.skip=DOCHDR #commented" >> $TERRIER_PROP
echo "#set to true if the tags can be of various case" >> $TERRIER_PROP
echo "TrecDocTags.casesensitive=false" >> $TERRIER_PROP

echo "#set of tags to process" >> $TERRIER_PROP
echo "TrecQueryTags.process=TOP,NUM,TITLE" >> $TERRIER_PROP
echo "#set of tags to skip" >> $TERRIER_PROP
echo "TrecQueryTags.skip=DESC,NARR" >> $TERRIER_PROP

echo "#stop-words file" >> $TERRIER_PROP
echo "stopwords.filename=stopword-list.txt" >> $TERRIER_PROP

echo "#the processing stages a term goes through" >> $TERRIER_PROP
echo "termpipelines=Stopwords,PorterStemmer" >> $TERRIER_PROP


####################################################################################
############################CONFIGURATION FOR COURSEWORK############################
####################################################################################
echo "#Coursework 1 configuration" >> $TERRIER_PROP
echo "#terrier home directory" >> $TERRIER_PROP
#echo 'terrier.home="/Users/santteegt/GitRepositories/irdm-terrier-trec"' >> $TERRIER_PROP
echo "trec.collection.class=SimpleFileCollection" >> $TERRIER_PROP
echo "indexing.simplefilecollection.extensionsparsers=txt:TaggedDocument" >> $TERRIER_PROP
echo "#Tokeniser class declared explicitly" >> $TERRIER_PROP
echo "tokeniser=org.terrier.indexing.tokenisation.EnglishTokeniser" >> $TERRIER_PROP
echo "#convert document words to lowercase" >> $TERRIER_PROP
echo "lowercase=true" >> $TERRIER_PROP
echo "#prefix for index filenames" >> $TERRIER_PROP
echo "terrier.index.prefix=terrier_clueweb_index" >> $TERRIER_PROP
echo "#indexer properties" >> $TERRIER_PROP
echo "indexer.meta.forward.keys=docno" >> $TERRIER_PROP
echo "indexer.meta.forward.keylens=26" >> $TERRIER_PROP
echo "#unique id for documents" >> $TERRIER_PROP
echo "indexer.meta.reverse.keys=docno" >> $TERRIER_PROP
echo "#tags to skip" >> $TERRIER_PROP
echo "TrecDocTags.skip=script,style" >> $TERRIER_PROP

echo "#denotes the beginning of the document" >> $TERRIER_PROP
echo "TrecQueryTags.doctag=title" >> $TERRIER_PROP
echo "#tag to use as unique identifier" >> $TERRIER_PROP
echo "TrecQueryTags.idtag=num" >> $TERRIER_PROP

#RUNNING THE INDEXING
$TERRIER_HOME/bin/trec_terrier.sh -i -j

