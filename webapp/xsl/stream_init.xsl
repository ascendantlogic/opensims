<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: stream_init.xsl 1 2008-01-10 18:37:05Z smoot $
XSL transform from subscriber XML to what Flash expects

Paco NATHAN paco@symbiot.com
Lindsey SIMON lsimon@symbiot.com

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
 match="INIT"
>
<xsl:value-of
 select="name()"
/>

<xsl:apply-templates/>
</xsl:template>


<xsl:template
 match="VULN_TYPE"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@color"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@label"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@description"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@color_anim"
/>
</xsl:template>


<xsl:template
 match="ALERT_TYPE"
>
<xsl:text>|THREAT_TYPE&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;0x</xsl:text>
<xsl:value-of
 select="@color"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@label"
/>
</xsl:template>

</xsl:stylesheet>
