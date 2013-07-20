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
		<xsl:variable name="ond_id" select="$this/db:ONDZ_ID" />
		<xsl:variable name="per_id" select="$this/db:PERS_ID" />
		<xsl:variable name="role" select="$this/db:ROLE" />

		<!-- Associate a role to the project -->
		<rdf:Description rdf:about="{$baseURI}individual/Project_{$ond_id}">
			<vivo:realizedRole
				rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
		</rdf:Description>

		<xsl:choose>
			<!-- Projectleider (Project leader) -->
			<xsl:when test="$role = 'PRL'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#LeaderRole" />
					<vivo:leaderRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasLeaderRole rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Contactpersoon (Contact person) -->
			<xsl:when test="$role = 'CON'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#OutreachProviderRole" />
					<vivo:outreachProviderRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasOutreachProviderRole
						rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Co-promotor (Co-supervisor) -->
			<xsl:when test="$role = 'COP'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#CoPrincipalInvestigatorRole" />
					<vivo:co-PrincipalInvestigatorRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasCo-PrincipalInvestigatorRole
						rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Onderzoeker (Researcher) -->
			<xsl:when test="$role = 'OND'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearcherRole" />
					<vivo:researcherRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasResearcherRole
						rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Promotor (Supervisor) -->
			<xsl:when test="$role = 'PRM'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#PrincipalInvestigatorRole" />
					<vivo:principalInvestigatorRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasPrincipalInvestigatorRole
						rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
			<!-- Promovendus (Doctoral/PhD student) -->
			<xsl:when test="$role = 'PRV'">
				<!-- Describe the role -->
				<rdf:Description
					rdf:about="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}">
					<rdf:type rdf:resource="http://vivoweb.org/ontology/core#ResearcherRole" />
					<vivo:researcherRoleOf rdf:resource="{$baseURI}individual/Person_{$per_id}" />
				</rdf:Description>
				<!-- add inverse relations -->
				<rdf:Description rdf:about="{$baseURI}individual/Person_{$per_id}">
					<vivo:hasResearcherRole
						rdf:resource="{$baseURI}individual/RoleFor_{$per_id}_in_{$ond_id}" />
				</rdf:Description>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>
