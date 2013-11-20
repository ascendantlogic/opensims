<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: fixor_host.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - interface configuration (ifconfig)

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
 name="domain_name"
/>
<xsl:param
 name="selected_interface"
/>


<xsl:template
 match="HOST"
>
<HOST>
<xsl:copy-of
 select="@*"
/>

<xsl:attribute
 name="name"
>
<xsl:value-of
 select="$domain_name"
/>
</xsl:attribute>

<xsl:copy-of
 select="SELECT"
/>

<xsl:apply-templates
 select="INTERFACE"
/>
</HOST>
</xsl:template>


<xsl:template
 match="INTERFACE"
>
<INTERFACE>
<xsl:copy-of
 select="@*"
/>

<xsl:if
 test="@name = $selected_interface"
>
<xsl:attribute
 name="selected"
>
<xsl:text>true</xsl:text>
</xsl:attribute>
</xsl:if>

<xsl:copy-of
 select="NETWORK"
/>
</INTERFACE>
</xsl:template>

</xsl:stylesheet>
