package org.javastro.ivoa.schema;



import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A program to locate all the schema files in the schema-source tree and to
 * write an HTML page as an index for them.
 *
 * Assume that the program always runs from the base directory of the contracts
 * project.
 *
 * @author Guy Rixon
 */
public class SchemaIndexer {

  public static void main(String[] args) throws Exception {
    File target = new File("target/site/schema");

    SchemaIndexer i = new SchemaIndexer(target);
    i.beginDocument();

    i.beginSection("Common Execution Architecture and UWS");
    i.searchTree(new File("src/schema/cea"));
    i.endSection();

    i.beginSection("Coordinate systems");
    i.searchTree(new File("src/schema/stc"));
    i.endSection();

    i.beginSection("Data formats");
    i.searchTree(new File("src/schema/vo-formats"));
    i.endSection();

    i.beginSection("DSA metadata");
    i.searchTree(new File("src/schema/dsa"));
    i.endSection();

    i.beginSection("JES");
    i.searchTree(new File("src/schema/jes"));
    i.endSection();

    i.beginSection("OAI");
    i.searchTree(new File("src/schema/oai"));
    i.endSection();

    i.beginSection("Registry service");
    i.searchTree(new File("src/schema/registry"));
    i.endSection();

    i.beginSection("Resource-registry entries");
    i.searchTree(new File("src/schema/vo-resource-types"));
    i.endSection();

    i.beginSection("Query language");
    i.searchTree(new File("src/schema/adql"));
    i.endSection();

    i.beginSection("VOEvent");
    i.searchTree(new File("src/schema/VOEvent"));
    i.endSection();

    i.beginSection("VOSpace");
    i.searchTree(new File("src/schema/vo-space"));
    i.endSection();
    
    i.beginSection("VOSI");
    i.searchTree(new File("src/schema/VOSI"));
    i.endSection();

    i.endDocument();
  }

  private PrintWriter out;

  public SchemaIndexer(File targetTree) throws Exception {
    targetTree.mkdirs();
    File index = new File(targetTree, "index.html");
    out = new PrintWriter(index, "UTF-8");
  }

  public void beginDocument() throws Exception {
    out.println("<html>");
    out.println("<body>");
    out.println("<h1>Index of schemata</h1>");
    out.println("<p>The URIs in this list are the namespace URIs for AstroGrid");
    out.println("schemata that are stable and published. The hyperlinks in");
    out.println("the list point to the schema documents (.xsd, .wsdl);");
    out.println("the target URLs of the hyperlinks are the location URIs of");
    out.println("the schemata (i.e. you can use them in <i>schemaLocation</i>");
    out.println("attributes in your documents).</p>");
    out.println();
  }

  public void endDocument() throws Exception {
    out.println("</body>");
    out.println("</html>");
    out.flush();
  }

  public void beginSection(String title) throws Exception {
    out.print("<h2>");
    out.print(title);
    out.println("</h2>");
    out.println("<ul>");
  }

  public void endSection() throws Exception {
    out.println("</ul>");
    out.println();
  }

  /**
   * Searches a directory tree, "breadth first". First, lists all .xsd and
   * .wsdl files. Second, calls itself recursively on each sub-directory.
   *
   * @param startingDirectory The directry to search.
   */
  public void searchTree(File startingDirectory) throws Exception {
    searchTree(null, startingDirectory);
  }

  /**
   * Searches a directory tree, "breadth first". First, lists all .xsd and
   * .wsdl files. Second, calls itself recursively on each sub-directory.
   *
   * @param prefix A name prefix used when extracting namespaces.
   * @startingDirectory The directry to search.
   */
  public void searchTree(File prefix, File startingDirectory) throws Exception {
    prefix = (prefix == null)? new File(startingDirectory.getName()) :
                               new File(prefix, startingDirectory.getName());
    System.out.print(prefix);
    System.out.print(" => ");
    System.out.println(startingDirectory);

    File[] schemata = startingDirectory.listFiles(
      new FilenameFilter() {
        public boolean accept(File f, String s) {
          return s.endsWith(".xsd") || s.endsWith(".wsdl");
        }
      }
    );

    for (int i = 0; i < schemata.length; i++) {
      System.out.println(schemata[i].getAbsolutePath());
      extractNamespace(prefix, schemata[i]);
      transcribeSchema(prefix, schemata[i]);

    }

    File[] directories = startingDirectory.listFiles(
      new FileFilter() {
        public boolean accept(File f) {
          return f.isDirectory();
        }
      }
    );

    for (int i = 0; i < directories.length; i++) {
      searchTree(prefix, directories[i]);
    }
  }

  /**
   * Parses a schema file (XSD or WSDL), extracting the target namespace.
   *
   * @param prefix A prefix to added to the links in the output.
   * @param schemaFile The file to transform.
   */
  public void extractNamespace(File prefix, File schemaFile) throws Exception {
    StreamSource schema = new StreamSource(schemaFile);
    StreamSource transform = new StreamSource(
      new FileInputStream("src/xsl/extract-namespace.xsl")
    );
    Transformer t = TransformerFactory.newInstance().newTransformer(transform);
    StreamResult result = new StreamResult(out);
    String href = prefix.toString() + "/" + schemaFile.getName();
    t.setParameter("filename", href);
    t.transform(schema, result);
  }

  /**
   * Copies the source schemata to the site-publishing tree, transforming
   * them in transit to fix the location and schemalocation attributes.
   */
  public void transcribeSchema(File prefix, File schemaFile) throws Exception {
    StreamSource schema = new StreamSource(schemaFile);
    StreamSource transform = new StreamSource(
      new FileInputStream("src/xsl/absolute-location.xsl")
    );
    Transformer t = TransformerFactory.newInstance().newTransformer(transform);
    File outDir = new File("target/site/schema", prefix.toString());
    outDir.mkdirs();
    File outFile = new File(outDir, schemaFile.getName());
    System.out.println("Transforming to " + outFile.toString());
    StreamResult result = new StreamResult(outFile);
    t.transform(schema, result);
  }

}
