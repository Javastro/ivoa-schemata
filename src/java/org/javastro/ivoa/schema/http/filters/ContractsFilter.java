package org.javastro.ivoa.schema.http.filters;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.Attributes;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Class: ContractsFilter
 * Description: This is a filter class that you can define in your web.xml.  Below is an example of what to be placed 
 * in the web.xml.  This filter will get wsdl and xsd files out of the contracts (jar resource).  It will then
 * parse the schema's or wsdl's and change schemaLocations to be coordinated with the local webapp.  It will
 * also change soap binding urls.  And a final note is you can define other config parameters in the web.xml
 * to handle things such as proxying.  Only required parameter is the wsdlFile.
 * Example of what to be placed in web.xml:
 *     <filter>
        <filter-name>ContractsFilter</filter-name>
        <filter-class>org.astrogrid.contracts.http.filters.ContractsFilter</filter-class>
         <init-param>
            <param-name>wsdlFile</param-name>
            <param-value>schema/registry/RegistrySearch/v0.1/RegistrySearch.wsdl</param-value>
         </init-param>        
        <!--
           Optional init-param(s), you may wish to use this for proxy type servicing to use your proxy
           settings for obtaining wsdls and schemas even if they come into your wsdls or schemas from a
           different url.  Since this filter uses the request urls, I suspect this will not need to be set.
            <init-param>
                <param-name>contractsURL</param-name>
                <param-value>http://server:port/contextpath</param-value>
            </init-param>
        -->
    </filter>
    <!--
    Hmmm be njcie to actually see if I can set it to the actual service?wsdl
    -->
    <filter-mapping>
      <filter-name>ContractsFilter</filter-name>
      <url-pattern>/services/*</url-pattern>
    </filter-mapping>

    <filter-mapping>
      <filter-name>ContractsFilter</filter-name>
      <url-pattern>/schema/*</url-pattern>
    </filter-mapping>
    

 * @author Kevin Benson
 *
 */
public final class ContractsFilter implements Filter {


    private FilterConfig filterConfig = null;
    
    private static String contextURL = null;
        
    public static String getContextURL() {
        return contextURL;
    }
    

    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
	throws IOException, ServletException {
      
      String schemaFile = filterConfig.getInitParameter("wsdlFile");
      
      //lets make sure we  have a reference to a wsdl and we are dealing with a wsdl or xsd file
      if(schemaFile == null || ((HttpServletRequest) request).getServletPath() == null) {
          chain.doFilter(request, response);
          return;
      }
      
      if( ((HttpServletRequest) request).getQueryString() == null &&
          !((HttpServletRequest) request).getServletPath().endsWith("wsdl") && 
    	  !((HttpServletRequest) request).getServletPath().endsWith("xsd")) {
          chain.doFilter(request, response);
          return;
      }
      
      if( ((HttpServletRequest) request).getQueryString() != null &&
  		  !((HttpServletRequest) request).getQueryString().endsWith("wsdl") ) {
              chain.doFilter(request, response);
              return;
          }
  		  
      
      //okay if we are dealing with a wsdl or xsd (not from axis "?wsdl") then
      //lets strip it out things down to the /schema area.
      if( ((HttpServletRequest) request).getServletPath().endsWith("wsdl") || 
          ((HttpServletRequest) request).getServletPath().endsWith("xsd") ) {
         schemaFile = ((HttpServletRequest) request).getServletPath().substring(((HttpServletRequest) request).getServletPath().indexOf("schema"));         
      }
      
      //okay get the url to use for referencing schemas and wsdls
      //might be overridden by a config property.
      if(contextURL == null) {
          contextURL =  filterConfig.getInitParameter("contractsURL") != null ? 
                  filterConfig.getInitParameter("contractsURL") : 
                  request.getScheme() + "://" + request.getServerName() + ":" +
                  request.getServerPort() + ((HttpServletRequest) request).getContextPath();
      }
      
      

      //get the current directory of the schema. Used to handle import locations that are in the same 
      //directory.
      String currentURLSchemaBase = null;
      if( ((HttpServletRequest) request).getQueryString() != null &&
          ((HttpServletRequest) request).getQueryString().endsWith("wsdl") ) {
          currentURLSchemaBase = (contextURL + "/" +  schemaFile).substring(0,(contextURL + "/" + schemaFile).lastIndexOf("/"));
      }else {
          currentURLSchemaBase = contextURL + ((HttpServletRequest) request).getServletPath().substring(0,((HttpServletRequest) request).getServletPath().lastIndexOf("/")); 
      }
      
      String addressInfo = contextURL + ((HttpServletRequest) request).getServletPath()  + ((HttpServletRequest) request).getPathInfo();
      try {
          //set the xml
          response.setContentType("text/xml");
          //get the writer
          PrintWriter out = response.getWriter();
          
          //start a contracthandler sax events to send out to the client.
          XMLReader myReader = XMLReaderFactory.createXMLReader();
          myReader.setContentHandler(new ContractHandler(out, contextURL, currentURLSchemaBase, addressInfo));
          InputStream is  = this.getClass().getClassLoader().getResourceAsStream(schemaFile);
          if(is == null) {
              //could not find this resource (it's not in the contracts.jar)
              //let it chain down, nothing we can do
              chain.doFilter(request, response);
              return;
          }
          myReader.parse(new InputSource(is));          
        } catch (SAXException e) {
          System.err.println(e.getMessage());
        }

      //chain.doFilter(request, response);
    }


    public void destroy() {
    }


    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
    
    class ContractHandler extends DefaultHandler {
        
        private PrintWriter out = null;
        private String urlString = null;
        private String currentURLSchemaBase = null;
        private String addressURL = null;
        /**
         * ContractHandler to send xml schemas and wsdls to the client.
         * @param out
         * @param urlString
         * @param currentURLSchemaBase
         */
        public ContractHandler(PrintWriter out, String urlString, String currentURLSchemaBase, String addressURL) {
            this.out = out;
            this.urlString = urlString;
            this.currentURLSchemaBase = currentURLSchemaBase;
            this.addressURL = addressURL;
        }
        
        private String definedNS = "";
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
          String elem = "<" + qName + " ";
          String actualLocation = null;
          for(int i = 0;i < attributes.getLength();i++) {
              if( ("import".equals(localName) || "include".equals(localName)) && 
                  ("location".equals(attributes.getQName(i)) || "schemaLocation".equals(attributes.getQName(i)))   ) {
                  //okay its an import with a location or schemalocation
                  char []loc = attributes.getValue(i).toCharArray();
                  for(int j = 0;j < loc.length;j++) {
                      if(loc[j] != '.' && loc[j] != '/') {
                        actualLocation = String.valueOf(loc,j,(loc.length - j));
                        if(j <= 3) {
                            //ahha the user did not do a ../
                            //means the xsd/wsdl reference is in the same directory. or below.
                            //this should be a rare case in the current way contracts is setup.
                            actualLocation = currentURLSchemaBase.substring(urlString.length()) + "/" + actualLocation;
                        }//if
                        j = loc.length;
                      }//if
                  }//for
                  if(actualLocation.startsWith("schema") || actualLocation.startsWith("/schema")) {
                      elem += attributes.getQName(i) + "=\"" + urlString + actualLocation + "\" ";
                  }else {
                      elem += attributes.getQName(i) + "=\"" + urlString + "/schema/" + actualLocation + "\" ";
                  }//else
              }else if( ("address".equals(localName)) && 
                      ("location".equals(attributes.getQName(i)))   ) {
                  elem += attributes.getQName(i) + "=\"" + addressURL+ "\" ";
              } else {
            	
                  elem += attributes.getQName(i) + "=\"" + attributes.getValue(i).replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("&","&amp;") + "\" ";
              }
          }

          if(definedNS.trim().length() > 0) {
              elem += " " + definedNS;
          }
          
          elem += ">";
          definedNS = "";
          actualLocation = null;
          out.println(elem);
        }
        
        public void endElement(String uri, String localName, String qName) throws SAXException {
            out.println("</" + qName + ">");
        }
        
        public void characters(char[] ch,
                int start,
                int length)
         throws SAXException {
        	//System.out.print(ch);
        	
        	//System.out.println("-->done with characters and start = " + start + " with length = " + length);
        	out.print(String.valueOf(ch,start,length).replaceAll("<","&lt;").replaceAll(">","&gt;").replaceAll("&","&amp;") );
        }
        
        public void startPrefixMapping(String prefix, String uri) {
          if(prefix == null || prefix.trim().length() == 0) {
              definedNS += "xmlns=\"" + uri + "\" ";
          }else {
              definedNS += "xmlns:" + prefix + "=\"" + uri + "\" ";
          }          
        }
    }
}