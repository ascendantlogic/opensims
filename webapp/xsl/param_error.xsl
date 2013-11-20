<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_error.xsl 1 2008-01-10 18:37:05Z smoot $

iSIMS - generate XML for explaining error codes

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
 match="ERROR"
>
<xsl:copy>
<xsl:copy-of
 select="@*|text()|node()"
/>

<xsl:choose>
<xsl:when
 test="@code = 0"
>
<HEADER>Socket Server Error</HEADER>
<MESSAGE>Unable to establish the connection to the socket server.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 1"
>
<HEADER>Socket Server Error</HEADER>
<MESSAGE>Never received a PING packet from the socket server.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 2"
>
<HEADER>Socket Server Error</HEADER>
<MESSAGE>There is too much lag between data packets with socket server. You might need to turn up XML_SOCKET_LAG_TIMEOUT in config.xml.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 3"
>
<HEADER>RSS Error</HEADER>
<MESSAGE>Error loading and reading RSS XML feed.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 4"
>
<HEADER>XML Error</HEADER>
<MESSAGE>Error loading and reading XML for popup menu.</MESSAGE>
</xsl:when>


<xsl:when
 test="@code = 5"
>
<HEADER>Socket Server Error</HEADER>
<MESSAGE>The Socket Server unexpectedly closed.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 6"
>
<HEADER>Socket Server Error</HEADER>
<MESSAGE>Not enough arguments to subscribe to a stream.</MESSAGE>
</xsl:when>


<xsl:when
 test="@code = 97"
>
<HEADER>Flash function error</HEADER>
<MESSAGE>Error loading external swf in ContentListener</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 98"
>
<HEADER>Flash function error</HEADER>
<MESSAGE>_global.newDraggableWindow function called without enough params for instanceName.</MESSAGE>
</xsl:when>

<xsl:when
 test="@code = 99"
>
<HEADER>Unknown Error</HEADER>
<MESSAGE>Unknown problem.</MESSAGE>
</xsl:when>

</xsl:choose>
</xsl:copy>
</xsl:template>


</xsl:stylesheet>
