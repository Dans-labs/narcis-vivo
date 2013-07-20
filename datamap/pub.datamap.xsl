<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:dcterms='http://purl.org/dc/terms/' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
	xmlns:bibo='http://purl.org/ontology/bibo/' xmlns:md5="java.security.MessageDigest"
	xmlns:bigint="java.math.BigInteger" exclude-result-prefixes="md5 bigint">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://XXX.example.org/'" />
	
	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:variable name="this" select="." />
		<xsl:variable name="id" select="$this/db:PUBLICATION_ID" />
		<xsl:variable name="jnl_id" select="$this/db:JOURNALID" />
		<xsl:variable name="genre" select="$this/db:GENRE" />
		<rdf:Description rdf:about="{$baseURI}individual/Publication_{$id}">
			<!-- General attributes -->
			<rdfs:label>
				<xsl:value-of select="$this/db:TITLE" />
			</rdfs:label>
			<dcterms:title>
				<xsl:value-of select="$this/db:TITLE" />
			</dcterms:title>
			<!-- Additions per type -->
			<xsl:choose>
				<xsl:when test="$genre = 'annotation'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Note" />
					<bibo:annotates rdf:resource="{$baseURI}individual/Journal_{$jnl_id}" />
				</xsl:when>
				<xsl:when test="$genre = 'article'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/AcademicArticle" />
					<vivo:hasPublicationVenue rdf:resource="{$baseURI}individual/Journal_{$jnl_id}" />
				</xsl:when>
				<xsl:when test="$genre = 'book'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Book" />
				</xsl:when>
				<xsl:when test="$genre = 'book part'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/BookSection" />
				</xsl:when>
				<xsl:when test="$genre = 'book review'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource" />
				</xsl:when>
				<xsl:when test="$genre = 'conference paper'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ConferencePaper" />
				</xsl:when>
				<xsl:when test="$genre = 'contribution to periodical'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource" />
				</xsl:when>
				<xsl:when test="$genre = 'doctoral thesis'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Thesis" />
				</xsl:when>
				<xsl:when test="$genre = 'lecture'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource" />
				</xsl:when>
				<xsl:when test="$genre = 'preprint'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource" />
				</xsl:when>
				<xsl:when test="$genre = 'report'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Report" />
				</xsl:when>
				<xsl:when test="$genre = 'research proposal'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearchProposal" />
				</xsl:when>
				<xsl:when test="$genre = 'technical documentation'">
					<rdf:type rdf:resource="http://purl.org/ontology/bibo/Manual" />
				</xsl:when>
				<xsl:when test="$genre = 'working paper'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#WorkingPaper" />
				</xsl:when>
				<xsl:when test="$genre = 'null'">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#InformationResource" />
				</xsl:when>
			</xsl:choose>
		</rdf:Description>
	</xsl:template>
</xsl:stylesheet>
