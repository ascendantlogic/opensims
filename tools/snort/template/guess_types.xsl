<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: guess_types.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to generate heuristics for guessing alert types

Paco NATHAN paco@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="text"
 omit-xml-declaration="yes"
 indent="no"
/>


<xsl:template
 match="ALERT_TYPES"
>
<xsl:apply-templates
 select="ALERT_TYPE/SNORT_CLASS"
/>
</xsl:template>


<xsl:template
 match="SNORT_CLASS"
>
<xsl:value-of
 select="../@name"
/>
<xsl:text>,</xsl:text>
<xsl:value-of
 select="@field"
/>
<xsl:text>,</xsl:text>
<xsl:value-of
 select="@pattern"
/>
<xsl:text>
</xsl:text>
</xsl:template>


</xsl:stylesheet>
