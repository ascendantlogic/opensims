<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_config_mac_lookup.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to configure "symplugin-mac-lookup"

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
 name="device"
/>
<xsl:param
 name="target"
/>

<xsl:template
 match="AGENT"
>
<ENABLE_PLUGINS>
<PLUGIN
 name="symplugin-mac-lookup"
>
<CONFIG>
<SCAN>
<xsl:attribute
 name="device"
>
<xsl:value-of
 select="$device"
/>
</xsl:attribute>

<xsl:attribute
 name="target"
>
<xsl:value-of
 select="$target"
/>
</xsl:attribute>

<xsl:attribute
 name="interval"
>
<xsl:value-of
 select="0"
/>
</xsl:attribute>
</SCAN>
</CONFIG>
</PLUGIN>
</ENABLE_PLUGINS>
</xsl:template>

</xsl:stylesheet>
