/* 
 * Created on 17 Mar 2008 by Paul Harrison (paul.harrison@manchester.ac.uk)
 * Copyright 2008 Astrogrid. All rights reserved.
 *
 * This software is published under the terms of the Astrogrid 
 * Software License, a copy of which has been included 
 * with this distribution in the LICENSE.txt file.  
 *
 */ 

package org.javastro.ivoa.schema;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates the "current" namespaces. This gives symbolic names to the most up to date namespace URIs
 * used within schema in astrogrid, as well as providing a default prefix suggestion for the namespace URI.
 * 
 * Note that this class should explictly avoid making references to obsolete namespaces.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 17 Mar 2008
 * @version $Name:  $
 * @since VOTech Stage 7
 */
public final class Namespaces {

    static final Map<String, Namespaces> prefixes = new HashMap<String, Namespaces>(); 
    static final Map<String, Namespaces> namespaces = new HashMap<String, Namespaces>();
    private final String prefix;
    private final String namespace;
    
    private Namespaces(String prefix, String namespace){
	this.namespace = namespace;
	this.prefix = prefix;
	Namespaces.prefixes.put(prefix, this);
	Namespaces.namespaces.put(namespace, this);
    }
// useful namespaces...   
    public static final Namespaces XSI = new Namespaces("xsi","http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespaces XSD = new Namespaces("xsd","http://www.w3.org/2001/XMLSchema");
    public static final Namespaces RI = new Namespaces("ri","http://www.ivoa.net/xml/RegistryInterface/v1.0");
    public static final Namespaces REG = new Namespaces("reg","http://www.ivoa.net/xml/VORegistry/v1.0");
    public static final Namespaces VR = new Namespaces("vr","http://www.ivoa.net/xml/VOResource/v1.0");
    public static final Namespaces VS = new Namespaces("vs","http://www.ivoa.net/xml/VODataService/v1.1");
    public static final Namespaces VA = new Namespaces("va","http://www.ivoa.net/xml/VOApplication/v1.0rc1");
    public static final Namespaces UWS = new Namespaces("uws","http://www.ivoa.net/xml/UWS/v1.0");
    public static final Namespaces XLINK = new Namespaces("xlink","http://www.w3.org/1999/xlink");
    public static final Namespaces STC = new Namespaces("stc","http://www.ivoa.net/xml/STC/stc-v1.30.xsd");
    public static final Namespaces REGTAP = new Namespaces("tr","http://www.ivoa.net/xml/TAPRegExt/v1.0");  
    public static final Namespaces CS = new Namespaces("cs","http://www.ivoa.net/xml/ConeSearch/v1.0");  
    public static final Namespaces SIA = new Namespaces("sia","http://www.ivoa.net/xml/SIA/v1.1");  
    public static final Namespaces VSTD = new Namespaces("vstd","http://www.ivoa.net/xml/StandardsRegExt/v1.0");  
    public static final Namespaces VOSI_CAP = new Namespaces("vca","http://www.ivoa.net/xml/VOSICapabilities/v1.0");
    public static final Namespaces VOSI_TAB = new Namespaces("vta","http://www.ivoa.net/xml/VOSITables/v1.0");
    public static final Namespaces VOSI_AV = new Namespaces("vav","http://www.ivoa.net/xml/VOSIAvailability/v1.0");
    public static final Namespaces OAI_PMH = new Namespaces("oai", "http://www.openarchives.org/OAI/2.0/");
    public static final Namespaces VODML = new Namespaces("vodml", "http://www.ivoa.net/xml/VODML/v1");
    public static final Namespaces SSAP = new Namespaces("ssap", "http://www.ivoa.net/xml/SSA/v1.1");
    public static final Namespaces SLAP = new Namespaces("slap", "http://www.ivoa.net/xml/SLAP/v1.0");
    public static final Namespaces VOT = new Namespaces("vot", "http://www.ivoa.net/xml/VOTable/v1.3");
    
     
//TODO add the other "current" ones...         
    public static String[] getNamespaceURIs()
    {
	return (String[]) namespaces.keySet().toArray(new String[0]);
    }
    
    public static Namespaces getNameSpaceFromPrefix(String prefix)
    {
	return (Namespaces) prefixes.get(prefix);
    }
    
    public static Namespaces getNameSpaceFromURI(String uri)
    {
	return (Namespaces) namespaces.get(uri);
    }

    public String getPrefix() {
        return prefix;
    }

    public String getNamespace() {
        return namespace;
    }
    
    
}


