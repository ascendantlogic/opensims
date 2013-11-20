<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: local_openssl.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - authentication key manager (openssl)

Mike ERWIN mikee@symbiot.com
Lindsey SIMON lsimon@symbiot.com
Paco NATHAN paco@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="text"
 omit-xml-declaration="yes"
 indent="no"
/>


<xsl:param
 name="cnf_template"
/>
<xsl:param
 name="build_tag"
/>
<xsl:param
 name="unit_guid"
/>
<xsl:param
 name="domain_name"
/>
<xsl:param
 name="dname_country_name"
/>
<xsl:param
 name="dname_province_name"
/>
<xsl:param
 name="dname_locality_name"
/>
<xsl:param
 name="dname_org_name"
/>
<xsl:param
 name="dname_email_addr"
/>


<xsl:template
 match="/"
>
<xsl:value-of
 select="$cnf_template"
/>
<xsl:text>

[ req_distinguished_name ]

C = </xsl:text>
<xsl:value-of
 select="$dname_country_name"
/>
<xsl:text>
ST = </xsl:text>
<xsl:value-of
 select="$dname_province_name"
/>
<xsl:text>
L = </xsl:text>
<xsl:value-of
 select="$dname_locality_name"
/>
<xsl:text>
O = </xsl:text>
<xsl:value-of
 select="$dname_org_name"
/>
<xsl:text>
OU = </xsl:text>
<xsl:value-of
 select="$build_tag"
/>
<xsl:text> </xsl:text>
<xsl:value-of
 select="$unit_guid"
/>
<xsl:text>
emailAddress = </xsl:text>
<xsl:value-of
 select="$dname_email_addr"
/>
<xsl:text>
CN = </xsl:text>
<xsl:value-of
 select="$domain_name"
/>
<xsl:text>
</xsl:text>
</xsl:template>


</xsl:stylesheet>
