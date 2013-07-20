<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:skos='http://www.w3.org/2004/02/skos/core#' xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />
	<xsl:variable name="baseURICommon" select="'http://common.example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id" select="$this/db:TERM" />
		<rdf:Description rdf:about="{$baseURICommon}individual/Concept_{$id}">
			<rdf:type rdf:resource="http://www.w3.org/2004/02/skos/core#Concept" />
			<xsl:if test="not( $this/db:BROADERTERM = 'null' )">
				<skos:broader rdf:resource="{$baseURICommon}individual/Concept_{$this/db:BROADERTERM}" />
			</xsl:if>
			<xsl:if test="not( $this/db:TERM_NL = 'null' )">
				<skos:prefLabel xml:lang='nl'>
					<xsl:value-of select="$this/db:TERM_NL" />
				</skos:prefLabel>
			</xsl:if>
			<xsl:if test="not( $this/db:TERM_EN = 'null' )">
				<skos:prefLabel xml:lang='en'>
					<xsl:value-of select="$this/db:TERM_EN" />
				</skos:prefLabel>
				<rdfs:label>
					<xsl:value-of select="$this/db:TERM_EN" />
				</rdfs:label>
			</xsl:if>
		</rdf:Description>
		<!-- Add inverse relation -->
		<rdf:Description rdf:about="{$baseURICommon}individual/Concept_{$this/db:BROADERTERM}">
			<skos:narrower rdf:resource="{$baseURICommon}individual/Concept_{$id}" />
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
 