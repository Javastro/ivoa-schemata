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
        <title>Service: <xsl:value-of select="contract:service/contract:title"/></title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="contract:service">
    <h1>Service: <xsl:value-of select="contract:title"/></h1>
    <p><xsl:value-of select="contract:type"/></p>
    <p>
      <xsl:value-of select="contract:description"/>
    </p>
    <h2>Semantics</h2>
    <p>
      See the <a href="semantics.html">semantic specification</a>.
    </p>
    <h2>XML schemata</h2>
    <dl>
      <xsl:for-each select="contract:schema">
        <dt><a>
          <xsl:attribute name="href">../schema/<xsl:value-of select="contract:package"/>/<xsl:value-of select="contract:version"/>/<xsl:value-of select="contract:file"/></xsl:attribute>
          <xsl:value-of select="contract:namespace"/>
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
