<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#" xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/'
	xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:param name="this" select="." />
		<xsl:param name="id" select="$this/db:PERS_ID" />
		<rdf:Description rdf:about="{$baseURI}individual/Person_{$id}">
			<!-- Define the type of the person -->
			<rdf:type rdf:resource="http://xmlns.com/foaf/0.1/Person" />
			<xsl:if test="not( $this/db:TITULATUUR = 'null' )">
				<xsl:variable name="type" select="$this/db:TITULATUUR" />
				<vivo:preferredTitle>
					<xsl:value-of select="$type" />
				</vivo:preferredTitle>
				<xsl:choose>
					<xsl:when test="$type = 'Prof.dr.'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember" />
					</xsl:when>
					<xsl:when test="$type = 'Dr.'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#FacultyMember" />
					</xsl:when>
					<xsl:when test="$type = 'Mr.'">
						<rdf:type rdf:resource="http://vivoweb.org/ontology/core#NonAcademic" />
					</xsl:when>
				</xsl:choose>
			</xsl:if>

			<xsl:if test="not( $this/db:VOORVOEGSEL = 'null' )">
				<vivo:middleName>
					<xsl:value-of select="$this/db:VOORVOEGSEL" />
				</vivo:middleName>
			</xsl:if>
			<xsl:if test="not( $this/db:INITIALEN = 'null' )">
				<foaf:firstName>
					<xsl:value-of select="$this/db:INITIALEN" />
				</foaf:firstName>
			</xsl:if>
			<xsl:if test="not( $this/db:ACHTERNAAM = 'null' )">
				<foaf:lastName>
					<xsl:value-of select="$this/db:ACHTERNAAM" />
				</foaf:lastName>
			</xsl:if>
			<xsl:if test="not( $this/db:PREFERREDNAME = 'null' )">
				<rdfs:label>
					<xsl:value-of select="$this/db:PREFERREDNAME" />
				</rdfs:label>
			</xsl:if>
			<xsl:if test="not( $this/db:DAI = 'null' )">
				<vivonl:daiId>
					<xsl:value-of select="$this/db:DAI" />
				</vivonl:daiId>
			</xsl:if>
			<xsl:if test="not( $this/db:EMAIL = 'null' )">
				<vivo:primaryEmail>
					<xsl:value-of select="$this/db:EMAIL" />
				</vivo:primaryEmail>
			</xsl:if>
			<xsl:if test="not( $this/db:URL_ID = 'null' )">
				<vivo:webpage rdf:resource="{$baseURI}individual/{$this/db:URL_ID}" />
			</xsl:if>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
  