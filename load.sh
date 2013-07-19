#!/bin/bash

SOURCES='eur kun ouh rug rul rum ruu tud tue tum uva uvt vua wur' 

for source in $SOURCES;
do
	rm -f /tmp/$source.nt
	touch /tmp/$source.nt
	echo $source
	for file in data/VIVO/$source/*
	do
		cat $file >> /tmp/$source.nt
	done
	curl -v -T /tmp/$source.nt -H "Content-Type: text/plain;charset=UTF-8" http://localhost:8888/openrdf-sesame/repositories/$source/statements
done
