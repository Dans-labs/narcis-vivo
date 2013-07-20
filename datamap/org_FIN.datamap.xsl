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
		<xsl:param name="id" select="$this/db:ORG_ID" />
		<!-- Describe the organization -->
		<rdf:Description rdf:about="{$baseURI}individual/Organization_{$id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FundingOrganization" />
			<xsl:if test="not( $this/db:NAAM = 'null' )">
				<rdfs:label xml:lang='nl'>
					<xsl:value-of select="$this/db:NAAM" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:NAAM_EN = 'null' )">
				<rdfs:label xml:lang='en'>
					<xsl:value-of select="$this/db:NAAM_EN" />
				</rdfs:label>
				<rdfs:label>
					<xsl:value-of select="$this/db:NAAM_EN" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:ADRES_ID = 'null' )">
				<vivo:mailingAddress rdf:resource="{$baseURI}individual/{$this/db:ADRES_ID}" />
			</xsl:if>
			<xsl:if test="not( $this/db:TELEFOON = 'null' )">
				<vivo:primaryPhoneNumber>
					<xsl:value-of select="$this/db:TELEFOON" />
				</vivo:primaryPhoneNumber>
			</xsl:if>
			<xsl:if test="not( $this/db:URL_ID = 'null' )">
				<vivo:webpage rdf:resource="{$baseURI}individual/{$this/db:URL_ID}" />
			</xsl:if>
			<xsl:if test="not( $this/db:EMAIL = 'null' )">
				<vivo:primaryEmail>
					<xsl:value-of select="$this/db:EMAIL" />
				</vivo:primaryEmail>
			</xsl:if>
			<xsl:if test="not( $this/db:BOVENLIGGEND_ORG_ID = 'null' )">
				<vivo:subOrganizationWithin
					rdf:resource="{$baseURI}individual/Organization_{$this/db:BOVENLIGGEND_ORG_ID}" />
			</xsl:if>
			<xsl:if test="not( $this/db:ACRONIEM = 'null' )">
				<vivo:abbreviation>
					<xsl:value-of select="$this/db:ACRONIEM" />
				</vivo:abbreviation>
			</xsl:if>
			<xsl:if test="not( $this/db:TAAK_EN = 'null' )">
				<vivo:overview>
					<xsl:value-of select="$this/db:TAAK_EN" />
				</vivo:overview>
				<vivo:overview xml:lang='en'>
					<xsl:value-of select="$this/db:TAAK_EN" />
				</vivo:overview>
			</xsl:if>
			<xsl:if test="not( $this/db:TAAK = 'null' )">
				<vivo:overview xml:lang='nl'>
					<xsl:value-of select="$this/db:TAAK" />
				</vivo:overview>
			</xsl:if>
		</rdf:Description>
		<!-- Add inverse link for mailing address -->
		<xsl:if test="not( $this/db:ADRES_ID = 'null' )">
			<rdf:Description rdf:about="{$baseURI}individual/{$this/db:ADRES_ID}">
				<vivo:mailingAddressFor rdf:resource="{$baseURI}individual/Organization_{$id}" />
			</rdf:Description>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
 