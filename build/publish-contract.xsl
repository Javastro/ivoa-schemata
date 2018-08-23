<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:contract="urn:astrogrid:schema:architecture:contract:v0.1">
  
  <xsl:output method="html"/>
  
  <!-- Generate the shell of the document. Use the matches of other templates
       to fill in the details of the schemata. -->
  <xsl:template match="/">
    <html>
      <head>
        <title>Contract: <xsl:value-of select="contract:contract/contract:title"/></title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="contract:contract">
    <h1>Contract: <xsl:value-of select="contract:title"/></h1>
    <dl>
      <xsl:for-each select="contract:coupling">
        <xsl:variable name="packagePath">
          <xsl:value-of select="translate(substring-after(contract:name,'urn:astrogrid:service:'),':','/')"/>
        </xsl:variable>
        <dt><a>
          <xsl:attribute name="href">../../../service/<xsl:value-of select="$packagePath"/>/index.html</xsl:attribute>
          <xsl:value-of select="contract:name"/>
        </a></dt>
        <dd><xsl:value-of select="contract:description"/></dd>
      </xsl:for-each>
    </dl>
    <h2>Java classes and interfaces</h2>
    <ul>
      <xsl:for-each select="contract:java"/>
    </ul>
    <h2>Conformance tests</h2>
    <ul>
    <xsl:for-each select="contract:test"/>
    </ul>
  </xsl:template>
  
</xsl:stylesheet>
