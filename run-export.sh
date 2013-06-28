#!/bin/bash

###########################################################
# Configuration parameters
###########################################################
export HARVEST_NAME=vivo-vu-data
HARVESTER_INSTALL_DIR=/home/cgueret/Downloads/harvester-1.5
CSV_DIR=/home/cgueret/Dropbox/Documents/Projects/DANS/Vivo/data/csv

# Set extra parameters
export DATE=`date +%Y-%m-%d'T'%T`
export PATH=$PATH:$HARVESTER_INSTALL_DIR/bin
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/bin/harvester.jar:$HARVESTER_INSTALL_DIR/bin/dependency/*
export CLASSPATH=$CLASSPATH:$HARVESTER_INSTALL_DIR/build/harvester.jar:$HARVESTER_INSTALL_DIR/build/dependency/*
DIAGNOSTIC_OPTS="-XX:+PrintG -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:HeapDumpPath /tmp/harvester/dumps/ -XX:-HeapDumpOnOutOfMemoryError"
OPTS="-Xms1g -Xmx2g -server -d64 -Dharvester-task=$HARVEST_NAME.$DATE"

# Stop at first error
set -e

###########################################################
# Function to process a file
# parameter: full path of a CSV file
###########################################################
process ()
{
	csv_file=$1
	csv_name=$(basename $1)
	table=$(echo "$csv_name" | sed -e 's/_vivo_//' | sed -e 's/\.csv//')

	if [ -f datamap/$table.datamap.xsl ]; then
		#
		# Step 1: import and adapt the CSV
		# 
		echo "raw CSV -> CSV"
		cleaned_csv=/tmp/harvester/csv/$table.csv
		cp $csv_file $cleaned_csv
		# Turn the N into null
		sed -i -e 's/\\N/null/g' $cleaned_csv
		# Remove all the '
		sed -i -e "s/'//g" $cleaned_csv
		# Replace tabs with commas
		sed -i -e 's/,/;/g' $cleaned_csv
		sed -i -e 's/\t/,/g' $cleaned_csv
		# Insert null when no data
		sed -i -e "s/,,/,null,/g" $cleaned_csv

		#
		# Step 2: import CSV into a disk based JDBC
		#
		echo "CSV -> JDBC"
		java $OPTS -Dprocess-task=CSVtoJDBC org.vivoweb.harvester.util.CSVtoJDBC \
		--inputFile $cleaned_csv \
		--driver org.h2.Driver \
		--connection jdbc:h2:/tmp/harvester/store \
		--username vivo \
		--password vivo \
		--tableName csv 2>&1 > /dev/null

		#
		# Step 3: extract the content of the JDBC into raw records
		#
		echo "JDBC -> XML"
		java $OPTS -Dprocess-task=JDBCFetch org.vivoweb.harvester.fetch.JDBCFetch \
		--driver org.h2.Driver \
		--connection jdbc:h2:/tmp/harvester/store \
		--username vivo \
		--password vivo \
		--output raw-records.config.xml 2>&1 > /dev/null

		#
		# Step 4: translate the raw records into proper RDF
		#
		echo "XML -> RDF"
		java $OPTS -Dprocess-task=XSLTranslator org.vivoweb.harvester.translate.XSLTranslator \
		--input raw-records.config.xml \
		--output translated-records.config.xml \
		--xslFile datamap/$table.datamap.xsl 2>&1 > /dev/null

		#
		# Step 5: Concatenate all the triples
		#
		echo "Adjust namespace"
		for file in `ls /tmp/harvester/translated`; do
			rapper -q -i guess -o ntriples /tmp/harvester/translated/$file >> /tmp/harvester/triples/$table.nt
		done
		# replace name space
		sed -i -e 's/example.org/localhost:8080\/vivo/g' /tmp/harvester/triples/$table.nt
		
		#
		# Clean up
		#
		rm -f /tmp/harvester/store*
		rm -rf /tmp/harvester/raw/*
		rm -rf /tmp/harvester/translated/*
	fi
}


###########################################################
# Beginning of harvest process
###########################################################

# Erase previous harvesting data if any
rm -rf /tmp/harvester
mkdir -p /tmp/harvester/triples
mkdir -p /tmp/harvester/csv

# Iterate over all the CSV
for file in $CSV_DIR/*.csv; do
	echo $file
	process $file
done
#process /home/cgueret/Dropbox/Documents/Projects/DANS/Vivo/data/csv/_vivo_pub.csv
#process /home/cgueret/Dropbox/Documents/Projects/DANS/Vivo/data/csv/_vivo_pub_authors.csv
#process /home/cgueret/Dropbox/Documents/Projects/DANS/Vivo/data/csv/_vivo_pub_journals.csv

# Wipe out all the current data
echo "Wipe out previous data"
harvester-jenaconnect -j vivo.model.xml -t 2>&1 > /dev/null

# Push the new triples
echo "Push new data"
for file in /tmp/harvester/triples/*.nt; do
	echo $file
	harvester-transfer -o vivo.model.xml -r $file -R N-TRIPLE 2>&1 > /dev/null
done

# Check that some triples made it to the store
echo "Test"
harvester-jenaconnect -j vivo.model.xml -q 'select * where { ?s ?p ?o} limit 10' 
