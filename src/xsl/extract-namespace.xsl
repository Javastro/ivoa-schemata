<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/">
  <xsl:output method="html"/>
      
  <!-- The URL of the file transformed, relative to the index page. -->
  <xsl:param name="filename"/>

  <!-- Do not echo text or attribute nodes. -->
  <xsl:template match="text()|@*"/>

  <!-- Write out the namespaces as HTML links to the files being transformed. -->
  <xsl:template match="xsd:schema[@targetNamespace]|wsdl:definitions[@targetNamespace]">
      <xsl:element name="li">
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="$filename"/></xsl:attribute>
          <xsl:value-of select="@targetNamespace"/>
        </xsl:element>
      </xsl:element>
  </xsl:template>

</xsl:stylesheet>
