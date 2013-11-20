<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: rpt_defender_profile.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Defender Profile Report

@LICENSE@
-->
<xsl:output method="xml" omit-xml-declaration="no" indent="no" />
<xsl:strip-space elements="*" />
<xsl:template match="REPORT">
<document>
<properties>
<title> Defender Profile: 
<xsl:value-of select="DEF/@id" />
</title> 
</properties>
<body>
<section>
<xsl:attribute name="name">
<xsl:text>
Defender Profile: 
</xsl:text>
<xsl:value-of select="DEF/@id" />
</xsl:attribute>
<xsl:apply-templates select="DEF" />
</section>
<section name="Discovered Services">
<xsl:apply-templates select="DEF/HOST/SERVICE" />
</section>
</body>
</document>
</xsl:template>
<xsl:template match="DEF">
<row>
<header align="left">
IP address:
</header>
<column>
<xsl:value-of select="@ip" />
</column>
</row>
<row>
<header align="left">
DNS:
</header>
<column>
<xsl:value-of select="@fqdn" />
</column>
</row>
<xsl:apply-templates select="GROUP" />
<xsl:apply-templates select="HOST" />
</xsl:template>
<xsl:template match="HOST">
<row>
<header align="left">
MAC address:
</header>
<column>
<code> 
<xsl:value-of select="@mac_addr" />
</code> 
</column>
</row>
<row>
<header align="left">
Platform:
</header>
<column>
<xsl:apply-templates select="PLATFORM" />
</column>
</row>
<row>
<header align="left">
Status:
</header>
<column>
<xsl:value-of select="@status" />
</column>
</row>
<xsl:apply-templates select="UPTIME" />
</xsl:template>

<xsl:template match="GROUP">
<row>
<header align="left">
Groups:
</header>
<column>
<input type="checkbox"> 
<xsl:attribute name="name">
<xsl:text>group_</xsl:text>
<xsl:value-of select="@name" />
</xsl:attribute>
<xsl:if test="@enabled = 'true'">				
<xsl:attribute name="checked">true</xsl:attribute>
</xsl:if>
</input>
<xsl:value-of select="@name" />
</column>
</row>
</xsl:template>

<xsl:template match="PLATFORM">
<a target="_blank"> 
<xsl:attribute name="href">
<xsl:text>http://www.google.com/search?q=define%3A</xsl:text>
<xsl:value-of select="@osfamily" />
</xsl:attribute>
<xsl:attribute name="title">
<xsl:value-of select="@osfamily" />
</xsl:attribute>
<img border="0"> 
<xsl:attribute name="src">
<xsl:text>images/os/</xsl:text>
<xsl:value-of select="@osfamily" />
<xsl:text>.jpg</xsl:text>
</xsl:attribute>
<xsl:attribute name="alt">
<xsl:value-of select="@osfamily" />
</xsl:attribute>
</img> </a> 
<xsl:text>
</xsl:text>
<xsl:choose>
<xsl:when test="@description">
<xsl:value-of select="@description" />
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="@vendor" /><xsl:text> - </xsl:text><xsl:value-of select="@osfamily" /><xsl:text></xsl:text><xsl:value-of select="@osgen" /><xsl:text> - </xsl:text><xsl:value-of select="@type" />
</xsl:otherwise>
</xsl:choose>
</xsl:template>
<xsl:template match="UPTIME">
<row>
<header align="left">
Last boot:
</header>
<column>
<xsl:value-of select="@lastboot" />
</column>
</row>
</xsl:template>
<xsl:template match="SERVICE">
<xsl:if test="position() = 1">
<row>
<header>
name
</header>
<header>
port
</header>
<header>
protocol
</header>
<header>
status
</header>
<header>
application
</header>
<header>
version
</header>
<header>
info
</header>
</row>
</xsl:if>
<row>
<column>
<xsl:value-of select="@name" />
</column>
<column align="right">
<xsl:value-of select="@port" />
</column>
<column>
<xsl:value-of select="@protocol" />
</column>
<column>
<xsl:value-of select="@state" />
</column>
<column>
<a target="_blank"> 
<xsl:attribute name="href">
<xsl:text>http://www.google.com/search?q=</xsl:text>
<xsl:value-of select="@product" />
</xsl:attribute>
<xsl:value-of select="@product" />
</a> 
<br />
</column>
<column>
<xsl:value-of select="@version" />
<br />
</column>
<column>
<xsl:value-of select="@extrainfo" />
<br />
</column>
</row>
</xsl:template>
</xsl:stylesheet>
