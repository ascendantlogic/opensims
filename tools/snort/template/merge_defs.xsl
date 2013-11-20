<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: merge_defs.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - XSL transform to merge alert definition files

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
 name="alert_types_template"
/>
<xsl:param
 name="prev_defs_file"
/>


<xsl:variable
 name="auto_defs_doc"
 select="/"
/>
<xsl:variable
 name="auto_defs_doc_id_list"
 select="$auto_defs_doc//ALERT_DEF/@unique_id"
/>
<xsl:variable
 name="alert_types_doc"
 select="document($alert_types_template)"
/>
<xsl:variable
 name="prev_defs_doc"
 select="document($prev_defs_file)"
/>


<xsl:template
 match="ALERT_DEFS"
>
<xsl:copy>
<xsl:copy-of
 select="@*"
/>
<xsl:copy-of
 select="$alert_types_doc//ALERT_TYPE"
/>
<xsl:copy-of
 select="$prev_defs_doc//ALERT_DEF[not(@unique_id = $auto_defs_doc_id_list)]"
/>
<xsl:apply-templates
/>
</xsl:copy>
</xsl:template>


<xsl:template
 match="ALERT_DEF"
>
<xsl:variable
 name="unique_id"
 select="@unique_id"
/>

<xsl:copy>
<xsl:copy-of
 select="@*|node()"
/>
</xsl:copy>
</xsl:template>


</xsl:stylesheet>
