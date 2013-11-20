<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet
 version="2.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<!--
$Id: param_popup.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate XML for configuring the Flash popup menus

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
 match="POPUP"
>
<xsl:if
 test="@context = 'ATK'"
>
<MENU
 context="ATK"
>
<LINK
 label="Attacker Profile"
 url="report?name=attacker_profile&amp;ip=$IP"
/>
<LINK
 label="   ... in new window"
 url="report?name=attacker_profile&amp;ip=$IP"
 newwin="1"
/>
<LINK
 label="Top Attackers"
 url="report?name=top_attackers"
/>
<LINK
 label="SamSpade"
 url="http://www.samspade.org/t/lookat?a=$IP"
/>
<LINK
 label="NetworkTools"
 url="http://network-tools.com/default.asp?prog=express&amp;host=$IP"
 />
<LINK
 label="SimpleLogic"
 url="http://www.simplelogic.com/cgi-bin/utils/netutils.pl?ping=on&amp;nslookup=on&amp;svr=&amp;traceroute=on&amp;user=&amp;at=@&amp;host=$IP"
/>
</MENU>
</xsl:if>

<xsl:if
 test="@context = 'DEF'"
>
<MENU
 context="DEF"
>
<LINK
 label="Defender Profile"
 url="report?name=defender_profile&amp;ip=$IP"
/>
<LINK
 label="   ... in new window"
 url="report?name=defender_profile&amp;ip=$IP"
 newwin="1"
/>
<LINK
 label="Network Model"
 url="report?name=network_model"
/>
<!--
   <LINK label="MRTG" url="/mrtg/IP=$IP" />
 -->
</MENU>
</xsl:if>

<xsl:if
 test="@context = 'ALT'"
>
<MENU
 context="ALT"
>
<LINK
 label="Incident Report"
 url="report?name=incident&amp;alert_id=$ID"
/>
<LINK
 label="   ... in new window"
 url="report?name=incident&amp;alert_id=$ID"
 newwin="1"
/>
<LINK
 label="Top Attacks"
 url="report?name=top_attacks"
/>
<!--
   <LINK label="Incident Report" func="SCREEN.drawIncidentReport" args="$ID,$SCREEN,300,330"/>
 -->
</MENU>
</xsl:if>
</xsl:template>


</xsl:stylesheet>
