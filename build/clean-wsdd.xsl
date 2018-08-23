<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:strip-space elements="*"/>
<xsl:output method="xml" indent="yes"/>

<xsl:template match="/" >
    <xsl:apply-templates />
</xsl:template>


<!-- copy all input to output.
-->
<xsl:template match="node()">
        <!-- adds link to wsdl file. not quite right - will leave for now
    <xsl:choose>
        <xsl:when test="name() = 'service' ">
          <xsl:copy>
            <xsl:copy-of select="@*" />
                <wsdlFile>/wsdl/<xsl:value-of select="node()/@name" />.wsdl</wsdlFile>
                <xsl:apply-templates select="@* | node()" />
          </xsl:copy>
        </xsl:when>
    <xsl:otherwise>
    -->
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
      <!-- </xsl:otherwise>
    </xsl:choose>
    -->
</xsl:template>

<!--
bugfix for axis tool - sometimes drops spare &gt; around the place
 - filter these out.
-->
<xsl:template match="@*">
        <xsl:attribute name="{name(.)}"><xsl:value-of select="translate(.,'&gt;','')" /></xsl:attribute>
</xsl:template>



</xsl:stylesheet>
