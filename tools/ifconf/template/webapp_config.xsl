<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: webapp_config.xsl 1 2008-01-10 18:37:05Z smoot $
OpenSIMS - interface configuration (ifconfig)

Paco NATHAN paco@symbiot.com
Mike W. ERWIN mikee@symbiot.com

@LICENSE@
 -->
	<xsl:output method="xml" omit-xml-declaration="no" indent="yes" />
	<xsl:template match="HOST">
		<CONFIG>
			<IDENTITY name="OpenSIMS Network Visualization" email="info@opensims.org" org="OpenSIMS Development" locality="Austin" province="Texas" country="US">
				<PRODUCT name="OpenSIMS" description="OpenSIMS Security Visualization Installation">
					<UPDATE version="0.9.0" tick="1145134352461" class="org.opensims.SimsUpdater" />
				</PRODUCT>
			</IDENTITY>
			<xsl:comment>
				Platform-specific file layout settings
			</xsl:comment>
			<PARAM name="opensims.home" value="@OPENSIMS.HOME@" />
			<PARAM name="webapp.dir" value="@WEBAPP.DIR@" />
			<PARAM name="var.lib" value="@VAR.LIB@" />
			<PARAM name="temp.dir" value="@TEMP.DIR@" />
			<xsl:comment>
				Correlation Engine settings
			</xsl:comment>
			<CORRELATOR class="org.opensims.SimsCorrelator" name="@HOST.NAME@">
				<xsl:attribute name="name">
					<xsl:value-of select="/HOST/@name" />
				</xsl:attribute>
				<TASK method="taskCheckExpiry" delay="15000" period="15000" enabled="true" />
				<TASK method="taskDataVacuum" delay="0" period="60000" enabled="true" />
				<TASK method="taskPingLogger" delay="0" period="300000" enabled="true" />
			</CORRELATOR>
			<xsl:comment>
				Geo-location data source
			</xsl:comment>
			<GEOLOC class="org.opensims.geoloc.MaxMindGeoIP" data="@GEOIP.HOME@/GeoIP.dat" jar="@GEOIP.HOME@/geoip.jar" fixer="world.xml" />
			<PARAM name="data.age_limit" value="2592000000" />
			<xsl:comment>
				AgentManager definitions for agents/plugins
			</xsl:comment>
			<AGENT class="org.opensims.agent.GenericAgent" name="symagent" heartbeat="60" local="true" threads="5">
				<LOCALHOST localtime="US/Central" latitude="0.0" longitude="0.0" altitude="0.0"/>
				<INTERFACE name="@INTF.NAME@" role="sniffer" mac_addr="@MAC.ADDR@" ip_addr="@IP.ADDR@">
					<xsl:attribute name="name">
						<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/@name" />
					</xsl:attribute>
					<xsl:attribute name="mac_addr">
						<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/@mac_addr" />
					</xsl:attribute>
					<xsl:attribute name="ip_addr">
						<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/@ip_addr" />
					</xsl:attribute>
					<NETWORK cidr="@CIDR@" description="@HOST.NAME@" topology="@TOPOLOGY@">
						<xsl:attribute name="cidr">
							<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/NETWORK/@cidr" />
						</xsl:attribute>
						<xsl:attribute name="description">
							<xsl:value-of select="/HOST/@name" />
						</xsl:attribute>
						<xsl:attribute name="topology">
							<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/NETWORK/@topology" />
						</xsl:attribute>
						<SCAN class="org.opensims.model.GenericScan" fsm="autod_uml.xml" permissive="false" enabled="true" />
						<WHITELIST cidr="127.0.0.1/32" enabled="true" />
						<WHITELIST enabled="true">
							<xsl:attribute name="cidr">
								<xsl:value-of select="/HOST/INTERFACE[@selected = 'true']/@ip_addr" />
								<xsl:text>/32</xsl:text>
							</xsl:attribute>
						</WHITELIST>
					</NETWORK>
				</INTERFACE>
				<PLUGIN class="org.opensims.agent.TesterPlugin" name="symplugin-tester" enabled="false">
					<FOO value="bar" />
					<PARAM name="test_file" value="servlet_test_vector.xml" />
				</PLUGIN>
				<PLUGIN class="org.opensims.agent.SnortLogPlugin" name="symplugin-snort-log" enabled="true" threads="5">
					<WATCH_FILE path="@SNORT.LOG@" watch_interval="10" format="compact" />
				</PLUGIN>
				<PLUGIN class="org.opensims.agent.MacLookupPlugin" name="symplugin-mac-lookup" enabled="false">
					<SCAN device="@INTF.NAME@" target="192.168.0.1/24" />
					<PARAM name="arp_scan" value="true" />
				</PLUGIN>
				<PLUGIN class="org.opensims.agent.NmapPlugin" name="symplugin-nmap" enabled="false" />
				<PLUGIN class="org.opensims.agent.NetworkPlugin" name="symplugin-network" enabled="false">
					<INTERFACE device="@INTF.NAME@" transmit_interval="5" report="summary" />
					<PARAM name="pcap_args" value=" (tcp or udp or icmp) " />
				</PLUGIN>
				<PLUGIN class="org.opensims.agent.ProcessesPlugin" name="symplugin-processes" enabled="false">
					<TRANSMIT_INTERVAL value="15" />
				</PLUGIN>
				<PLUGIN class="org.opensims.agent.LogWatcherPlugin" name="symplugin-log-watcher" enabled="true">
					<WATCH_FILE path="@NAGIOS.LOG@" watch_interval="10" watch_style="contents">
						<PATTERN_LIST>
							<PATTERN ref="nagios" pattern="(OK)|(UP)" options="v" />
						</PATTERN_LIST>
					</WATCH_FILE>
					<WATCH_FILE path="/var/log/messages" watch_interval="5" watch_style="tail">
						<PATTERN_LIST>
							<PATTERN ref="pix_syslog" pattern="%PIX-" />
						</PATTERN_LIST>
					</WATCH_FILE>
				</PLUGIN>
			</AGENT>
			<xsl:comment>
				AgentManager settings for autodiscovery
			</xsl:comment>
			<PARAM name="symplugin-mac-lookup.arp_scan" value="" />
			<PARAM name="symplugin-nmap.fingerprint" value=" -M 200 -O -F -oX - " />
			<PARAM name="symplugin-nmap.ping" value=" -M 200 -sP -oX - " />
			<PARAM name="symplugin-nmap.port_scan" value=" -M 200 -sS -oX - " />
			<PARAM name="symplugin-nmap.services_probe" value=" -M 200 -sS -sV -oX - " />
			<PARAM name="symplugin-traffic.pcap_args" value=" (tcp or udp or icmp) " />
			<PARAM name="symplugin-tester.test_file" value="servlet_test_vector.xml" />
			<xsl:comment>
				SSL X.509 key management
			</xsl:comment>
			<PARAM name="auth.keystore_type" value="JKS" />
			<PARAM name="auth.algorithm" value="SunX509" />
			<PARAM name="auth.protocol" value="TLS" />
			<PARAM name="auth.truststore_file" value="@TRUSTSTORE@" />
			<PARAM name="auth.truststore_pass" value="@TRUSTSTORE.PASS@" />
			<PARAM name="auth.keystore_file" value="@KEYSTORE@" />
			<PARAM name="auth.keystore_pass" value="@KEYSTORE.PASS@" />
			<xsl:comment>
				Repository access and interaction
			</xsl:comment>
			<PARAM name="gpg.home" value="@GPG.HOME@" />
			<PARAM name="repository.email" value="@REPOSITORY.EMAIL@" />
			<PARAM name="repository.url" value="@REPOSITORY.URL@" />
			<PARAM name="repository.user" value="@REPOSITORY.USER@" />
			<PARAM name="repository.password" value="@REPOSITORY.PASSWORD@" />
			<xsl:comment>
				SubscriberManager settings for client/stream handling
			</xsl:comment>
			<LISTENER class="org.opensims.client.GenericListener" port="@SUBSCRIBER.PORT@">
				<SUBSCRIBER class="org.opensims.client.GenericSubscriber" />
				<STREAM class="org.opensims.stream.InitStream" name="init" transform="stream_init.xsl" />
				<STREAM class="org.opensims.stream.PingStream" name="ping" transform="stream_ping.xsl" />
				<STREAM class="org.opensims.stream.VizStream" name="viz" transform="stream_viz.xsl" />
			</LISTENER>
			<PARAM name="ping.period" value="10000" />
			<xsl:comment>
				NotifyManager settings for notification
			</xsl:comment>
			<NOTIFY class="org.opensims.notify.GenericNotify" name="jabber dev@conference.www.opensims.org" enabled="false" min_rm="600.0" min_ci="0.5">
				<xmpp host="www.opensims.org" port="5223" ssl="true" user="OpenSIMS" pass="qu3ldr0l3" room="dev@conference.www.opensims.org" />
			</NOTIFY>
			<NOTIFY class="org.opensims.notify.TelnetNotify" name="telnet listener" enabled="false" min_rm="300.0" min_ci="0.6">
				<telnet host="localhost" port="5000" />
			</NOTIFY>
			<NOTIFY class="org.opensims.notify.SmtpNotify" name="email nobody@opensims.org" enabled="false" min_rm="300.0" min_ci="0.6">
				<smtp host="mail.opensims.org" to="nobody@opensims.org" from="someone@opensims.org" subject="Ruh-roh" />
			</NOTIFY>
			<NOTIFY class="org.opensims.notify.HttpNotify" name="OpenSIMS NOC" enabled="false" min_rm="600.0" min_ci="0.5">
				<http url="https://www.opensims.org/opensims/ops" />
			</NOTIFY>
			<xsl:comment>
				This could become an XQuery thing, to encapsulate rules for notification SYSLOG host="localhost" port="5000" user="foo" BUGZILLA from="foo@x.com" category="alerts" WSDL host="example.com" port="5000" BREEZE user="foo" url="localhost:5000" ODDCAST server="localhost" 
			</xsl:comment>
			<xsl:comment>
				Reporting Configuration
			</xsl:comment>
			<REPORT class="org.opensims.report.IncidentReport" name="incident" transform="rpt_incident.xsl" enabled="true" />
			<REPORT class="org.opensims.report.AlertTypeReport" name="alert_type" transform="rpt_alert_type.xsl" enabled="true" />
			<REPORT class="org.opensims.report.AlertDefReport" name="alert_def" transform="rpt_alert_def.xsl" enabled="true" />
			<REPORT class="org.opensims.report.CountryReport" name="country" transform="rpt_country.xsl" enabled="true" />
			<REPORT class="org.opensims.report.AttackerProfileReport" name="attacker_profile" transform="rpt_attacker_profile.xsl" enabled="true" />
			<REPORT class="org.opensims.report.DefenderProfileReport" name="defender_profile" transform="rpt_defender_profile.xsl" enabled="true" />
			<REPORT class="org.opensims.report.TopAttackersReport" name="top_attackers" transform="rpt_top_attackers.xsl" enabled="true" />
			<REPORT class="org.opensims.report.TopAttacksReport" name="top_attacks" transform="rpt_top_attacks.xsl" enabled="true" />
			<REPORT class="org.opensims.report.NetworkModelReport" name="network_model" transform="rpt_network_model.xsl" enabled="true" />
			<REPORT class="org.opensims.report.AgentOperationReport" name="agent_operation" transform="rpt_agent_operation.xsl" enabled="true" />
		</CONFIG>
	</xsl:template>
</xsl:stylesheet>
