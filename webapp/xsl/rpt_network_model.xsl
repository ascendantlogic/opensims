<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_network_model.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Network Model Report

Paco NATHAN paco@symbiot.com
Lindsey SIMON lsimon@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="xml"
 omit-xml-declaration="no"
 indent="no"
/>
<xsl:strip-space
 elements="*"
/>


<xsl:template
 match="REPORT"
>
<document>
<properties>
<title>
Network Model
</title>
</properties>

<body>

<xsl:apply-templates
 select="MODEL/NETWORK"
/>

</body>
</document>
</xsl:template>


<xsl:template
 match="NETWORK"
>
<section>
<xsl:attribute
 name="name"
>
<xsl:text>Network: </xsl:text>
<xsl:value-of
 select="@cidr"
/>
</xsl:attribute>

<row>
<header
 align="left"
>scanner:</header>

<column>
<a
 href="http://www.insecure.org/nmap/"
 target="_blank"
>
NMAP 
<xsl:value-of
 select="@nmap"
/>
</a>
</column>
</row>

<xsl:apply-templates
 select="SCAN"
/>

<xsl:apply-templates
 select="WHITELIST"
/>
</section>

<section>
<xsl:attribute
 name="name"
>
<xsl:text>Hosts: </xsl:text>
<xsl:value-of
 select="@cidr"
/>
</xsl:attribute>

<xsl:apply-templates
 select="HOST"
/>
</section>
</xsl:template>


<xsl:template
 match="SCAN"
>
<row>
<header
 align="left"
>scan enabled:</header>

<column>
<xsl:value-of
 select="@enabled"
/>
</column>
</row>

<row>
<header
 align="left"
>permissive:</header>

<column>
<xsl:value-of
 select="@permissive"
/>
</column>
</row>

<row>
<header
 align="left"
>state machine:</header>

<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>uml/</xsl:text>
<xsl:value-of
 select="@fsm"
/>
</xsl:attribute>
<xsl:value-of
 select="@fsm"
/>
</a>
</column>
</row>
</xsl:template>


<xsl:template
 match="WHITELIST"
>
<xsl:if
 test="@enabled = 'true'"
>
<row>
<header
 align="left"
>whitelisted:</header>

<column>
<xsl:value-of
 select="@cidr"
/>
</column>
</row>
</xsl:if>
</xsl:template>


<xsl:template
 match="HOST"
>
<xsl:if
 test="position() = 1"
>
<row>
<header>IP address</header>
<header>DNS Name</header>
<header>Platform</header>
<header>Status</header>
<header>Services</header>
</row>
</xsl:if>

<row>
<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=defender_profile&amp;ip=</xsl:text>
<xsl:value-of
 select="@ip_addr"
/>
</xsl:attribute>
<xsl:value-of
 select="@ip_addr"
/>
</a>
</column>

<column
 align="left"
>
<xsl:value-of
 select="@fqdn"
/>
</column>

<column>
<xsl:apply-templates
 select="PLATFORM"
/>
</column>

<column
 align="center"
>
<xsl:value-of
 select="@status"
/>
</column>

<column
 align="right"
>
<xsl:value-of
 select="count(SERVICE)"
/>
</column>
</row>
</xsl:template>


<xsl:template
 match="PLATFORM"
>
<a
 target="_blank"
>
<xsl:attribute
 name="href"
>
<xsl:text>http://www.google.com/search?q=define%3A</xsl:text>
<xsl:value-of
 select="@osfamily"
/>
</xsl:attribute>
<xsl:attribute
 name="title"
>
<xsl:value-of
 select="@osfamily"
/>
</xsl:attribute>

<img
 border="0"
>
<xsl:attribute
 name="src"
>
<xsl:text>images/os/</xsl:text>
<xsl:value-of
 select="@osfamily"
/>
<xsl:text>.jpg</xsl:text>
</xsl:attribute>

<xsl:attribute
 name="alt"
>
<xsl:value-of
 select="@osfamily"
/>
</xsl:attribute>
</img>
</a>

<xsl:text> </xsl:text>

<xsl:choose>
<xsl:when
 test="@description"
>
<xsl:value-of
 select="@description"
/>
</xsl:when>

<xsl:otherwise>
<xsl:value-of
 select="@vendor"
/>
<xsl:text> - </xsl:text>
<xsl:value-of
 select="@osfamily"
/>
<xsl:text> </xsl:text>
<xsl:value-of
 select="@osgen"
/>
<xsl:text> - </xsl:text>
<xsl:value-of
 select="@type"
/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>


</xsl:stylesheet>
