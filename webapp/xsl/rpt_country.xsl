<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: rpt_country.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Incident Report

Paco NATHAN paco@symbiot.com
Lindsey SIMON lsimon@symbiot.com

@LICENSE@
 -->
	<xsl:output method="xml" omit-xml-declaration="no" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="REPORT">
		<document>
			<properties>
				<title> Country Report: 
					<xsl:value-of select="COUNTRY/@name" />
				</title> 
			</properties>
		<body>
			<section>
				<xsl:attribute name="name">
					<xsl:text>
						Country Report: 
					</xsl:text>
					<xsl:value-of select="COUNTRY/@name" />
				</xsl:attribute>
				<blockquote>
					<cite> 
						<xsl:text>
							Source addresses ranked by rate of attacks, originating within 
						</xsl:text>
						<xsl:apply-templates select="COUNTRY" />
					</cite> 
				</blockquote>
			</section>
			<section name="Top Attackers">
				<xsl:apply-templates select="ATK" />
			</section>
			</body>
		</document>
	</xsl:template>
	<xsl:template match="COUNTRY">
		<a target="_blank"> 
			<xsl:attribute name="href">
				<xsl:value-of select="@factbook" />
			</xsl:attribute>
			<xsl:attribute name="title">
				<xsl:value-of select="@name" />
			</xsl:attribute>
			<img border="0"> 
				<xsl:attribute name="src">
					<xsl:text>
						images/flags/
					</xsl:text>
					<xsl:value-of select="@flag" />
				</xsl:attribute>
				<xsl:attribute name="alt">
					<xsl:value-of select="@name" />
				</xsl:attribute>
			</img> 
			<xsl:text>
			</xsl:text>
			<xsl:value-of select="@name" />
		</a> 
		<xsl:text>
			(
		</xsl:text>
		<xsl:value-of select="@iso3166" />
		<xsl:text>
			)
		</xsl:text>
	</xsl:template>
	<xsl:template match="ATK">
		<xsl:if test="position() = 1">
			<row>
				<header>
					IP address
				</header>
				<header>
					percentage
				</header>
				<header>
					incident count
				</header>
				<header>
					most recent
				</header>
			</row>
		</xsl:if>
		<row>
			<column>
				<a> 
					<xsl:attribute name="href">
						<xsl:text>
							report?name=attacker_profile&amp;ip=
						</xsl:text>
						<xsl:value-of select="@ip" />
					</xsl:attribute>
					<xsl:value-of select="@ip" />
				</a> 
			</column>
			<column align="right">
				<xsl:value-of select="format-number(@count div ../@total, '00.000%')" />
			</column>
			<column align="right">
				<xsl:value-of select="@count" />
			</column>
			<column align="left">
				<xsl:value-of select="@recent" />
			</column>
		</row>
	</xsl:template>
</xsl:stylesheet>
