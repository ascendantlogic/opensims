<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: rpt_top_attackers.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Top Attackers Report

Paco NATHAN paco@symbiot.com
Jim NASBY jnasby@symbiot.com
Lindsey SIMON lsimon@symbiot.com

@LICENSE@
 -->
	<xsl:output method="xml" omit-xml-declaration="no" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="REPORT">
		<document>
			<properties>
				<title> Top Attackers Report </title> 
			</properties>
		<body>
			<section name="Top Attackers Report">
				<blockquote>
					<cite> 
						<xsl:text>
							Most frequent attackers. 
						</xsl:text>
					</cite> 
				</blockquote>
				<row>
					<header>
						IP address
					</header>
					<header>
						source
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
				<xsl:for-each select="*">
					<row>
						<xsl:apply-templates select="." />
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
				</xsl:for-each>
			</section>
			</body>
		</document>
	</xsl:template>
	<xsl:template match="ATK">
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
		<column>
			<a> 
<!-- use only the iso3166 country code from GeoLoc class lookup -->
				<xsl:attribute name="href">
					<xsl:text>
						report?name=country&amp;cc=
					</xsl:text>
					<xsl:value-of select="@cc" />
					<!--
					<xsl:text>
						.html
					</xsl:text>
					-->
				</xsl:attribute>
				<xsl:attribute name="title">
					<xsl:value-of select="@country" />
				</xsl:attribute>
				<img border="0"> 
					<xsl:attribute name="src">
						<xsl:text>
							images/flags/
						</xsl:text>
						<xsl:value-of select="@cc" />
						<xsl:text>
							.jpg
						</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="alt">
						<xsl:value-of select="@country" />
					</xsl:attribute>
				</img> 
				<xsl:text>
				</xsl:text>
				<xsl:value-of select="@country" />
			</a> 
		</column>
	</xsl:template>
	<xsl:template match="DEF">
		<column>
			<a> 
				<xsl:attribute name="href">
					<xsl:text>
						report?name=defender_profile&amp;ip=
					</xsl:text>
					<xsl:value-of select="@ip" />
				</xsl:attribute>
				<xsl:value-of select="@ip" />
			</a> 
		</column>
		<column>
			<a target="_blank"> 
				<xsl:attribute name="href">
					<xsl:text>
						http://www.google.com/search?q=define%3A
					</xsl:text>
					<xsl:value-of select="@os" />
				</xsl:attribute>
				<xsl:attribute name="title">
					<xsl:value-of select="@os" />
				</xsl:attribute>
				<img border="0"> 
					<xsl:attribute name="src">
						<xsl:text>
							images/os/
						</xsl:text>
						<xsl:value-of select="@os" />
						<xsl:text>
							.jpg
						</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="alt">
						<xsl:value-of select="@os" />
					</xsl:attribute>
				</img> 
				<xsl:text>
				</xsl:text>
				<xsl:value-of select="@os" />
			</a> 
		</column>
	</xsl:template>
</xsl:stylesheet>
