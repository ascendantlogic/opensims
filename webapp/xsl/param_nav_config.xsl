<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_nav_config.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate site nav menu configuration

Lindsey SIMON lsimon@symbiot.com
Paco NATHAN paco@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="xml"
 omit-xml-declaration="no"
 indent="no"
/>
<xsl:strip-space
 elements="*"
/>


<xsl:template
 match="NAV_CONFIG"
>
<xsl:copy>
<xsl:copy-of
 select="@*"
/>

<menu>
<xsl:attribute
 name="name"
><TRANSLATE
 key="live"
/></xsl:attribute>

<item
 href="live?view=network"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="network"
/></xsl:attribute>
</item>

</menu>

<menu>
<xsl:attribute
 name="name"
><TRANSLATE
 key="reports"
/></xsl:attribute>

<item
 href="report?name=top_attacks"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="top_attacks"
/></xsl:attribute>
</item>

<item
 href="report?name=top_attackers"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="top_attackers"
/></xsl:attribute>
</item>

<item
 href="report?name=network_model"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="network_model"
/></xsl:attribute>
</item>

<item
 href="report?name=agent_operation"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="agent_operation"
/></xsl:attribute>
</item>
</menu>

<menu>
<xsl:attribute
 name="name"
><TRANSLATE
 key="support"
/></xsl:attribute>

<item
 href="support?name=contacts"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="contacts"
/></xsl:attribute>
</item>

<item
 href="support?name=docs"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="documentation"
/></xsl:attribute>
</item>

<item
 href="support?name=diagnostics"
>
<xsl:attribute
 name="name"
><TRANSLATE
 key="diagnostics"
/></xsl:attribute>
</item>
</menu>

</xsl:copy>
</xsl:template>


</xsl:stylesheet>
