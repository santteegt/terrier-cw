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
echo "#INDEXING properties" >> $TERRIER_PROP
echo "terrier.index.path=/Users/santteegt/terrier-core-4.1/var/index" >> $TERRIER_PROP
echo "terrier.etc=/Users/santteegt/terrier-core-4.1/etc" >> $TERRIER_PROP
echo "terrier.share=/Users/santteegt/terrier-core-4.1/share" >> $TERRIER_PROP
echo "collection.spec=/Users/santteegt/terrier-core-4.1/etc/collection.spec" >> $TERRIER_PROP
echo "FieldTags.process=TITLE,H1,H2,H3,H4,H5,H6,ELSE" >> $TERRIER_PROP
echo "trec.collection.class=SimpleFileCollection" >> $TERRIER_PROP
echo "indexing.simplefilecollection.extensionsparsers=txt:TaggedDocument" >> $TERRIER_PROP
echo "indexer.meta.forward.keys=filename" >> $TERRIER_PROP
echo "indexer.meta.reverse.keys=filename" >> $TERRIER_PROP
echo "indexer.meta.forward.keylens=64" >> $TERRIER_PROP

echo "lowercase=true" >> $TERRIER_PROP
echo "tokeniser=EnglishTokeniser" >> $TERRIER_PROP
echo "terrier.index.prefix=terrier_clueweb_index" >> $TERRIER_PROP
echo "#end of coursework conf." >> $TERRIER_PROP

#RUNNING THE INDEXING
curr=`pwd`
cd $TERRIER_HOME
./bin/trec_terrier.sh -i
cd $curr

