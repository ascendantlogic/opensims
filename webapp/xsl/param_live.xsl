<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_live.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate XHTML for "Live" movie in Flash

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
 match="LIVE"
>
<html
 xmlns="http://www.w3.org/1999/xhtml"
 lang="en"
 xml:lang="en"
>
<head>
<meta
 http-equiv="Content-Type"
 content="text/html; charset=UTF-8"
/>
<link
 rel="icon"
 href="images/icon.png"
 type="image/png"
/>

</head>
<body>

<object
 id="index"
 classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000"
 codebase="https://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0"
 width="600"
 height="560"
 align="middle"
>

<param
 name="movie"
>
<xsl:attribute
 name="value"
>
<xsl:text>index.swf?view=</xsl:text>
<xsl:value-of
 select="@view"
/>
<xsl:text>,</xsl:text>
<xsl:value-of
 select="@instance"
/>
</xsl:attribute>
</param>

<param
 name="quality"
 value="high"
/>
<param
 name="bgcolor"
 value="#ffffff"
/>
<param
 name="allowScriptAccess"
 value="sameDomain"
/>

<embed
 name="index"
 type="application/x-shockwave-flash"
 pluginspage="http://www.macromedia.com/go/getflashplayer"
 width="600"
 height="560"
 align="middle"
 quality="high"
 bgcolor="#ffffff"
 allowScriptAccess="sameDomain"
>

<xsl:attribute
 name="src"
>
<xsl:text>index.swf?view=</xsl:text>
<xsl:value-of
 select="@view"
/>
<xsl:text>,</xsl:text>
<xsl:value-of
 select="@instance"
/>
</xsl:attribute>

</embed>
</object>

</body>
</html>
</xsl:template>


</xsl:stylesheet>
