<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_config_snort_log.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to configure "symplugin-snort-log"

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
 name="path"
/>


<xsl:template
 match="AGENT"
>
<ENABLE_PLUGINS>
<PLUGIN
 name="symplugin-snort-log"
>
<CONFIG>
<WATCH_FILE
>
<xsl:copy-of
 select="AVAIL_PLUGINS/PLUGIN[@name = 'symplugin-snort-log']/CONFIG/WATCH_FILE/@*"
/>

<xsl:attribute
 name="path"
>
<xsl:value-of
 select="$path"
/>
</xsl:attribute>

</WATCH_FILE>
</CONFIG>
</PLUGIN>
</ENABLE_PLUGINS>
</xsl:template>

</xsl:stylesheet>
