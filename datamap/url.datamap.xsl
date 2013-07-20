<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:vivo='http://vivoweb.org/ontology/core#' xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/'>

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id" select="$this/db:URL_ID" />
		<rdf:Description rdf:about="{$baseURI}individual/{$id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#URLLink" />
			<xsl:if test="not( $this/db:URL = 'null' )">
				<vivo:linkURI>
					<xsl:value-of select="$this/db:URL" />
				</vivo:linkURI>
				<vivo:linkAnchorText>
					<xsl:value-of select="$this/db:URL" />
				</vivo:linkAnchorText>
			</xsl:if>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>