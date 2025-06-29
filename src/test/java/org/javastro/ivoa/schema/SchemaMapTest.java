package org.javastro.ivoa.schema;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JUnit tests for SchemaMap.
 *
 * @author Guy Rixon
 */
public class SchemaMapTest  {
  
  @Test
  public void testAllEntriesPresent() throws Exception {
    Map<String, URL> m = SchemaMap.ALLUrl;
    
    // The values in the schema map are the URLs leading to the schemata; the
    // keys are the namespace URIs. The URLs typically lead into the contracts
    // jar. For each key, check that there is a value that the value is a URL
    // and the the URL points to data. Don't bother reading the data.
    for (Iterator<String> i = m.keySet().iterator(); i.hasNext(); ) {
      Object k = i.next();
      System.out.println("\n" + k);
      Object v = m.get(k);
      assertNotNull(v);
      System.out.println(v);
      assertTrue(v instanceof URL);
      URL u = (URL)v;
      File f = new File(u.toURI());
      assertTrue(f.exists(),f.getName()+" does not exist");
    }
    System.out.println("catalogue");
    System.out.println(SchemaMap.xmlCatalogue);
  }
  
 
  
}
