<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: render_subant.xsl 1 2008-01-10 18:37:05Z smoot $
XSL transform to generate updater scripts

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


<xsl:param
 name="basedir"
/>


<xsl:template
 match="PRODUCT"
>
<project
 name="subant"
 default="downloads"
>
<xsl:attribute
 name="basedir"
>
<xsl:value-of
 select="$basedir"
/>
</xsl:attribute>

<target
 name="downloads"
>
<xsl:if
 test="UPDATE[@reboot = 'true']"
>
<ant
 target="stop_services"
/>
</xsl:if>

<xsl:apply-templates
 select="UPDATE[@status = 'pending']"
/>

<xsl:if
 test="UPDATE[@reboot = 'true']"
>
<ant
 target="reboot"
/>
</xsl:if>

</target>
</project>
</xsl:template>


<xsl:template
 match="UPDATE"
>
<ant
 target="run_updates"
>
<property
 name="tarball"
>
<xsl:attribute
 name="value"
>
<xsl:value-of
 select="@file"
/>
</xsl:attribute>
</property>

<property
 name="md5.server"
>
<xsl:attribute
 name="value"
>
<xsl:value-of
 select="@md5"
/>
</xsl:attribute>
</property>

<property
 name="description"
>
<xsl:attribute
 name="value"
>
<xsl:value-of
 select="@description"
/>
</xsl:attribute>
</property>

</ant>
</xsl:template>


</xsl:stylesheet>
