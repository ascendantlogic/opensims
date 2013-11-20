<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: gen_build.xsl 1 2008-01-10 18:37:05Z smoot $
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
 name="ifconf_properties"
/>


<xsl:template
 match="HOST"
>
<project
 name="prompt"
 default="prompt_intf_name"
 basedir="."
>

<target
 name="prompt_domain_name"
 description="prompt for the fully qualified domain name"
 unless="domain.name"
>
<input
 addproperty="domain.name"
>
<xsl:attribute
 name="defaultvalue"
>
<xsl:value-of
 select="@name"
/>
</xsl:attribute>
<xsl:text>What is the Fully Qualified Domain Name (FQDN)?  [ default: '</xsl:text>
<xsl:value-of
 select="@name"
/>
<xsl:text>' ]</xsl:text>
</input>
</target>

<target
 name="prompt_intf_name"
 depends="prompt_domain_name"
 description="prompt for the interface name"
 unless="selected.interface"
>
<xsl:apply-templates/>
</target>

</project>
</xsl:template>


<xsl:template
 match="SELECT"
>
<input
 addproperty="selected.interface"
>
<xsl:copy-of
 select="@*"
/>
<xsl:copy-of
 select="text()"
/>
</input>

<propertyfile
 comment="OpenSIMS dns/interface/network settings"
>
<xsl:attribute
 name="file"
>
<xsl:value-of
 select="$ifconf_properties"
/>
</xsl:attribute>

<entry
 key="domain.name"
 type="string"
>
<xsl:attribute
 name="value"
>
<xsl:text>${domain.name}</xsl:text>
</xsl:attribute>
</entry>

<entry
 key="selected.interface"
 type="string"
>
<xsl:attribute
 name="value"
>
<xsl:text>${selected.interface}</xsl:text>
</xsl:attribute>
</entry>

</propertyfile>
</xsl:template>


</xsl:stylesheet>
