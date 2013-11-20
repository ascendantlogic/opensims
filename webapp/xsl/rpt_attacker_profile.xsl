<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_attacker_profile.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Attacker Profile Report

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
Attacker Profile: 
<xsl:value-of
 select="ATK/@ip"
/>
</title>
</properties>

<body>
<section>
<xsl:attribute
 name="name"
>
<xsl:text>Attacker Profile: </xsl:text>
<xsl:value-of
 select="ATK/@id"
/>
</xsl:attribute>

<xsl:apply-templates
 select="ATK"
/>
</section>

<section
 name="Most Recent Attacks"
>
<blockquote>
<cite>

<xsl:text>
Most recent attacks matching this attacker.
</xsl:text>

</cite>
</blockquote>

<xsl:apply-templates
 select="ATK/ALT"
/>
</section>

</body>
</document>
</xsl:template>


<xsl:template
 match="ATK"
>
<row>
<header
 align="left"
>IP address:</header>

<column>
<xsl:value-of
 select="@ip"
/>
</column>
</row>

<row>
<header
 align="left"
>DNS:</header>

<column>
<xsl:value-of
 select="@fqdn"
/>
</column>
</row>

<row>
<header
 align="left"
>GeoLocation:</header>

<column>
<a>
<xsl:choose>

<xsl:when
 test="COUNTRY"
>
<!-- use the 'fixed' iso3166 country info from Symbiot.NET -->
<xsl:attribute
 name="href"
>
<xsl:text>report?name=country&amp;cc=</xsl:text>
<xsl:value-of
 select="COUNTRY/@iso3166"
/>
</xsl:attribute>
<xsl:attribute
 name="title"
>
<xsl:value-of
 select="COUNTRY/@name"
/>
</xsl:attribute>

<img
 border="0"
>
<xsl:attribute
 name="src"
>
<xsl:text>images/flags/</xsl:text>
<xsl:value-of
 select="COUNTRY/@flag"
/>
</xsl:attribute>

<xsl:attribute
 name="alt"
>
<xsl:value-of
 select="COUNTRY/@name"
/>
</xsl:attribute>
</img>

<xsl:text> </xsl:text>
<xsl:value-of
 select="COUNTRY/@name"
/>
</xsl:when>

<xsl:otherwise>
<!-- use only the iso3166 country code from GeoLoc class lookup -->
<xsl:attribute
 name="href"
>
<xsl:text>report?name=country&amp;cc=</xsl:text>
<xsl:value-of
 select="@cc"
/>
<xsl:text>.html</xsl:text>
</xsl:attribute>
<xsl:attribute
 name="title"
>
<xsl:value-of
 select="@country"
/>
</xsl:attribute>

<img
 border="0"
>
<xsl:attribute
 name="src"
>
<xsl:text>images/flags/</xsl:text>
<xsl:value-of
 select="@cc"
/>
<xsl:text>.jpg</xsl:text>
</xsl:attribute>

<xsl:attribute
 name="alt"
>
<xsl:value-of
 select="@country"
/>
</xsl:attribute>
</img>

<xsl:text> </xsl:text>
<xsl:value-of
 select="@country"
/>
</xsl:otherwise>

</xsl:choose>
</a>
</column>
</row>
</xsl:template>


<xsl:template
 match="ALT"
>
<xsl:if
 test="position() = 1"
>
<row>
<header>IP address</header>
<header>port</header>
<header>protocol</header>
<header>alert source</header>
<header>description</header>
<header>most recent</header>
</row>
</xsl:if>

<row>
<column>
<a
 target="_blank"
>
<xsl:attribute
 name="href"
>
<xsl:text>http://www.google.com/search?q=define%3A</xsl:text>
<xsl:value-of
 select="DEF/@os"
/>
</xsl:attribute>
<xsl:attribute
 name="title"
>
<xsl:value-of
 select="DEF/@os"
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
 select="DEF/@os"
/>
<xsl:text>.jpg</xsl:text>
</xsl:attribute>

<xsl:attribute
 name="alt"
>
<xsl:value-of
 select="DEF/@os"
/>
</xsl:attribute>
</img>
</a>

<xsl:text> </xsl:text>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=defender_profile&amp;ip=</xsl:text>
<xsl:value-of
 select="@dstAddr"
/>
</xsl:attribute>
<xsl:value-of
 select="@dstAddr"
/>
</a>
</column>

<column
 align="right"
>
<xsl:value-of
 select="@dstPort"
/>
</column>

<column>
<xsl:value-of
 select="@protocol"
/>
</column>

<column>
<xsl:value-of
 select="@source"
/>
</column>

<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=incident&amp;alert_id=</xsl:text>
<xsl:value-of
 select="@id"
/>
</xsl:attribute>
<xsl:value-of
 select="@text"
/>
</a>
</column>

<column
 align="left"
>
<xsl:value-of
 select="@recent"
/>
</column>
</row>
</xsl:template>


</xsl:stylesheet>
