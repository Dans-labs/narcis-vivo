Import scripts NARCIS->VIVO
===
This repository contains mappings for the VIVO harvester to turn CSV data exported from NARCIS into RDF using the VIVO ontology


Status
==
* address: complete mapping
* pers: complete mapping
* pers_term: complete
* terms: complete
* pers_org: complete mapping
* org: complete mapping


Util
==
* Import something from Karma: grep "NAMESPACE" model.n3 | sed -e 's/\^/\n/g'
* http://stackoverflow.com/questions/8024392/awk-replace-a-column-with-its-hash-value
* http://stackoverflow.com/questions/1684963/xslt-obtaining-or-matching-hashes-for-base64-encoded-data
* scp /tmp/harvester/triples/*.nt lindar@demo.datanetworkservice.nl:/home/lindar/triples
