<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: revise_log.xsl 1 2008-01-10 18:37:05Z smoot $
XSL transform to generate updater scripts

Paco NATHAN paco@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="xml"
 omit-xml-declaration="no"
 indent="yes"
/>
<xsl:strip-space
 elements="*"
/>


<xsl:param
 name="file"
/>
<xsl:param
 name="status"
/>
<xsl:param
 name="since"
/>


<xsl:template
 match="UPDATE"
>
<xsl:copy>
<xsl:copy-of
 select="@*|text()|comment()"
/>

<xsl:if
 test="@file = $file"
>
<xsl:attribute
 name="status"
>
<xsl:value-of
 select="$status"
/>
</xsl:attribute>

<xsl:attribute
 name="since"
>
<xsl:value-of
 select="$since"
/>
</xsl:attribute>
</xsl:if>

<xsl:apply-templates
/>
</xsl:copy>
</xsl:template>


<xsl:template
 match="*"
>
<xsl:copy>
<xsl:copy-of
 select="@*|text()|comment()"
/>
<xsl:apply-templates
/>
</xsl:copy>
</xsl:template>


</xsl:stylesheet>
