<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: rpt_agent_operation.xsl 1 2008-01-10 18:37:05Z smoot $
XSL stylesheet for Agent Operation

Paco NATHAN paco@symbiot.com
Lindsey SIMON lsimon@symbiot.com
Mike W. ERWIN mikee@symbiot.com

@LICENSE@
 -->
	<xsl:output method="xml" omit-xml-declaration="no" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="REPORT">
		<document>
			<properties>
				<title> Agent Operation </title> 
			</properties>
		<body>
			<xsl:apply-templates select="PROVISION/AGENT" />
			</body>
		</document>
	</xsl:template>
	<xsl:template match="AGENT">
		<section>
			<xsl:attribute name="name">
				<xsl:text>Agent: </xsl:text>
				<xsl:value-of select="@name" />
			</xsl:attribute>
			<row>
				<header align="left">
					class:
				</header>
				<column>
					<a target="_blank"> 
						<xsl:attribute name="href">
							<xsl:text>http://opensims.org/docs/javadoc/</xsl:text>
							<xsl:value-of select="translate(@class,'.','/')" />
						</xsl:attribute>
						<xsl:value-of select="@class" />
					</a> 
				</column>
			</row>
			<row>
				<header align="left">
					local:
				</header>
				<column>
					<xsl:value-of select="@local" />
				</column>
			</row>
			<row>
				<header align="left">
					heartbeat:
				</header>
				<column>
					<xsl:value-of select="@last_beat" />
					/ 
					<xsl:value-of select="@heartbeat * 1000" />
					ms 
				</column>
			</row>
			<xsl:apply-templates select="INTERFACE[@role = 'sniffer']" />
			<row>
				<header align="left">
					scan state:
				</header>
				<column>
					<xsl:value-of select="@scan_state" />
				</column>
			</row>
		</section>
		<xsl:apply-templates select="AVAIL_PLUGINS/PLUGIN" />
	</xsl:template>
	<xsl:template match="INTERFACE">
		<row>
			<header align="left">
				interface:
			</header>
			<column>
				<code> 
					<xsl:value-of select="@name" />
				</code> 
			</column>
		</row>
		<row>
			<header align="left">
				IP address:
			</header>
			<column>
				<a> 
					<xsl:attribute name="href">
						<xsl:text>report?name=defender_profile&amp;ip=</xsl:text>
						<xsl:value-of select="@ip_addr" />
					</xsl:attribute>
					<xsl:value-of select="@ip_addr" />
				</a> 
			</column>
		</row>
		<row>
			<header align="left">
				MAC address:
			</header>
			<column>
				<code> 
					<xsl:value-of select="@mac_addr" />
				</code> 
			</column>
		</row>
		<row>
			<header align="left">
				network:
			</header>
			<column>
				<a> 
					<xsl:attribute name="href">
						<xsl:text>report?name=network_model&amp;cidr=</xsl:text>
						<xsl:value-of select="NETWORK/@cidr" />
					</xsl:attribute>
					<xsl:value-of select="NETWORK/@cidr" />
				</a> 
			</column>
		</row>
	</xsl:template>
	<xsl:template match="PLUGIN">
		<section>
			<xsl:attribute name="name">
				<xsl:text>Plugin: </xsl:text>
				<xsl:value-of select="@name" />
			</xsl:attribute>
			<row>
				<header align="left">
					class:
				</header>
				<column>
					<a target="_blank"> 
						<xsl:attribute name="href">
							<xsl:text>http://opensims.org/docs/javadoc/</xsl:text>
							<xsl:value-of select="translate(@class,'.','/')" />
						</xsl:attribute>
						<xsl:value-of select="@class" />
					</a> 
				</column>
			</row>
			<row>
				<header align="left">
					version:
				</header>
				<column>
					<xsl:value-of select="@version" />
				</column>
			</row>
			<row>
				<header align="left">
					signature:
				</header>
				<column>
					<code> 
						<xsl:value-of select="@signature" />
					</code> 
				</column>
			</row>
			<row>
				<header align="left">
					static enable:
				</header>
				<column>
					<xsl:value-of select="@enabled" />
				</column>
			</row>
		</section>
	</xsl:template>
</xsl:stylesheet>
