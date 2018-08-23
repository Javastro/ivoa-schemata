<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<!-- Match each element in the tree in turn.
		   Copy the element and invoke templates to handle first its attributes
		   and second its children. (XSLT is too warped to consider attributes as
		   children, so they have to be matched separately.) -->
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates select="child::node()"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Copy any attribute to the output.
		   This is overridden in special cases, as below. -->
	<xsl:template match="@*">
		<xsl:copy/>
	</xsl:template>
	
	<!-- For the special case of the schemaLocation attribute starting
		   with the string "../", invoke a template to make the value into 
		   an absolute URL. -->
	<xsl:template match="@schemaLocation[starts-with(.,'../')]">
		<xsl:call-template name="strip-level">
			<xsl:with-param name="location" select="."/>
		</xsl:call-template>
	</xsl:template>
	
	<!-- Given a relative URL starting with "../", strip that prefix and
		   check again, by recursion, whether the result also starts with "../".
		   Given any other URL, write out the URL preceded by the root
		   http://software.astrogrid.org/schema/, thus making the URL absolute. -->
	<xsl:template name="strip-level">
		<xsl:param name="location"/>
		<xsl:choose>
			<xsl:when test="contains($location,'../')">
				<xsl:call-template name="strip-level">
					<xsl:with-param name="location" select="substring-after($location,'../')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:attribute name="schemaLocation"><xsl:value-of select="concat('http://software.astrogrid.org/schema/',$location)"/></xsl:attribute>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
