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
		<xsl:param name="id_org" select="$this/db:ORG_ID" />
		<!-- Connect the person to the position -->
		<rdf:Description rdf:about="{$baseURI}individual/Person_{$id_pers}">
			<vivo:personInPosition
				rdf:resource="{$baseURI}individual/Position_{$id_pers}_{$id_org}" />
			<xsl:if test="not( $this/db:EMAIL = 'null' )">
				<vivo:email>
					<xsl:value-of select="$this/db:EMAIL" />
				</vivo:email>
			</xsl:if>
			<xsl:if test="not( $this/db:TELEFOON = 'null' )">
				<vivo:phoneNumber>
					<xsl:value-of select="$this/db:TELEFOON" />
				</vivo:phoneNumber>
			</xsl:if>
		</rdf:Description>
		<!-- Describe the position -->
		<rdf:Description rdf:about="{$baseURI}individual/Position_{$id_pers}_{$id_org}">
			<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Position" />
			<vivo:positionForPerson rdf:resource="{$baseURI}individual/Person_{$id_pers}" />
			<vivo:positionInOrganization
				rdf:resource="{$baseURI}individual/Organization_{$id_org}" />
			<xsl:if test="not( $this/db:LEEROPDRACHT_EN = 'null' )">
				<rdfs:label>
					<xsl:value-of select="$this/db:LEEROPDRACHT_EN" />
				</rdfs:label>
				<rdfs:label xml:lang='en'>
					<xsl:value-of select="$this/db:LEEROPDRACHT_EN" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:LEEROPDRACHT = 'null' )">
				<rdfs:label xml:lang='nl'>
					<xsl:value-of select="$this/db:LEEROPDRACHT" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:OMSCHRIJVING_EN = 'null' )">
				<rdfs:comment>
					<xsl:value-of select="$this/db:OMSCHRIJVING_EN" />
				</rdfs:comment>
				<rdfs:comment xml:lang='en'>
					<xsl:value-of select="$this/db:OMSCHRIJVING_EN" />
				</rdfs:comment>
			</xsl:if>
			<xsl:if test="not( $this/db:OMSCHRIJVING = 'null' )">
				<rdfs:comment xml:lang='nl'>
					<xsl:value-of select="$this/db:OMSCHRIJVING" />
				</rdfs:comment>
			</xsl:if>
			<xsl:if test="not( $this/db:URL_ID = 'null' )">
				<vivo:webpage rdf:resource="{$baseURI}individual/{$this/db:URL_ID}" />
			</xsl:if>
		</rdf:Description>
		<!-- Add the inverse property of vivo:positionInOrganization -->
		<rdf:Description rdf:about="{$baseURI}individual/Organization_{$id_org}">
			<vivo:organizationForPosition
				rdf:resource="{$baseURI}individual/Position_{$id_pers}_{$id_org}" />
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
 