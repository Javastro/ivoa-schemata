/*
 * Copyright (C) AstroGrid. All rights reserved.
 *
 * This software is published under the terms of the AstroGrid 
 * Software License version 1.2, a copy of which has been included 
 * with this distribution in the LICENSE.txt file.  
 *
 */
package org.javastro.ivoa.schema;

import static org.javastro.ivoa.schema.Namespaces.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * static class that provides maps of all namespace - schema locations in this project.
 * The schema are all local copies in the project.
 */
public class SchemaMap {

    
    /** logger for this class */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(SchemaMap.class);
    
    /** Construct a new SchemaMap
     * 
     */
    private SchemaMap() {
    }
    
    /** ALL the map of all the namespaces to locations of the schema on the classpath.
     * 
     */
    public static final Map<String, String> ALL; 
    static final Map<String, URL> ALLUrl ;
    public static final String xmlCatalogue;
    
    /**
     * Static area for placing all namespaces.
     */
    static {
        // populate the map.
        ALL = new HashMap<String, String>();
        
        //registry schemas
        ALL.put("http://www.ivoa.net/xml/RegistryInterface/v1.0","/schema/RegistryInterface-v1.0.xsd");
        
        //oai schemas
        ALL.put("http://www.openarchives.org/OAI/2.0/","/schema/OAI-PMH.xsd");
        ALL.put("http://www.openarchives.org/OAI/2.0/oai_dc/","/schema/oai_dc.xsd");
        ALL.put("http://purl.org/dc/elements/1.1/","/schema/simpledc20021212.xsd");

        
        //stc schemas (go with adql imports usually)
        
        ALL.put("http://www.ivoa.net/xml/STC/stc-v1.30.xsd","/schema/STC-v1.30.xsd");
        ALL.put("http://www.ivoa.net/xml/STC/stc-v1.20.xsd","/schema/STC-v1.20.xsd");
        
        ALL.put("http://www.ivoa.net/xml/STC/STCcoords/v1.20","/schema/STC-coords-v1.20.xsd");
        ALL.put("http://www.ivoa.net/xml/STC/STCregion/v1.20","/schema/STC-region-v1.20.xsd");
        
        //votable schemas
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.0","/schema/VOTable-1.0.xsd");
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.1","/schema/VOTable-1.1.xsd");
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.2","/schema/VOTable-1.2.xsd");
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.3","/schema/VOTable-1.4.xsd");

        //vo-resource-types
        ALL.put("http://www.ivoa.net/xml/ConeSearch/v1.0","/schema/ConeSearch-v1.0.xsd");
        
        ALL.put("http://www.ivoa.net/xml/SSA/v1.1","/schema/SSA-v1.2.xsd");
                
        ALL.put("http://www.ivoa.net/xml/SIA/v1.0","/schema/SIA-v1.0.xsd");        
        ALL.put("http://www.ivoa.net/xml/SIA/v1.1","/schema/SIA-v1.1.xsd");        
        ALL.put("http://www.ivoa.net/xml/SkyNode/v0.2","/schema/OpenSkyNode-v0.2.xsd");      
        ALL.put("http://www.ivoa.net/xml/VODataService/v1.0","/schema/VODataService-v1.0.xsd");        
        ALL.put("http://www.ivoa.net/xml/VODataService/v1.1","/schema/VODataService-v1.2.xsd");        
       
      	ALL.put("http://www.ivoa.net/xml/VORegistry/v1.0","/schema/VORegistry-v1.0.xsd");        	
       
        ALL.put("http://www.ivoa.net/xml/VOResource/v1.0","/schema/VOResource-v1.1.xsd");
        
      	ALL.put("http://www.ivoa.net/xml/VOApplication/v1.0rc1","/schema/VOApplication-1.0.xsd");
        ALL.put("http://www.ivoa.net/xml/StandardsRegExt/v1.0","/schema/StandardsRegExt-1.0.xsd");

        
        ALL.put("http://www.ivoa.net/xml/TAPRegExt/v1.0","/schema/TAPRegExt-v1.0.xsd");
        
        ALL.put("http://www.ivoa.net/xml/SLAP/v1.0","/schema/SLAP-v1.1.xsd");

        //VOSI
        ALL.put("http://www.ivoa.net/xml/VOSIAvailability/v1.0", "/schema/VOSIAvailability-v1.0.xsd");
        ALL.put("http://www.ivoa.net/xml/VOSICapabilities/v1.0", "/schema/VOSICapabilities-v1.0.xsd");
        ALL.put("http://www.ivoa.net/xml/VOSITables/v1.0", "/schema/VOSITables-v1.1.xsd");
        
        ALL.put("http://www.ivoa.net/xml/UWS/v1.0", "/schema/UWS-1.1.xsd");

        ALL.put("http://www.ivoa.net/xml/VODML/v1", "/schema/vo-dml-v1.0.xsd");
        
        
        ALLUrl = ALL.entrySet().stream().
                collect(Collectors.toMap(Entry::getKey, v -> nameToURL(v)
                        ));
        xmlCatalogue = makeCatalogue();
        
        
    }
    //IMPL small helper function to make the collectors.toMap understand the typing.   
    private static URL nameToURL(Entry<String,String> e) {
        return  SchemaMap.class.getResource( e.getValue());
    }
    
    static String makeCatalogue() {
       StringWriter writer = new StringWriter();
       
       writer.write("<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n");
       ALL.forEach((k,v) -> {
           writer.append("<uri name=\"");
           writer.append(k);
           writer.append("\" uri=\"classpath:");
           writer.append(v);
           writer.append("\"/>\n");
           });
       
       writer.write("</catalog>");
       
       
       return writer.toString();
    }
    
    public static URL getSchemaURL(String namespace)
    {
       return ALLUrl.get(namespace);
    }
    
    /**
     * return an inputstream for the namespace
     * @param namespace - the namespace idendifier for the schema
     * @return
     */
    public static InputStream getSchemaAsStream(String namespace) {
        return SchemaMap.class.getResourceAsStream(ALL.get(namespace));
    }
    
    
    
    /**
     * Get the sources of schema used in the registry.
     * 
     * This is necessary as the way that the schema validators work is that they need an initial schema to 
     * validate against - it is not worked out dynamically by reading the namespace declarations unfortunately
     * (probably because they can appear anywhere in the instance - so in practice two runs through the document are neccessary
     * to be able to capture this information and then to do the actual validation)
     * 
     * The only way that the parsers do tend to do the "dynamic" schema loading is if there is a xsi:schemaLocation
     * declaration at the top.
     * 
     * 
     * @return
     * 
     */
    public static StreamSource[] getRegistrySchemaAsSources()
    {
        List<Namespaces> ns = Arrays.asList(OAI_PMH, OAI_DC, DC, RI, VR, VS, SIA,CS,REG, VA, VSTD, VOSI_TAB, STC);
        return schemaSourceFromNamespaces(ns);
    }
    
    public static StreamSource[] getAllSchemaAsSources() {
        return schemaSourceFromNamespaces(Namespaces.getAllIVOA());
    }

    private static StreamSource[] schemaSourceFromNamespaces(
            List<Namespaces> ns) {
        return ns.stream().map(n -> SchemaMap.getSchemaURL(n.getNamespace()))
                   .map(u -> {
                    try {
                        return new StreamSource(u.openStream(),u.toExternalForm());
                    } catch (IOException e) {
                        logger.error("problem with getting url for schema", e);
                        return new StreamSource();//IMPL is this OK? hopefully should not happen
                    }
                }).toArray(StreamSource[]::new);
    }

    
     
    
    public static InputSource asXMLCatalogue()
    {
        return new InputSource(new StringReader(xmlCatalogue));
    }
    
  
}


