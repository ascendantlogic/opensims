<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_merge.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to merge agent plugin runtime configuration documents

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
 name="config_file"
/>


<xsl:variable
 name="avail_doc"
 select="/"
/>
<xsl:variable
 name="avail_doc_plugin_list"
 select="$avail_doc/AVAIL_PLUGINS/PLUGIN/@name"
/>
<xsl:variable
 name="config_doc"
 select="document($config_file)"
/>


<xsl:template
 match="AVAIL_PLUGINS"
>
<xsl:copy>
<xsl:copy-of
 select="@*"
/>
<xsl:apply-templates
/>
</xsl:copy>
</xsl:template>


<xsl:template
 match="PLUGIN"
>
<xsl:variable
 name="name"
 select="@name"
/>
<xsl:variable
 name="enabled"
 select="$config_doc//PLUGIN[@name = $name]/@enabled"
/>

<xsl:copy>
<xsl:choose>
<xsl:when
 test="$enabled = 'true'"
>
<xsl:attribute
 name="enabled"
>
<xsl:text>true</xsl:text>
</xsl:attribute>
</xsl:when>

<xsl:otherwise>
<xsl:attribute
 name="enabled"
>
<xsl:text>false</xsl:text>
</xsl:attribute>
</xsl:otherwise>
</xsl:choose>

<xsl:copy-of
 select="@*"
/>
<xsl:copy-of
 select="$config_doc//PLUGIN[@name = $name]/@class"
/>
<xsl:copy-of
 select="$config_doc//PLUGIN[@name = $name]/@threads"
/>
<xsl:copy-of
 select="*"
/>

<CONFIG>
<xsl:copy-of
 select="$config_doc//PLUGIN[@name = $name]/*"
/>
</CONFIG>
</xsl:copy>
</xsl:template>


</xsl:stylesheet>
