<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: stream_viz.xsl 1 2008-01-10 18:37:05Z smoot $
XSL transform from subscriber XML to what Flash expects

Paco NATHAN paco@symbiot.com
Lindsey SIMON lsimon@symbiot.com

@LICENSE@
 -->

<xsl:output
 method="text"
 omit-xml-declaration="yes"
 indent="no"
/>
<xsl:strip-space
 elements="*"
/>


<xsl:template
 match="MODEL"
>
<xsl:text>VIZ</xsl:text>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@tick"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@revision"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@tick_cursor"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@tick_start"
/>
<xsl:apply-templates/>
</xsl:template>


<xsl:template
 match="NET"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ip"
/>
</xsl:template>


<xsl:template
 match="ATK"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ip"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@cc"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@country"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@warn"
/>
</xsl:template>


<xsl:template
 match="DEF"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ip"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@os"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@warn"
/>
</xsl:template>


<xsl:template
 match="CON"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@src"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@dst"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ids"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@alert"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@state"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@level"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@duplex"
/>
</xsl:template>


<xsl:template
 match="ALT"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@tick"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@id"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@source"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@count"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ids"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@src"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@srcAddr"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@srcPort"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@srcRole"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@dst"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@dstAddr"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@dstPort"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@dstRole"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@text"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ce"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@te"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ve"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@rm"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@ci"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@risk"
/>
</xsl:template>

<xsl:template
 match="METRIC"
>
<xsl:text>|</xsl:text>
<xsl:value-of
 select="name()"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@name"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@min"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@mean"
/>
<xsl:text>&amp;</xsl:text>
<xsl:value-of
 select="@max"
/>
</xsl:template>

</xsl:stylesheet>
