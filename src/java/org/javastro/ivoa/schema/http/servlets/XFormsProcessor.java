/*

 */

package org.javastro.ivoa.schema.http.servlets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.StringReader;
import java.io.BufferedReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Properties;
import java.io.Writer;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource; 
import javax.xml.transform.stream.StreamResult;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;


/**
 * Class: XFormsProcessor
 * Description: The processor is a XForms servlet which will build up XForms related to a particular xforms document
 * defined in the xfomrs/properties/xforms.prop file.  Clients may pass in certain variables and instance urls to help
 * construct a xforms document.  If nothing is passed in then a Generic html form is produced allowing them to choose
 * the XForms they wish to construct based on an empty instance document.  This class uses various xml/html pieces to
 * build up a full html document for XForms. By default it has three submit abilities after the user fills out the
 * xforms/html and hits submit it send the filled out XML either to a file on the server, send it as part of a SOAP web service or
 * go directly to a url depending on how it is defined as a servlet in the web.xml.  It uses the FormFaces XForms implementation.
 * Since this deals with normally editing xml that is essentially for an application it should be placed behind
 * necessary security restrictions.  Typically in "/admin"
 * Example of what needs to be placed in a web.xml:
 * 
 *     <servlet>
        <servlet-name>XForms</servlet-name>
        <servlet-class>org.astrogrid.contracts.http.servlets.XFormsProcessor</servlet-class>
        <!-- 
         the xforms.output can be to a one of the three 
            file:// (for file saving on the server a backup of the original file if any will be created and timestamped) or 
            http:// (to your own web page such as a jsp or servlet)
            http:// (with another optional xforms.output.soapresponse to define a soap element
            which is used to place the submitted XML into the SOAP:Body and sent).
         -->
        <init-param>
            <param-name>xforms.output</param-name>
            <param-value>file:///usr/config/cea.xml</param-value>
        </init-param>
        <!--
          Usually not needed, defaults to "..", used for a simple menu for the user to go back home instead of doing the
          xforms.
        -->
        <init-param>
            <param-name>xforms.home.url</param-name>
            <param-value>http://www.server.com/index.jsp</param-value>
        </init-param>
    </servlet>
    
    <servlet-mapping>
        <servlet-name>XForms</servlet-name>
        <url-pattern>/admin/XForms</url-pattern>
    </servlet-mapping>
    
    TODO  Probably move this out of contracts and use some other thrid party libraries to make things easier.
    TODO  Sending SOAP message seems to work just missing the SoapAction placed in the header hence again thrid partly lib could make it easier
    instead of doing it all with the jdk.
    TODO  Exception Handling has not been done nor logging.
    TODO  File Save works of the xforms, but only just added the check and rename ability for the backup/original file.
    TODO  When submitting the XML empty nodes are still sent in the request typically you wan't those removed.  A xsl sheet
    is in the xforms/xsl area called RemoveChildNodes.xsl.  FormFaces is supposed to be fixing this area relatively soon.
    TODO  Making it pretty
    TODO  :(   IE works fine at the moment but mozilla does not like loading a css and js file from a file:// url which comes
    from the classloader.getResourceURL("xforms/js/formfaces.js").  Not sure what to do about this, it cannot be loaded and written into the page
    has certain tags that makes it get messed up that way in a DOM object I think 2 CDATA tags which are not allowed.  I think the only 
    solution at the moment is either a.) hard coded http url b.) http url back to this servlet that simply streams out the js and css pages.    
 * 
 */
public class XFormsProcessor extends HttpServlet {
    
    Properties props = null;
    
    private static final String XFORMS_PROPERTIES = "xforms/properties/xforms.prop";

    /*
     * variables to hold information from the config in the web.xml
     */
    private static String outputURL;
    private static String soapResource;
    private static String homeURL;
    
    
    /**
     * Initialize servlet for log4j purposes in servlet container (war file).
     */
    public void init() throws ServletException {

        props = new Properties();
        
        //Load the properties file.
        ClassLoader loader = this.getClass().getClassLoader();
        try {
        props.load(loader.getResourceAsStream(XFORMS_PROPERTIES));
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        //get info from the config.
        outputURL = getInitParameter("xforms.output");
        soapResource = getInitParameter("xforms.output.soapResource");
        homeURL = getInitParameter("xforms.home.url");
        if(homeURL == null || homeURL.trim().length() == 0) {
            homeURL = "/";
        }
    }
    
    /**
     * Method: makeDoc
     * Description: Quick helper method to make a Document DOM object from an InputSource.
     * @param in InputSource
     * @return Document DOM
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document makeDoc(InputSource in) throws ParserConfigurationException, SAXException, IOException
    {
          DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
          dbf.setNamespaceAware(true);          
          DocumentBuilder builder = dbf.newDocumentBuilder();
          System.out.println("making Document object in inputsource");
          Document doc = builder.parse(in);
          System.out.println("made document object root node = "  + doc.getDocumentElement().getNodeName());
          return doc;
    }
    
    /**
     * Method: makeDoc
     * Description: Quick helper method to make a Document DOM object from an InputStream.
     * @param in InputSource
     * @return Document DOM
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private Document makeDoc(InputStream is)  throws ParserConfigurationException, SAXException, IOException 
    {        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);        
        DocumentBuilder builder = dbf.newDocumentBuilder();
        System.out.println("making Document object in inputstream");
        Document doc = builder.parse(is);        
        System.out.println("made document object root node = "  + doc.getDocumentElement().getNodeName());
        return doc;
    }
    
    /**
     * Method: doGet
     * Description: standard doGet for all http GET requests currently sends it to doPost since both
     * post and get handle the same requirements.
     *
     * @param req HTTP Request object
     * @param res HTTP Response object
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) {
        doPost(req,res);
    }
    
    
    /**
     * Method: doPost
     * Description: Does the analyzing and processing of the request.  Handles piecing together a 
     * Document DOM object to be streamed out as HTML to the browser which is there xforms.  When the user
     * submits the XForms it handles the response to save it to a file or do a soap web service call.
     *
     * @param req HTTP Request object
     * @param res HTTP Response object
     */
    public void doPost(HttpServletRequest req, HttpServletResponse res) {
        
        //What kind of type/request is this.  The xforms.type is 
        //mapped in the properties file for particular xforms models.
        String type = req.getParameter("xforms.type");
        //See if the user put a request of an instance document to be used to fill out
        //the controls in the html.
        String instance = req.getParameter("xforms.instance");
        
        //see if a saveFile request has come in, below we will set the xforms outputURL to be
        //"?saveFile=true" if the outputURL is a file:// type url.
        String saveFile = req.getParameter("saveFile");
        
        //see if a saveFile request has come in, below we will set the xforms outputURL to be
        //"?sendWebService=true" if the outputURL is a http:// type url with a soapResource set in the web.xml.        
        String sendWebService = req.getParameter("sendWebService");
        
        String streamFile = req.getParameter("streamFile");
        
        
        String []models;
        InputStream is;
        String temp = null;
        ClassLoader loader = this.getClass().getClassLoader();
        Writer out = null;
        System.out.println("here is xforms.type = " + type);
        System.out.println("here is xforms.instance = " + instance);
        try {
            out = res.getWriter();
            //User is saving the file to disk.
            if(saveFile != null) {
                saveFile(req,out);
                return;
            }
            
            if(streamFile != null) {
                /*
                URL streamFileURL = loader.getResource(streamFile);
                System.out.println("start streaming this file = " + streamFile);
                BufferedReader br = new BufferedReader(new java.io.InputStreamReader(streamFileURL.openStream()));
                while((temp = br.readLine()) != null) {
                    System.out.println("streamfile contents temp = " + temp);
                    out.write(temp);
                }//while
                System.out.println("finished streaming file streamFile = " + streamFile);
                return;
                */
                URL streamFileURL = loader.getResource(streamFile);
                System.out.println("start streaming this file = " + streamFile);
                java.io.InputStreamReader isr = new java.io.InputStreamReader(streamFileURL.openStream());
                int tempTry;
                while((tempTry = isr.read()) != -1) {
                    //System.out.println("streamfile contents temp = " + tempTry);
                    //out.write();
                    out.write(tempTry);
                }//while
                System.out.println("finished streaming file streamFile = " + streamFile);
                return;
            }
            
            
            //user is sending it as a webservice to a url endpoint.
            if(sendWebService != null) {
                sendToWebService(req,out);
                return;
            }
            
            //User came in with nothing really on the request show the user the
            //default generic html page listing all the xforms.type's available for 
            //choosing.
            if(type == null) {
                out.write(makeGenericXFormPage());
                return;
            }
            //okay get all the moel file names must be comma seperated.
            temp = props.getProperty(type);
            System.out.println("got the type/model property = " + temp);
            //split into an array.
            models = temp.split(",");
            System.out.println("models size = " + models.length);
            //check if there is a null, if not then lets load an empty instance document.
            if(instance == null || instance.trim().length() == 0) {
                System.out.println("making from = " + "xforms/instance/" + props.getProperty(type + ".instance"));
                is = loader.getResourceAsStream("xforms/instance/" + props.getProperty(type + ".instance"));
                System.out.println("have inputsource for is");
            }else {
                URL url = new URL(instance);
                is = new BufferedInputStream(url.openStream());
            }
            byte []info;
            System.out.println("making instanceDoc");
            //make a Document DOM of the instance xml.
            Document instanceDoc = makeDoc(is);
            System.out.println("have it = " + instanceDoc.getDocumentElement().getNodeName());
            //get the formfaces and css files.
            URL cssURL = loader.getResource("xforms/css/xforms.css");
            URL jsURL = loader.getResource("xforms/js/formfaces.js");
            String htmlString = "<html xmlns:ev=\"http://www.w3.org/2001/xml-events\" " +
          "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
          "xmlns:xforms=\"http://www.w3.org/2002/xforms\" " +
          "xmlns=\"http://www.w3.org/1999/xhtml\" " +          
          "xmlns:xhtml=\"http://www.w3.org/1999/xhtml\"" +
          ">" +
          "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"" + req.getRequestURL() + "?streamFile=xforms/css/xforms.css\">Style</link> " +
          //"<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"" + cssURL.toString() + "\">Style</link> " +
          "<script type=\"text/javascript\" src=\"" + req.getRequestURL() + "?streamFile=xforms/js/formfaces.js\">Nothing</script> " +          
          //"<script type=\"text/javascript\" src=\"" + jsURL.toString() + "\">Nothing</script> " +
          " <xforms:model> " +
          "</xforms:model> \n</head>\n<body>  </body> </html>";
                        
          System.out.println("the htmlstring = " + htmlString);
          //create an htmlDoc and put in the instanceNode.
          Document htmlDoc = makeDoc(new InputSource(new StringReader(htmlString)));
          System.out.println("try2 to append instanceNode into model");
          Node instanceNode = htmlDoc.importNode(instanceDoc.getDocumentElement(),true);
          
          System.out.println("importNode done and size of how many models = " + htmlDoc.getDocumentElement().getElementsByTagName("model").getLength());
          
          
          Node xfModelNode = htmlDoc.getDocumentElement().getElementsByTagNameNS("*","model").item(0);
          Element submissionElement = htmlDoc.createElementNS("http://www.w3.org/2002/xforms","submission");
          submissionElement.setPrefix("xforms");
          //submissionElement.setAttribute("method","post");
          submissionElement.setAttribute("method","post");
          //set the outputURL to go back to this servlet with a saveFile or sendWebService
          //else just go to the url.
          if(outputURL.startsWith("file")) {
              submissionElement.setAttribute("action","?saveFile=true");
          }else if(soapResource != null) {
              submissionElement.setAttribute("action","?sendWebService=true");
          } else {
              submissionElement.setAttribute("action",outputURL);
          }
          submissionElement.setAttribute("id","s00");
          
          System.out.println("got modelnode lets appendChild");
          xfModelNode.appendChild(instanceNode);
          
          System.out.println("okay placed in the instanceNode lets put in the body/model");
          //Okay start filling out the body with the various model xforms html.
          Node bodyNode = htmlDoc.getDocumentElement().getElementsByTagNameNS("*","body").item(0);
          for(int i = (models.length - 1);i >= 0;i--) {
              System.out.println("loading = " + "xforms/html/" + models[i]);
              Document modelDoc = makeDoc(loader.getResourceAsStream("xforms/html/" + models[i]));
              Node modelNode = htmlDoc.importNode(modelDoc.getDocumentElement(),true);              
              System.out.println("added into the bodyNode");
              bodyNode.appendChild(modelNode);
          }//for
          System.out.println("going for bindings");
          //xfModelNode = htmlDoc.getDocumentElement().getElementsByTagNameNS("*","model").item(0);
          //check if there are any bindings if so add that to the model piece of the html.
          for(int i = (models.length - 1);i >= 0;i--) {
              InputStream bindingStream = loader.getResourceAsStream("xforms/bindings/" + models[i]);
              if(bindingStream != null) {
                  Document bindingDoc = makeDoc(bindingStream);
                  NodeList bindingNodeList = bindingDoc.getDocumentElement().getChildNodes();
                  for(int j = 0;j < bindingNodeList.getLength();j++) {
                      Node bindNode = htmlDoc.importNode(bindingNodeList.item(j),true);
                      xfModelNode.appendChild(bindNode);
                  }//for
              }
          }//for
          System.out.println("done with bindings now put in the submit elements");
          //add the submission element.
          xfModelNode.appendChild(submissionElement);
          
          Element submitXFormElement = htmlDoc.createElementNS("http://www.w3.org/2002/xforms","submit");
          submitXFormElement.setPrefix("xforms");
          submitXFormElement.setAttribute("submission","s00");
          Element submitLabel = htmlDoc.createElementNS("http://www.w3.org/2002/xforms","label");
          submitLabel.setPrefix("xforms");
          
          if(outputURL.startsWith("file")) {
              submitLabel.appendChild(htmlDoc.createTextNode("Save File"));
          } else
          {
              submitLabel.appendChild(htmlDoc.createTextNode("Submit"));    
          }
          
          submitXFormElement.appendChild(submitLabel);
          bodyNode.appendChild(submitXFormElement);
          
          //this is a FormFaces thing you can do to add a status.
          //which puts out xforms debug info at the bottom.
          Element statusElem = htmlDoc.createElement("p");
          statusElem.setAttribute("id","status");
          bodyNode.appendChild(statusElem);
          //take this Document and stream it out to the browser.
          //using the default Transformations.
          transform(htmlDoc,out);
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        }catch(SAXException sax) {
            sax.printStackTrace();
        }finally {
            try {
                out.flush();
                out.close();
            }catch(IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("done with servlet everything should be streamed out now.");
        }
        
          
    }


    /**
     * Method: transform
     * Description: basic transformer to take a Document DOM and send it to the Writer.
     * @param sourceDoc
     * @param out
     * @throws IOException
     */
    private void transform(Document sourceDoc, Writer out) throws IOException {
            try {
                System.out.println("going to transformer factory");
                TransformerFactory tFactory = TransformerFactory.newInstance();
                Transformer transformer = tFactory.newTransformer();
                DOMSource source = new DOMSource(sourceDoc);
                StreamResult result = new StreamResult(out);
                System.out.println("perform transformation");
                transformer.transform(source, result);
            } catch (TransformerConfigurationException tce) {
                // Error generated by the parser
                System.out.println ("* Transformer Factory error");
                System.out.println("  " + tce.getMessage() );
      
                 // Use the contained exception, if any
                Throwable x = tce;
                if (tce.getException() != null)
                  x = tce.getException();
                x.printStackTrace(); 
                out.write(errorHTMLString());          
            } catch (TransformerException te) {
                // Error generated by the parser
                System.out.println ("* Transformation error");
                System.out.println("  " + te.getMessage() );
      
                // Use the contained exception, if any
                Throwable x = te;
                if (te.getException() != null)
                  x = te.getException();
                x.printStackTrace();
                out.write(errorHTMLString());
            }
    }

    /**
     * Method: transform
     * Description: basic transformer to take a Document DOM and send it to the OutputStream.
     * @param sourceDoc
     * @param out
     * @throws IOException
     */
    private void transform(Document sourceDoc, OutputStream out) throws IOException {              
        try {
            System.out.println("going to transformer factory");
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            DOMSource source = new DOMSource(sourceDoc);
            StreamResult result = new StreamResult(out);
            System.out.println("perform transformation");
            transformer.transform(source, result);
        } catch (TransformerConfigurationException tce) {
            // Error generated by the parser
            System.out.println ("* Transformer Factory error");
            System.out.println("  " + tce.getMessage() );
  
             // Use the contained exception, if any
            Throwable x = tce;
            if (tce.getException() != null)
              x = tce.getException();
            x.printStackTrace(); 
            out.write(errorHTMLString().getBytes());          
        } catch (TransformerException te) {
            // Error generated by the parser
            System.out.println ("* Transformation error");
            System.out.println("  " + te.getMessage() );
  
            // Use the contained exception, if any
            Throwable x = te;
            if (te.getException() != null)
              x = te.getException();
            x.printStackTrace();
            out.write(errorHTMLString().getBytes());
        }
    }
    
    /**
     * Method: getXML
     * Description: Takes the XML submitted in a POST request and returns it as a String.
     * Currently not sure in Xforms how to get rid of the <data> xml element so strip it out
     * of the string.
     * @param req
     * @param out
     * @return
     * @throws IOException
     */
    private String getXML(HttpServletRequest req, Writer out) throws IOException {
        BufferedReader br = req.getReader();
        String line = null;
        String xmlString = "";
        int temp = -1;
        boolean startconcat = false;        
        while((line = br.readLine()) != null) {
            if(startconcat) {             
             xmlString += line + "\n";   
            } else {
                if((temp = line.indexOf("<data")) != -1) {
                    startconcat = true;
                    xmlString = line.substring(line.indexOf(">",temp) + 1) + "\n";
                }
            }
        }
        if(xmlString.trim().length() > 0)
            xmlString = xmlString.substring(0,xmlString.indexOf("</data"));        
        return xmlString;
    }
    
    /**
     * Method: sendToWebService
     * Description: takes soapResource which is a SOAP envelope and body then takes XML from a POST request
     * and inserts into a SOAP:Body {first child element which should be the web service method} and 
     * sends it to the outputURL which is presumbly an outputURL which is an endpoint.
     * @param req
     * @param out
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void sendToWebService(HttpServletRequest req, Writer out) throws IOException, ParserConfigurationException, SAXException  {
        String xmlString = getXML(req,out);
        InputStream is;
        if(soapResource.startsWith("http") || soapResource.startsWith("file")) {
            is = new URL(soapResource).openStream();
        } else {
            is = this.getClass().getClassLoader().getResourceAsStream("xforms/soap/" + soapResource);   
        }

        Document doc = makeDoc(is);
        Document xmlDoc = makeDoc(new InputSource(new StringReader(xmlString)));
        Node xmlNode = doc.importNode(xmlDoc.getDocumentElement(),true);
        
        Node soapBodyNode = doc.getDocumentElement().getElementsByTagNameNS("*","Body").item(0);
        if(soapBodyNode.hasChildNodes()) {
            soapBodyNode.getFirstChild().appendChild(xmlNode);
        } else {
            soapBodyNode.appendChild(xmlNode);    
        }
        HttpURLConnection huc = (HttpURLConnection) new URL(outputURL).openConnection();
        huc.setRequestMethod("POST");
        huc.setDoOutput(true);
        huc.setDoInput(true);
        huc.connect();
        OutputStream os = huc.getOutputStream();
        transform(doc,os);
        os.flush();
        
        is = huc.getInputStream();
        String htmlString = "<html><head><title>Info sent to Web Service</title></head>" +
        "<body><p>The XML shown below is the response from a web service call to the designated output. (Being HTML you may need to do a View source to see xml completely)<br /> " + 
        " Click <a href='" + homeURL + "'>here</a> to go back home.<br />";
        
        out.write(htmlString);        
        while(is.available() > 0) {
            byte []info = new byte[is.available()];
            is.read(info);
            out.write(new String(info));
        }
        out.write("</body></html>");        
        
        is.close();
        os.close();
        huc.disconnect();
        return;
    }
    
    /**
     * Method: saveFile
     * Description: Takes XML from a soap request and saves it to a local file on the system.
     * Should also bakckup and timestamp the previous file if it exists.
     * @param req
     * @param out
     * @throws IOException
     */
    private void saveFile(HttpServletRequest req, Writer out) throws IOException {
        String xmlString = getXML(req,out);   
        try {
            System.out.println("this will hopefully be saved to " + outputURL + " data = " + xmlString);
            File saveFile = new File(new URI(outputURL));
            File renameFile = new File(new URI(outputURL +".backup_" + System.currentTimeMillis()));
            boolean success = saveFile.renameTo(renameFile);
            String htmlString = null;
            if(!success) {
                htmlString = "<html><head><title>File Save Error</title></head><body><p>" +
                "Could not rename the original file to " + renameFile.toString() + "<br />No File was written, exiting.</p></body></html>";
            } else {
                FileWriter fw = new FileWriter(saveFile);
                fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                fw.write(xmlString);
                fw.flush();
                fw.close();
                htmlString = "<html><head><title>File Saved</title></head>" +
                "<body><p>The XML shown below has been saved on the local file system. (Being HTML you may need to do a View source to see xml completely)<br /> " + 
                " Click <a href='" + homeURL + "'>here</a> to go back home.<br />" +
                xmlString + "</body></html>";
                out.write(htmlString);
            }
        }catch(URISyntaxException us)  {
            us.printStackTrace();
        }
    }
    
    /**
     * Method: errorHTMLString
     * Description: Basic error html page to the user.
     * @return
     */
    private String errorHTMLString() {
        return "<html><head><title>Error</title></head><body>" +
        "<p>Information has been logged for the sys admin to take a look at the problem.<br />" +
        "Back to Home: <a href='" + homeURL + "'>home</a></p></body></html>";
    }
    
    /**
     * Method: makeGenericXFormPage
     * Description: Creates a generic html page to show all the various xforms they can submit for processing.
     * Will load all the various xforms types and allow them to choose which type they like which will call back to this
     * servlet and display xforms (with an empty xforms instance document). 
     * @return
     */
    private String makeGenericXFormPage() {
        String htmlString = "<html><head><title>Xforms</title></head><body";
        htmlString += "<p>Welcome this is Generic XForms processor page.  Here you will find all possible Xforms";
        htmlString += " available via Astrogrid.<br />  Choose a Type and hit submit or coming soon will be the option to choose a type with a";
        htmlString += " loaded XML document on your system.<br /></p>";        
        htmlString += " Here is a link back to your home if needed: <a href='" + homeURL + "'>home</a><br /><p>";        
        htmlString += "<p><form mehod='post'><br /><select name='xforms.type'>";
        Enumeration keyEnum = props.keys();
        String tempType = null;
        while(keyEnum.hasMoreElements()) {
            tempType = (String)keyEnum.nextElement();
            if(tempType.startsWith("xforms.type")) {
                htmlString += "<option value='" + tempType + "'>" + tempType.substring("xforms.type".length() + 1) + "</option>"; 
            }
        }//while
        htmlString += "</select><br /><input type='submit' value='Process'></form><br /><br /></p></body></html>";
        return htmlString;
    }
}