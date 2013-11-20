<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_alert_type.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Alert Type Report

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
Alert Type:
<xsl:value-of
 select="ALERT_TYPE/@name"
/>
</title>
</properties>

<body>
<section>
<xsl:attribute
 name="name"
>
<xsl:text>Alert Type: </xsl:text>
<xsl:value-of
 select="ALERT_TYPE/@name"
/>
</xsl:attribute>

<xsl:apply-templates
 select="ALERT_TYPE"
/>

</section>

<section
 name="Most Frequent Attacks"
>
<blockquote>
<cite>

<xsl:text>
Most frequent attacks matching this alert type.
</xsl:text>

</cite>
</blockquote>

<xsl:apply-templates
 select="ALERT_DEF"
/>
</section>

</body>
</document>
</xsl:template>


<xsl:template
 match="ALERT_TYPE"
>
<row>
<header
 align="left"
>color:</header>

<column>
<xsl:value-of
 select="@color"
/>
</column>
</row>

<row>
<header
 align="left"
>relative frequency:</header>

<column>
<xsl:value-of
 select="@freq"
/>
<xsl:text>%</xsl:text>
</column>
</row>

<xsl:apply-templates
 select="REF"
/>
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
 match="ALERT_DEF"
>
<xsl:if
 test="position() = 1"
>
<row>
<header>ID</header>
<header>description</header>
<header>percentage</header>
<header>incident count</header>
<header>most recent</header>
</row>
</xsl:if>

<row>
<column>
<xsl:value-of
 select="@unique_id"
/>
</column>

<column>
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
 select="@msg"
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
