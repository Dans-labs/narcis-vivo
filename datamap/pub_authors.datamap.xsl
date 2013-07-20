<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:dcterms='http://purl.org/dc/terms/' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
	xmlns:bibo='http://purl.org/ontology/bibo/'>

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="pub_id" select="$this/db:PUBLICATION_ID" />
		<xsl:param name="per_id" select="$this/db:PERS_ID" />
		<!-- Description of the authorship -->
		<rdf:Description rdf:about="{$baseURI}individual/Authorship_{$pub_id}_{$per_id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Authorship" />
			<vivo:linkedAuthor rdf:resource="{$baseURI}individual/Person_{$per_id}" />
			<vivo:linkedInformationResource
				rdf:resource="{$baseURI}individual/Publication_{$pub_id}" />
		</rdf:Description>
		<!-- Inverse relation for the publication -->
		<rdf:Description rdf:about="{$baseURI}individual/Publication_{$pub_id}">
			<vivo:informationResourceInAuthorship
				rdf:resource="{$baseURI}individual/Authorship_{$pub_id}_{$per_id}" />
		</rdf:Description>
		<!-- Inverse relation for the person -->
		<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
			<vivo:authorInAuthorship
				rdf:resource="{$baseURI}individual/Authorship_{$pub_id}_{$per_id}" />
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
