<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_live_config.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate the XML config for "Live" movie in Flash

Lindsey SIMON lsimon@symbiot.com
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


<xsl:template
 match="LIVE_CONFIG"
>
<xsl:copy>
<xsl:copy-of
 select="@*"
/>

<!-- debug: whether or not to log msgs to a textarea on screen -->

<DEBUG
 value="false"
 type="Boolean"
/>
<DEBUG_VERBOSE
 value="false"
 type="Boolean"
/>
<DEBUG_X
 value="35"
 type="Number"
/>
<DEBUG_Y
 value="30"
 type="Number"
/>
<DEBUG_WIDTH
 value="400"
 type="Number"
/>
<DEBUG_HEIGHT
 value="400"
 type="Number"
/>

<!-- debug main drawing function exec timestamps -->
<TIMESTAMPS
 value="false"
 type="Boolean"
/>


<!-- main window -->

<SCREEN_TITLE
 value="OpenSIMS"
 type="String"
/>
<SCREEN_TITLE_LIVE
 value="Live"
 type="String"
/>
<SCREEN_MENU_NETWORK_TITLE
 value="Network"
 type="String"
/>
<SCREEN_MENU_REFRESH_TITLE
 value="Refresh"
 type="String"
/>
<SCREEN_MENU_THROTTLE_TITLE
 value="Throttle"
 type="String"
/>
<THROTTLE_MENU_OPTIONS
 value="5"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="15"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="50"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="150"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="500"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="1000"
 type="Array"
/>
<THROTTLE_MENU_OPTIONS
 value="3000"
 type="Array"
/>

<!-- sets swf container size -->
<SCREEN_WIDTH
 value="597"
 type="Number"
/>
<SCREEN_HEIGHT
 value="560"
 type="Number"
/>

<!-- defaults for new window popups -->
<NEW_WINDOW_WIDTH
 value="825"
 type="Number"
/>
<NEW_WINDOW_HEIGHT
 value="575"
 type="Number"
/>

<!-- XMLSocket host, empty = localhost -->
<XML_HOST
 value=""
 type="String"
/>

<!-- XMLSocket listener port -->
<XML_PORT
 value="8445"
 type="Number"
/>

<!-- timeout rate for java socket listener to flash client -->
<XML_SOCKET_LAG_TIMEOUT
 value="100000"
 type="Number"
/>

<!-- time between new data packets to flash, in milliseconds -->
<VIZ_REFRESH
 value="4000"
 type="Number"
/>

<!-- time to delay the first data packets, in milliseconds -->
<VIZ_DELAY
 value="500"
 type="Number"
/>

<!-- time that an ATK stays onscreen after activity, in milliseconds -->
<VIZ_ACTIVITY
 value="30000"
 type="Number"
/>

<!-- default throttle -->
<VIZ_THROTTLE
 value="15"
 type="Number"
/>

<!-- default viz stream -->
<VIZ_THREAT
 value="perimeter"
 type="String"
/>

<!-- default network_id, blank = default -->
<VIZ_NETWORK_ID
 value=""
 type="String"
/>

<!-- time to delay the first data packets, in milliseconds -->
<HIST_DELAY
 value="500"
 type="Number"
/> 

<!-- DEF internal threat ring radius -->
<DEF_RADIUS
 value="120"
 type="Number"
/>

<!-- SVC bars -->
<SVC_BAR_HEIGHT
 value="15"
 type="Number"
/>

<!-- packet dot animation and speed -->
<LINE_ANIM
 type="Boolean"
>
<xsl:attribute
 name="value"
>
<xsl:choose>
<xsl:when
 test="@view = 'network'"
>
<xsl:text>true</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>false</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</LINE_ANIM>

<LINE_ANIM_SPEED
 value="30"
 type="Number"
/>
<LINE_ANIM_INTERVAL
 value="500"
 type="Number"
/>

<!-- default,faded,starting alpha of screen objects -->
<ALPHA_DEFAULT
 value="100"
 type="Number"
/>
<ALPHA_FADED
 value="60"
 type="Number"
/>
<ALPHA
 value="100"
 type="Number"
/>


<!-- Network (Attacker/Defender) window -->

<ATKDEF_X
 type="Number"
>
<xsl:attribute
 name="value"
>
<xsl:choose>
<xsl:when
 test="@view = 'network'"
>
<xsl:text>1</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>0</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</ATKDEF_X>

<ATKDEF_Y
 value="1"
 type="Number"
/>
<ATKDEF_WIDTH
 value="596"
 type="Number"
/>
<ATKDEF_HEIGHT
 value="360"
 type="Number"
/>
<ATKDEF_TITLE
 value="Network"
 type="String"
/>
<ATKDEF_THREAT_MENU_TITLE
 value="Threat"
 type="String"
/>
<ATKDEF_THREAT_MENU_INTERNAL
 value="Internal"
 type="String"
/>
<ATKDEF_THREAT_MENU_PERIMETER
 value="Perimeter"
 type="String"
/>

<!-- threats icon spacing -->
<ATKDEF_ROLE_SPACING_PER
 value="18"
 type="Number"
/> 
<ATKDEF_ROLE_SPACING_IN
 value="18"
 type="Number"
/> 
<ATKDEF_ROLE_SPACING_ME
 value="70"
 type="Number"
/>

<!-- ATK/DEF onRollOver scale -->
<ATKDEF_ROLLOVER_SCALE
 value="120"
 type="Number"
/>


<!-- RSS window -->

<RSS_X
 type="Number"
>
<xsl:attribute
 name="value"
>
<xsl:choose>
<xsl:when
 test="@view = 'network'"
>
<xsl:text>1</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>0</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</RSS_X>

<RSS_Y
 value="363"
 type="Number"
/>
<RSS_WIDTH
 value="595"
 type="Number"
/>
<RSS_HEIGHT
 value="155"
 type="Number"
/>
<RSS_TITLE
 value="RSS Feed"
 type="String"
/>
<RSS_REFRESH_INTERVAL
 value="100000"
 type="Number"
/>
<RSS_URL
 value="https://symbiot.net/opensims.net/feed.xml?gui=1"
 type="Array"
/>


<!-- Security Alerts window -->

<ALT_X
 type="Number"
>
<xsl:attribute
 name="value"
>
<xsl:choose>
<xsl:when
 test="@view = 'network'"
>
<xsl:text>1</xsl:text>
</xsl:when>
<xsl:otherwise>
<xsl:text>0</xsl:text>
</xsl:otherwise>
</xsl:choose>
</xsl:attribute>
</ALT_X>

<ALT_Y
 value="288"
 type="Number"
/>
<ALT_WIDTH
 value="596"
 type="Number"
/>
<ALT_HEIGHT
 value="230"
 type="Number"
/>
<ALT_TITLE
 value="Security Alerts"
 type="String"
/>
<ALT_BAR_HEIGHT
 value="15"
 type="Number"
/>
<ALT_HEIGHT_BETWEEN_BARS
 value="1"
 type="Number"
/>

<!-- ALT text spacing -->
<ALT_TIME_X
 value="2"
 type="Number"
/>
<ALT_TIME_LABEL
 value="Time"
 type="String"
/>
<ALT_RISK_X
 value="50"
 type="Number"
/>
<ALT_RISK_LABEL
 value="Risk"
 type="String"
/>
<ALT_SOURCE_X
 value="90"
 type="Number"
/>
<ALT_SOURCE_LABEL
 value="Source"
 type="String"
/>
<ALT_SRCIP_X
 value="130"
 type="Number"
/>
<ALT_SRCIP_LABEL
 value="Source"
 type="String"
/>
<ALT_DSTIP_X
 value="220"
 type="Number"
/>
<ALT_DSTIP_LABEL
 value="Dest."
 type="String"
/>
<ALT_TEXT_X
 value="310"
 type="Number"
/>
<ALT_TEXT_LABEL
 value="Text"
 type="String"
/>
<ALT_TEXT_SIZE
 value="250"
 type="Number"
/>


<!-- sets a done loading config var to beat race condition -->
<!-- don't change this or you'll kill your browser -->

<DONE_LOADING_CONFIG
 value="true"
 type="Boolean"
/>

</xsl:copy>
</xsl:template>


</xsl:stylesheet>
