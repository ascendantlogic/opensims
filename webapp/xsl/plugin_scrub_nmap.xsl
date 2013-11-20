<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: plugin_scrub_nmap.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform of raw Nmap XML output for autodiscovery

Paco NATHAN paco@symbiot.com
Dan CAMPER dan@bti.net

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
 match = "/NMAP_OUTPUT"
>
<NETWORK>
<xsl:attribute
 name="nmap"
>
<xsl:value-of
 select="nmaprun/@version"
/>
</xsl:attribute>

<xsl:copy-of
 select="@*"
/>

<xsl:apply-templates
 select="nmaprun/host"
/>
</NETWORK>
</xsl:template>


<xsl:template
 match = "host"
>
<HOST>
<xsl:attribute
 name="ip_addr"
>
<xsl:value-of
 select="address/@addr"
/>
</xsl:attribute>

<xsl:attribute
 name="status"
>
<xsl:value-of
 select="status/@state"
/>
</xsl:attribute>

<xsl:if
 test="smurf/@responses"
>
<xsl:attribute
 name="smurf"
>
<xsl:value-of
 select="smurf/@responses"
/>
</xsl:attribute>
</xsl:if>

<xsl:copy-of
 select="address/@name"
/>

<xsl:apply-templates
 select="uptime"
/>

<xsl:apply-templates
 select="os/osclass"
>
<xsl:sort
 select="@accuracy"
 data-type="number"
 order="descending"
/>
</xsl:apply-templates>

<xsl:apply-templates
 select="ports/port"
/>

</HOST>
</xsl:template>


<xsl:template
 match = "uptime"
>
<UPTIME>
<xsl:attribute
 name="tick"
>
<xsl:copy-of
 select="(/NMAP_OUTPUT/nmaprun/@start - @seconds) * 1000"
/>
</xsl:attribute>

<xsl:copy-of
 select="@lastboot"
/>
</UPTIME>
</xsl:template>


<xsl:template
 match = "osclass"
>
<xsl:if
 test="position() = 1"
>
<PLATFORM>
<xsl:copy-of
 select="@*"
/>
<xsl:if
 test="../osmatch/@name"
>
<xsl:attribute
 name="description"
>
<xsl:value-of
 select="../osmatch/@name"
/>
</xsl:attribute>
</xsl:if>
</PLATFORM>
</xsl:if>
</xsl:template>


<xsl:template
 match = "port"
>
<SERVICE>

<xsl:copy-of
 select="@protocol"
/>

<xsl:attribute
 name="port"
>
<xsl:value-of
 select="@portid"
/>
</xsl:attribute>

<xsl:copy-of
 select="state/@*"
/>

<xsl:copy-of
 select="service/@*"
/>

</SERVICE>
</xsl:template>


</xsl:stylesheet>
