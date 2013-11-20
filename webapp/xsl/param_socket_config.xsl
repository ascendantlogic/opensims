<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_socket_config.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate XML for configuring XMLSocket movie in Flash

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
 match="SOCKET_CONFIG"
>
<xsl:copy>
<xsl:copy-of
 select="@*"
/>
       <!-- whether or not to log debug msgs to a textarea on screen -->
       <DEBUG value="false" type="Boolean" />
       <DEBUG_VERBOSE value="false" type="Boolean" />
       <DEBUG_X value="0" type="Number" />
       <DEBUG_Y value="100" type="Number" />
       <DEBUG_WIDTH value="150" type="Number" />
       <DEBUG_HEIGHT value="100" type="Number" />
      
       <!-- XMLSocket host, empty = localhost -->
       <SOCKET_HOST value="" type="String" />

       <!-- XMLSocket listener port -->
       <SOCKET_PORT value="8445" type="Number" />

       <!-- timeout rate for java socket listener to flash client -->
       <SOCKET_LAG_TIMEOUT value="15000" type="Number" />

       <STATUS_TITLE value="Status" type="String" />
       <PAUSED value="Paused" type="String" />
       <PLAYING value="Playing" type="String" />
       <LAST_PACKET_AT value="Last packet:" type="String" />
       <PACKET_RECEIVED value="packet received" type="String" />
       <WAITING_FOR_PING value="Waiting for PING ..." type="String" />
       <WAITING_FOR_PACKETS value="Waiting for packets ..." type="String" />
       <WAITING_FOR_CONNECTION value="Waiting for connection ..." type="String" />
       <CONNECTION_INITIALIZING value="Connection initializing." type="String" />
       <CONNECTION_SUCCESSFUL value="Connection success." type="String" />
       <SUBSCRIBING_TO value="Subscribing to" type="String" />
       <AGENT_STATUS value="Server Status:" type="String" />

       <!-- sets a done loading config var to beat race condition -->
       <!-- don't change this or you'll kill your browser -->
       <DONE_LOADING_CONFIG value="true" type="Boolean" />

</xsl:copy>
</xsl:template>


</xsl:stylesheet>
