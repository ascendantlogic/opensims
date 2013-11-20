<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: stream_ping.xsl 1 2008-01-10 18:37:05Z smoot $
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
 match="PING"
>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@tick"
/>
<xsl:apply-templates/>
</xsl:template>


<xsl:template
 match="STATUS"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@reboot"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@model"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@error"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@message"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@url"
/>
</xsl:template>


</xsl:stylesheet>
