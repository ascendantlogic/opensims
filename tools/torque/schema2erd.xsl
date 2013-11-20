<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : schema2erd.xsl
    Created on : 7. September 2004, 13:12
    Author     : Patrick CARL patrick.carl@coi.de

Converts from Torque XML schema to Mogwai ERDesigner XDM graph.
See also:

    http://www.mail-archive.com/torque-user@db.apache.org/msg03283.html
    http://mogwai.sourceforge.net/erdesigner/erdesigner.html

-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html"/>

    <xsl:template match="database">
        <ERDesignerModel version="0.9.2">
            
            <Entities>
                <xsl:apply-templates select="table"/>
            </Entities>
            <Relations>
                <xsl:for-each select="table/foreign-key">
                    <xsl:call-template name="f-key"/>
                </xsl:for-each>
            </Relations>
        </ERDesignerModel>
    </xsl:template>

    
    <xsl:template match="table">
        <Entity>
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="comment"><xsl:value-of select="@description"/></xsl:attribute>
            <xsl:apply-templates/>
        </Entity>
    </xsl:template>
    
    <xsl:template match="column">
        <Attribute>
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="isrequired"><xsl:value-of select="@required"/></xsl:attribute>
            <xsl:attribute name="isprimarykey"><xsl:value-of select="@primaryKey"/></xsl:attribute>
            <xsl:attribute name="defaultvalue"><xsl:value-of select="@default"/></xsl:attribute>
            <xsl:attribute name="comment"><xsl:value-of select="@description"/></xsl:attribute>
        </Attribute>
    </xsl:template>
    
    <xsl:template name="f-key">
        <Relation>
            <xsl:attribute name="type">non-identifying</xsl:attribute>
            <xsl:attribute name="delete-rule">DATABASE_DEFAULT</xsl:attribute>
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="primary"><xsl:value-of select="@foreignTable"/></xsl:attribute>
            <xsl:attribute name="secondary"><xsl:value-of select="parent::table/@name"/></xsl:attribute>
            <Mapping>
                <xsl:attribute name="primary"><xsl:value-of select="reference/@foreign"/></xsl:attribute>
                <xsl:attribute name="secondary"><xsl:value-of select="reference/@local"/></xsl:attribute>                
            </Mapping>
        </Relation>
    </xsl:template>
</xsl:stylesheet>
