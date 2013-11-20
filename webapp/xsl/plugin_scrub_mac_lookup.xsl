<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_scrub_mac_lookup.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to parse input from "symplugin-mac-lookup"

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


<xsl:template
 match="MAC_LIST"
>
<NETWORK>
<xsl:attribute
 name="cidr"
>
<xsl:value-of
 select="@scan_target"
/>
</xsl:attribute>

<xsl:apply-templates
/>
</NETWORK>
</xsl:template>


<xsl:template
 match="ENTRY"
>
<xsl:if
 test="not(@mac_id = '{no_response}')"
>
<HOST>
<xsl:attribute
 name="mac_addr"
>
<xsl:value-of
 select="@mac_id"
/>
</xsl:attribute>

<xsl:attribute
 name="ip_addr"
>
<xsl:value-of
 select="@ip"
/>
</xsl:attribute>
</HOST>
</xsl:if>
</xsl:template>


</xsl:stylesheet>
