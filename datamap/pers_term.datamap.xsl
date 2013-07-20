<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:skos='http://www.w3.org/2004/02/skos/core#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id_pers" select="$this/db:PERS_ID" />
		<xsl:param name="id_term" select="$this/db:TERM" />
		<rdf:Description rdf:about="{$baseURI}individual/Person_{$id_pers}">
			<vivo:hasResearchArea rdf:resource="{$baseURI}individual/Concept_{$id_term}" />
		</rdf:Description>
		<rdf:Description rdf:about="{$baseURI}individual/Concept_{$id_term}">
			<vivo:researchAreaOf rdf:resource="{$baseURI}individual/Person_{$id_pers}" />
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
 