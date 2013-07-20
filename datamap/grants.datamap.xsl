<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:vivo='http://vivoweb.org/ontology/core#' xmlns:bibo='http://purl.org/ontology/bibo/'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/'>

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id" select="$this/db:GRANT_ID" />
		<rdf:Description rdf:about="{$baseURI}individual/Grant_{$id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Grant" />
			<vivo:grantAwardedBy
				rdf:resource="{$baseURI}individual/Organization_{$this/db:ORG_ID}" />
			<rdfs:label>
				<xsl:value-of select="$this/db:GRANT" />
			</rdfs:label>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>