package org.javastro.ivoa.schema;

/**
 * URI values for the standardID attribute.
 * Registrations of IVO service based on VOResource 1.0 and later use
 * capability elements which are distinguished by the values of their
 * standardID attribute. The values are usually IVORNs pointing to
 * the registrations of standards. This class lists the known ones:
 * global standards acknowledged by IVOA and local standards defined by
 * AstroGrid.
 *
 * @author Guy Rixon
 */
public class StandardIds {
  
  /**
   * Cone search: ivo://ivoa.net/std/ConeSearch.
   * Taken from IVOA's XML schema for http://www.ivoa.net/xml/ConeSearch/v1.0.
   */
  public final static String CONE_SEARCH = "ivo://ivoa.net/std/ConeSearch";
  
  /**
   * Simple Image Access Protocol: ivo://ivoa.net/std/SIA.
   * Taken from IVOA's XML schema for http://www.ivoa.net/xml/SIA/v1.0.
   */
  public final static String SIAP = "ivo://ivoa.net/std/SIA";
  
  /**
   * Simple Spectral Access Protocol: ivo://ivoa.net/std/SSA.
   * Taken from IVOA's XML schema for http://www.ivoa.net/xml/SSA/v0.2
   */
  public final static String SSAP = "ivo://ivoa.net/std/SSA";
  
  /**
   * Open Sky Node: ivo://ivoa.net/std/OpenSkyNode.
   * Taken from IVOA's XML schema for http://www.ivoa.net/xml/SkyNode/v0.2,
   */
  public final static String OPEN_SKY_NODE = 
      "ivo://ivoa.net/std/OpenSkyNode";
  
  /**
   * IVOA registry: ivo://ivoa.net/std/Registry.
   * Taken from IVOA's standard document for the registry interface.
   */
  public final static String REGISTRY = "ivo://ivoa.net/std/Registry";
  
  /**
   * Common Execution Architecture: ivo://org.astrogrid/std/CEA/v1.0.
   * Chosen by AstroGrid during cycle 1 of AG3.
   * A capability for CEA v1.0 implies the CEC SOAP service.
   */
  public final static String CEA = "ivo://org.astrogrid/std/CEA/v1.0";
  
  /**
   * Simple Temporal Access Protocol: ivo://org.astrogrid/std/STAP/v1.0.
   * Chosen by AstroGrid during cycle 1 of AG3.
   */
  public final static String STAP = 
      "ivo://org.astrogrid/std/STAP/v1.0";
  
   /**
   * Table Access Protocol: ivo://ivoa.net/std/TAP.
   */
  public final static String TAP = 
      "ivo://ivoa.net/std/TAP";
    
   
}
