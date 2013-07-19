#!/bin/bash

DATA=$1

###########################################################
# Configuration parameters
###########################################################
export HARVEST_NAME=vivo-$DATA
HARVESTER_INSTALL_DIR=./harvester
CSV_DIR=./data/VIVO/$DATA
NT_DIR=./triples/$DATA

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
	mkdir -p /tmp/harvester/csv
	mkdir -p $NT_DIR

	csv_file=$1
	csv_name=$(basename $1)
	table=$(echo "$csv_name" | sed -e 's/\.csv//')

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
		rm -f $NT_DIR/$table.nt
		for file in `ls /tmp/harvester/translated`; do
			rapper -q -i guess -o ntriples /tmp/harvester/translated/$file >> $NT_DIR/$table.nt
		done
		# replace name space
		# sed -i -e 's/example.org/localhost:8080\/vivo/g' $NT_DIR/$table.nt
		
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

# Iterate over all the CSV
for file in $CSV_DIR/*.csv; do
	echo $file
	process $file
done

