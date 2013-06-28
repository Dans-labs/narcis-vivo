<xsl:stylesheet version="2.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'
	xmlns:foaf='http://xmlns.com/foaf/0.1/' xmlns:vivo='http://vivoweb.org/ontology/core#'
	xmlns:vivonl='http://dans.knaw.nl/ontology/vivonl#' xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'
	xmlns:dcterms='http://purl.org/dc/terms/' xmlns:vitro="http://vitro.mannlib.cornell.edu/ns/vitro/0.7#"
	xmlns:db='jdbc:h2:/tmp/harvester/store/fields/CSV/' xmlns:vitro-public="http://vitro.mannlib.cornell.edu/ns/vitro/public#"
	xmlns:bibo='http://purl.org/ontology/bibo/' xmlns:md5="java.security.MessageDigest"
	xmlns:bigint="java.math.BigInteger" exclude-result-prefixes="md5 bigint">

	<xsl:output method="xml" indent="yes" />
	<xsl:variable name="baseURI" select="'http://example.org/'" />

	<xsl:template match="rdf:RDF">
		<rdf:RDF>
			<xsl:apply-templates select="rdf:Description" />
		</rdf:RDF>
	</xsl:template>

	<xsl:template match="rdf:Description">
		<xsl:variable name="this" select="." />
		<xsl:variable name="ond_id" select="$this/db:ONDZ_ID" />
		<xsl:variable name="org_id" select="$this/db:ORG_ID" />
		<xsl:variable name="role" select="$this/db:ROLE" />

		<!-- Associate a role to the project -->
		<rdf:Description rdf:about="{$baseURI}individual/Project_{$ond_id}">
			<vivo:realizedRole
				rdf:resource="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}" />
		</rdf:Description>

		<xsl:choose>
			<!-- Penvoerder -->
			<xsl:when test="$role = 'PEN'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#OutreachProviderRole" />
					<vivo:outreachProviderRoleOf
						rdf:resource="{$baseURI}individual/Organization_{$org_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Organization_{$org_id}">
					<vivo:hasOutreachProviderRole
						rdf:resource="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Samenwerking -->
			<xsl:when test="$role = 'SAM'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#MemberRole" />
					<vivo:memberRoleOf rdf:resource="{$baseURI}individual/Organization_{$org_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Organization_{$org_id}">
					<vivo:hasMemberRole
						rdf:resource="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Opdrachtgever -->
			<xsl:when test="$role = 'OPD'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#OrganizerRole" />
					<vivo:organizerRoleOf rdf:resource="{$baseURI}individual/Organization_{$org_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Organization_{$org_id}">
					<vivo:hasOrganizerRole
						rdf:resource="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Financier -->
			<xsl:when test="$role = 'FIN'">
				<!-- this connection should be established through the grant -->
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$org_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#Role" />
				</rdf:Description>
				<!-- add inverse relations -->
			</xsl:when>
		</xsl:choose>

	</xsl:template>
</xsl:stylesheet>
