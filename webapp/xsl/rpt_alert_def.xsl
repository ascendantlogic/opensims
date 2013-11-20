<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_alert_def.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Alert Def Report

Paco NATHAN paco@symbiot.com
Jim NASBY jnasby@symbiot.com
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
Alert Definition:
<xsl:value-of
 select="ALERT_DEF/@msg"
/>
</title>
</properties>

<body>
<section>
<xsl:attribute
 name="name"
>
<xsl:text>Alert Definition: </xsl:text>
<xsl:value-of
 select="ALERT_DEF/@msg"
/>
</xsl:attribute>

<xsl:apply-templates
 select="ALERT_DEF"
/>

<xsl:apply-templates
 select="ALERT_DEF/REF"
/>
</section>

<section
 name="Most Frequent Attackers"
>
<blockquote>
<cite>

<xsl:text>
Most frequent attackers matching this alert definition.
</xsl:text>

</cite>
</blockquote>

<xsl:apply-templates
 select="ATK"
/>
</section>

</body>
</document>
</xsl:template>


<xsl:template
 match="ALERT_DEF"
>
<row>
<header
 align="left"
>ID:</header>

<column>
<xsl:value-of
 select="@unique_id"
/>
</column>
</row>

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
>category:</header>

<column>
<xsl:value-of
 select="@class"
/>
</column>
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


<xsl:template
 match="ATK"
>
<xsl:if
 test="position() = 1"
>
<row>
<header>IP address</header>
<header>source</header>
<header>percentage</header>
<header>incident count</header>
<header>most recent</header>
</row>
</xsl:if>

<row>
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
<!-- requires only the iso3166 country code -->
<xsl:attribute
 name="href"
>
<xsl:text>report?name=country&amp;cc=</xsl:text>
<xsl:value-of
 select="@cc"
/>
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
</a>
</column>

<column
 align="right"
>
<xsl:value-of
 select="format-number(@count div ../@total, '00.000%')"
/>
</column>

<column
 align="right"
>
<xsl:value-of
 select="@count"
/>
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
