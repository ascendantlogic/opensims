<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_config_nmap.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to configure "symplugin-nmap"

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
 name="cmd_line_args"
/>
<xsl:param
 name="ip_addr_list"
/>
<xsl:param
 name="ref"
/>


<xsl:template
 match="AGENT"
>
<ENABLE_PLUGINS>
<PLUGIN
 name="symplugin-nmap"
>
<CONFIG>
<NMAP_ARGS>
<xsl:attribute
 name="args"
>
<xsl:value-of
 select="$cmd_line_args"
/>
<xsl:value-of
 select="$ip_addr_list"
/>
</xsl:attribute>

<xsl:attribute
 name="ref"
>
<xsl:value-of
 select="$ref"
/>
</xsl:attribute>

<xsl:attribute
 name="interval"
>
<xsl:value-of
 select="0"
/>
</xsl:attribute>
</NMAP_ARGS>
</CONFIG>
</PLUGIN>
</ENABLE_PLUGINS>
</xsl:template>

</xsl:stylesheet>
