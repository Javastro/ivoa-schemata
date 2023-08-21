/*
 * Copyright (C) AstroGrid. All rights reserved.
 *
 * This software is published under the terms of the AstroGrid 
 * Software License version 1.2, a copy of which has been included 
 * with this distribution in the LICENSE.txt file.  
 *
 */
package org.javastro.ivoa.schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.ls.LSInput;

import static org.javastro.ivoa.schema.Namespaces.*;

/**
 * static class that provides maps of all namespace - schema locations in this project.
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
    
    public static final Map<String, URL> ALL; 
    
    /**
     * Static area for placing all namespaces.
     */
    static {
        // populate the map.
        ALL = new HashMap<String, URL>();
        
        //registry schemas
        ALL.put("http://www.ivoa.net/xml/RegistryInterface/v1.0",SchemaMap.class.getResource("/schema/RegistryInterface-v1.0.xsd"));
        
        //oai schemas
        ALL.put("http://www.openarchives.org/OAI/2.0/",SchemaMap.class.getResource("/schema/OAI-PMH.xsd"));
        
        
        //stc schemas (go with adql imports usually)
        
        ALL.put("http://www.w3.org/1999/xlink",SchemaMap.class.getResource("/schema/xlink.xsd"));        	
        ALL.put("http://www.ivoa.net/xml/STC/stc-v1.30.xsd",SchemaMap.class.getResource("/schema/STC-v1.30.xsd"));        	
        ALL.put("http://www.ivoa.net/xml/STC/stc-v1.20.xsd",SchemaMap.class.getResource("/schema/STC-v1.20.xsd"));
        
        ALL.put("http://www.ivoa.net/xml/STC/STCcoords/v1.20",SchemaMap.class.getResource("/schema/STC-coords-v1.20.xsd"));
        ALL.put("http://www.ivoa.net/xml/STC/STCregion/v1.20",SchemaMap.class.getResource("/schema/STC-region-v1.20.xsd"));
        
        //votable schemas
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.0",SchemaMap.class.getResource("/schema/VOTable-1.0.xsd"));
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.1",SchemaMap.class.getResource("/schema/VOTable-1.1.xsd"));
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.2",SchemaMap.class.getResource("/schema/VOTable-1.2.xsd"));
        ALL.put("http://www.ivoa.net/xml/VOTable/v1.3",SchemaMap.class.getResource("/schema/VOTable-1.4.xsd"));

        //vo-resource-types
        ALL.put("http://www.ivoa.net/xml/ConeSearch/v1.0",SchemaMap.class.getResource("/schema/ConeSearch-v1.0.xsd"));
        
        ALL.put("http://www.ivoa.net/xml/SSA/v1.1",SchemaMap.class.getResource("/schema/SSA-v1.2.xsd"));
                
        ALL.put("http://www.ivoa.net/xml/SIA/v1.0",SchemaMap.class.getResource("/schema/SIA-v1.0.xsd"));        
        ALL.put("http://www.ivoa.net/xml/SIA/v1.1",SchemaMap.class.getResource("/schema/SIA-v1.1.xsd"));        
        ALL.put("http://www.ivoa.net/xml/SkyNode/v0.2",SchemaMap.class.getResource("/schema/OpenSkyNode-v0.2.xsd"));      
        ALL.put("http://www.ivoa.net/xml/VODataService/v1.0",SchemaMap.class.getResource("/schema/VODataService-v1.0.xsd"));        
        ALL.put("http://www.ivoa.net/xml/VODataService/v1.1",SchemaMap.class.getResource("/schema/VODataService-v1.2.xsd"));        
       
      	ALL.put("http://www.ivoa.net/xml/VORegistry/v1.0",SchemaMap.class.getResource("/schema/VORegistry-v1.0.xsd"));        	
       
        ALL.put("http://www.ivoa.net/xml/VOResource/v1.0",SchemaMap.class.getResource("/schema/VOResource-v1.1.xsd"));
        
      	ALL.put("http://www.ivoa.net/xml/VOApplication/v1.0rc1",SchemaMap.class.getResource("/schema/VOApplication-1.0.xsd"));
        ALL.put("http://www.ivoa.net/xml/StandardsRegExt/v1.0",SchemaMap.class.getResource("/schema/StandardsRegExt-1.0.xsd"));

        
        ALL.put("http://www.ivoa.net/xml/TAPRegExt/v1.0",SchemaMap.class.getResource("/schema/TAPRegExt-v1.0.xsd"));
        
        ALL.put("http://www.ivoa.net/xml/SLAP/v1.0",SchemaMap.class.getResource("/schema/SLAP-v1.1.xsd"));

        //VOSI
        ALL.put("http://www.ivoa.net/xml/VOSIAvailability/v1.0", SchemaMap.class.getResource("/schema/VOSIAvailability-v1.0.xsd"));
        ALL.put("http://www.ivoa.net/xml/VOSICapabilities/v1.0", SchemaMap.class.getResource("/schema/VOSICapabilities-v1.0.xsd"));
        ALL.put("http://www.ivoa.net/xml/VOSITables/v1.0", SchemaMap.class.getResource("/schema/VOSITables-v1.1.xsd"));

        ALL.put("http://www.ivoa.net/xml/UWS/v1.0", SchemaMap.class.getResource("/schema/UWS-1.1.xsd"));

        ALL.put("http://www.ivoa.net/xml/VODML/v1", SchemaMap.class.getResource("/schema/vo-dml-v1.0.xsd"));
    }
    
    public static URL getSchemaURL(String namespace)
    {
	return ALL.get(namespace);
    }
    
    
    public static StreamSource[] getRegistrySchema()
    {
        List<Namespaces> ns = Arrays.asList(RI,VR,VS,SIA,CS,REG, VA, VSTD, VOSI_TAB, STC);
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

    
    /**
     * A {@link LSInput} suitable for validation. Note that this is not general - lots of the interface is unimplemented
     * @author Paul Harrison (paul.harrison@manchester.ac.uk) 
     * @since 17 Aug 2021
     */
    private static class SchemaInput implements LSInput {

        
        private final String namespace;
        private final URL url;
        
        public SchemaInput(String namespace, URL url) {
            this.namespace = namespace;
            this.url = url;
        }
       
        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getCharacterStream()
         */
        @Override
        public Reader getCharacterStream() {
         return null;            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setCharacterStream(java.io.Reader)
         */
        @Override
        public void setCharacterStream(Reader characterStream) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setCharacterStream() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getByteStream()
         */
        @Override
        public InputStream getByteStream() {
            try {
                return url.openStream();
            } catch (IOException e) {
                logger.error("cannot open stream for schema",e);
                return null;
            }
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setByteStream(java.io.InputStream)
         */
        @Override
        public void setByteStream(InputStream byteStream) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setByteStream() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getStringData()
         */
        @Override
        public String getStringData() {
            return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setStringData(java.lang.String)
         */
        @Override
        public void setStringData(String stringData) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setStringData() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getSystemId()
         */
        @Override
        public String getSystemId() {
           return url.toString();            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setSystemId(java.lang.String)
         */
        @Override
        public void setSystemId(String systemId) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setSystemId() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getPublicId()
         */
        @Override
        public String getPublicId() {
            return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setPublicId(java.lang.String)
         */
        @Override
        public void setPublicId(String publicId) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setPublicId() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getBaseURI()
         */
        @Override
        public String getBaseURI() {
            return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setBaseURI(java.lang.String)
         */
        @Override
        public void setBaseURI(String baseURI) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setBaseURI() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getEncoding()
         */
        @Override
        public String getEncoding() {
            return null;
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setEncoding(java.lang.String)
         */
        @Override
        public void setEncoding(String encoding) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setEncoding() not implemented");
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#getCertifiedText()
         */
        @Override
        public boolean getCertifiedText() {
            return false;
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.w3c.dom.ls.LSInput#setCertifiedText(boolean)
         */
        @Override
        public void setCertifiedText(boolean certifiedText) {
            // TODO Auto-generated method stub
            throw new  UnsupportedOperationException("LSInput.setCertifiedText() not implemented");
            
        }

        
    }


    /**
     * @param namespaceURI
     * @return
     */
    public static LSInput getSchemaLSInput(String namespaceURI) {
       logger.trace("looking up schema with namespaxe {}", namespaceURI);
       return new SchemaInput(namespaceURI, ALL.get(namespaceURI));
        
    }
}


