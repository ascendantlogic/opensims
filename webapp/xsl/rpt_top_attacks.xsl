<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: rpt_top_attacks.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Top Attacks Report

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
Top Attacks Report
</title>
</properties>

<body>
<section
 name="Top Attacks Report"
>

<blockquote>
<cite>

<xsl:text>
Most frequent attacks.
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
 match="ALERT_DEF"
>
<xsl:if
 test="position() = 1"
>
<row>
<header>ID</header>
<header>alert type</header>
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
