<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:skos='http://www.w3.org/2004/02/skos/core#'
	xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id" select="$this/db:ONDZ_ID" />
		<!-- Describe the project -->
		<rdf:Description rdf:about="{$baseURI}individual/Project_{$id}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Project" />
			<xsl:if test="not( $this/db:NAAM_EN = 'null' )">
				<rdfs:label xml:lang='en'>
					<xsl:value-of select="$this/db:TITEL_EN" />
				</rdfs:label>
				<rdfs:label>
					<xsl:value-of select="$this/db:TITEL_EN" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:START_DATE = 'null' )">
				<vivo:dateTimeInterval
					rdf:resource="{$baseURI}individual/DateTimeInterval_{$this/db:START_DATE}_{$this/db:END_DATE}" />
			</xsl:if>
			<xsl:if test="not( $this/db:OMSCHRIJVING_EN = 'null' )">
				<vivo:description>
					<xsl:value-of select="$this/db:OMSCHRIJVING_EN" />
				</vivo:description>
			</xsl:if>
			<xsl:if test="not( $this/db:GRANT_ID = 'null' )">
				<vivo:hasFundingVehicle
					rdf:resource="{$baseURI}individual/Grant_{$this/db:GRANT_ID}" />
			</xsl:if>
		</rdf:Description>
		<!-- This information should be in the grant table -->
		<xsl:if test="not( $this/db:GRANT_ID = 'null' )">
			<xsl:if test="not( $this/db:AMOUNTAWARDED = 'null' )">
				<rdf:Description rdf:about="{$baseURI}individual/Grant_{$this/db:GRANT_ID}">
					<vivo:totalAwardAmount>
						<xsl:value-of select="$this/db:AMOUNTAWARDED" />
					</vivo:totalAwardAmount>
				</rdf:Description>
			</xsl:if>
		</xsl:if>
		<!-- The date time interval -->
		<xsl:if test="not( $this/db:START_DATE = 'null' )">
			<rdf:Description
				rdf:about="{$baseURI}individual/DateTimeInterval_{$this/db:START_DATE}_{$this/db:END_DATE}">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeInterval" />
				<vivo:start
					rdf:resource="{$baseURI}individual/DateTimeValue_{$this/db:START_DATE}" />
				<xsl:if test="not( $this/db:END_DATE = 'null' )">
					<vivo:end rdf:resource="{$baseURI}individual/DateTimeValue_{$this/db:END_DATE}" />
				</xsl:if>
			</rdf:Description>
		</xsl:if>
		<!-- Date time value for start -->
		<xsl:if test="not( $this/db:START_DATE = 'null' )">
			<rdf:Description
				rdf:about="{$baseURI}individual/DateTimeValue_{$this/db:START_DATE}">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
				<vivo:dateTime>
					<xsl:value-of select="$this/db:START_DATE" />
				</vivo:dateTime>
			</rdf:Description>
		</xsl:if>
		<!-- Date time value for end -->
		<xsl:if test="not( $this/db:END_DATE = 'null' )">
			<rdf:Description
				rdf:about="{$baseURI}individual/DateTimeValue_{$this/db:END_DATE}">
				<rdf:type rdf:resource="http://vivoweb.org/ontology/core#DateTimeValue" />
				<vivo:dateTime>
					<xsl:value-of select="$this/db:END_DATE" />
				</vivo:dateTime>
			</rdf:Description>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
 