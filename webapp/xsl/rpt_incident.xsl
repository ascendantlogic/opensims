<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_incident.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Incident Report

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
Incident Report: 
<xsl:value-of
 select="ALT/@id"
/>
</title>
</properties>

<body>
<section>
<xsl:attribute
 name="name"
>
<xsl:text>Incident Report: </xsl:text>
<xsl:value-of
 select="ALT/@id"
/>
</xsl:attribute>

<xsl:apply-templates
 select="ALT/SRC"
/>

<xsl:apply-templates
 select="ALT/DST"
/>

<xsl:apply-templates
 select="ALT/ALERT_DEF"
/>

<row>
<header
 align="left"
>most recent:</header>

<column>
<xsl:value-of
 select="ALT/@timestamp"
/>
</column>
</row>

</section>

</body>
</document>
</xsl:template>


<xsl:template
 match="SRC"
>
<row>
<header
 align="left"
>source:</header>

<xsl:apply-templates
 select="*"
/>
</row>

<row>
<header
 align="left"
>port:</header>

<column>
<xsl:value-of
 select="../@src_port"
/>
</column>
</row>
</xsl:template>


<xsl:template
 match="DST"
>
<row>
<header
 align="left"
>destination:</header>

<xsl:apply-templates
 select="*"
/>
</row>

<row>
<header
 align="left"
>port:</header>

<column>
<xsl:value-of
 select="../@dst_port"
/>
</column>
</row>

<row>
<header
 align="left"
>protocol:</header>

<column>
<xsl:value-of
 select="../@protocol"
/>
</column>
</row>
</xsl:template>


<xsl:template
 match="ATK"
>
<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=attacker_profile&amp;ip=</xsl:text>
<xsl:value-of
 select="@ip"
/>
</xsl:attribute>
<xsl:value-of
 select="@ip"
/>
</a>
</column>

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
</xsl:template>


<xsl:template
 match="DEF"
>
<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=defender_profile&amp;ip=</xsl:text>
<xsl:value-of
 select="@ip"
/>
</xsl:attribute>
<xsl:value-of
 select="@ip"
/>
</a>
</column>

<column>
<a
 target="_blank"
>
<xsl:attribute
 name="href"
>
<xsl:text>http://www.google.com/search?q=define%3A</xsl:text>
<xsl:value-of
 select="@os"
/>
</xsl:attribute>
<xsl:attribute
 name="title"
>
<xsl:value-of
 select="@os"
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
 select="@os"
/>
<xsl:text>.jpg</xsl:text>
</xsl:attribute>

<xsl:attribute
 name="alt"
>
<xsl:value-of
 select="@os"
/>
</xsl:attribute>
</img>

<xsl:text> </xsl:text>
<xsl:value-of
 select="@os"
/>
</a>
</column>
</xsl:template>


<xsl:template
 match="ALERT_DEF"
>
<row>
<header
 align="left"
>alert type:</header>

<column>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=alert_type&amp;type_name=</xsl:text>
<xsl:value-of
 select="@type"
/>
</xsl:attribute>
<xsl:value-of
 select="@type"
/>
</a>
</column>
</row>

<row>
<header
 align="left"
>definition:</header>

<column
 colspan="2"
>
<a>
<xsl:attribute
 name="href"
>
<xsl:text>report?name=alert_def&amp;unique_id=</xsl:text>
<xsl:value-of
 select="@unique_id"
/>
</xsl:attribute>

<xsl:value-of
 select="../@text"
/>
</a>
</column>

<xsl:apply-templates
 select="*"
/>
</row>
</xsl:template>


<xsl:template
 match="REF"
>
<row>
<header
 align="left"
>reference:</header>

<column>
<a
 target="_blank"
>
<xsl:attribute
 name="href"
>
<xsl:value-of
 select="@url"
/>
<xsl:value-of
 select="@path"
/>
</xsl:attribute>
<xsl:value-of
 select="@ref"
/>
</a>
</column>
</row>
</xsl:template>


</xsl:stylesheet>
