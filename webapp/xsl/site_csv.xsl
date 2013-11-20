<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: site_csv.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate web pages for GUI, reports, manual, etc.

Paco NATHAN paco@symbiot.com
William HURLEY whurley@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="text"
 omit-xml-declaration="yes"
 indent="no"
/>
<xsl:strip-space
 elements="*"
/>


<xsl:template
 match="document"
>
<xsl:apply-templates
 select="body/section"
/>
</xsl:template>


<xsl:template
 match="section"
>
<xsl:text>"</xsl:text>
<xsl:value-of
 select="@name"
/>
<xsl:text>"</xsl:text>
<xsl:apply-templates
 select="row"
/>
</xsl:template>


<xsl:template
 match="row"
>
<xsl:text>
</xsl:text>

<xsl:apply-templates
 select="column|header"
/>
</xsl:template>


<xsl:template
 match="column|header"
>
<xsl:if
 test="position() &gt; 1"
>
<xsl:text>,</xsl:text>
</xsl:if>

<xsl:text>"</xsl:text>
<xsl:copy-of
 select="text()"
/>
<xsl:apply-templates
 select="*"
/>
<xsl:text>"</xsl:text>
</xsl:template>


<xsl:template
 match="*"
>
<xsl:copy-of
 select="text()"
/>
</xsl:template>


</xsl:stylesheet>
