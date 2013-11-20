<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!--
$Id: site_html.xsl 1 2008-01-10 18:37:05Z smoot $

OpenSIMS - generate web pages for GUI, reports, manual, etc.

Paco NATHAN paco@symbiot.com
William HURLEY whurley@symbiot.com

@LICENSE@
 -->
	<xsl:output method="xml" omit-xml-declaration="no" indent="no" />
	<xsl:strip-space elements="*" />
	<xsl:template match="document">
		<xsl:comment>
			document
		</xsl:comment>
	<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
			<link rel="icon" href="images/icon.png" type="image/png" />
<style type="text/css">
<xsl:comment>
body,td,th {
	font-family: Verdana, Arial, Helvetica, sans-serif;
	font-size: 12px;
	color: #333333;
}
a:link {
	color: #0000CC;
}
a:visited {
	color: #0000CC;
}
a:hover {
	color: #FF6600;
}
a:active {
	color: #FF6600;
}
h1 {
	font-size: 16px;
}
h2 {
	font-size: 14px;
}
</xsl:comment>
</style> 
			<xsl:apply-templates select="properties" />
		</head>
		<xsl:apply-templates select="body" />
		</html>
	</xsl:template>
	<xsl:template match="properties">
		<xsl:comment>
			properties
		</xsl:comment>
		<xsl:if test="author">
			<meta name="author">
				<xsl:attribute name="value">
					<xsl:value-of select="author/text()" />
				</xsl:attribute>
			</meta>
			<meta name="email">
				<xsl:attribute name="value">
					<xsl:value-of select="author/@email" />
				</xsl:attribute>
			</meta>
		</xsl:if>
		<title> 
			<xsl:value-of select="title/text()" />
		</title> 
	</xsl:template>
	<xsl:template match="body">
		<xsl:comment>
			body
		</xsl:comment>
	<body>
		<table width="625" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td>
					<xsl:apply-templates select="../NAVIGATE" />
					<xsl:apply-templates select="section" />
				</td>
			</tr>
		</table>
		</body>
	</xsl:template>
	<xsl:template match="NAVIGATE">
		<form method="GET">
			<xsl:for-each select="HIDDEN">
				<input type="hidden"> 
					<xsl:attribute name="name">
						<xsl:value-of select="@name" />
					</xsl:attribute>
					<xsl:attribute name="value">
						<xsl:value-of select="@value" />
					</xsl:attribute>
				</input> 
			</xsl:for-each>
			<p>
				<center>
					<xsl:apply-templates select="MENU" />
					<input name="submit" type="submit" value="refresh" /> 
				</center>
			</p>
		</form>
		<hr width="40" />
	</xsl:template>
	<xsl:template match="MENU">
		<xsl:variable name="param_name" select="@name" />
		<xsl:if test="@prompt">
			<xsl:text>
			</xsl:text>
			<xsl:value-of select="@prompt" />
			<xsl:text>
			</xsl:text>
		</xsl:if>
		<select> 
			<xsl:attribute name="name">
				<xsl:value-of select="@name" />
			</xsl:attribute>
			<xsl:for-each select="OPTION">
				<option>
					<xsl:if test="@value = ../../PARAM[@name = $param_name]/@value">
						<xsl:attribute name="selected">
							<xsl:text>
								true
							</xsl:text>
						</xsl:attribute>
					</xsl:if>
					<xsl:copy-of select="@value|text()" />
				</option>
			</xsl:for-each>
		</select> 
	</xsl:template>
	<xsl:template match="section">
		<xsl:comment>
			section
		</xsl:comment>
		<a> 
			<xsl:attribute name="name">
				<xsl:value-of select="position()" />
			</xsl:attribute>
		</a> 
		<xsl:if test="position() &gt; 1">
			<hr width="40" />
		</xsl:if>
		<xsl:if test="@name">
			<h3> <font color="#0000CC"> 
					<xsl:value-of select="@name" />
				</font> </h3> 
		</xsl:if>
		<xsl:apply-templates select="*" />
		<xsl:if test="row">
			<table border="1" cellpadding="5" width="100%">
				<xsl:for-each select="row">
					<tr>
						<xsl:apply-templates select="header" />
						<xsl:apply-templates select="column" />
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
	</xsl:template>
	<xsl:template match="row">
<!-- ignore on first pass -->
	</xsl:template>
	<xsl:template match="header">
		<th>
			<xsl:copy-of select="*|@*|text()" />
		</th>
	</xsl:template>
	<xsl:template match="column">
		<td>
			<xsl:copy-of select="*|@*|text()" />
		</td>
	</xsl:template>
	<xsl:template match="*">
		<xsl:copy-of select="." />
	</xsl:template>
</xsl:stylesheet>
