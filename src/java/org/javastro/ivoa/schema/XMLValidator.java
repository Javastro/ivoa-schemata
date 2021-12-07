/*
 * Created on 9 Jul 2021 
 * Copyright 2021 Paul Harrison (paul.harrison@manchester.ac.uk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in file LICENSE
 */ 

package org.javastro.ivoa.schema;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Run XML validation against the locally stored IVOA schema.
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 
 * @since 9 Jul 2021
 */
public class XMLValidator {

    /** logger for this class */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
            .getLogger(XMLValidator.class);
    private SimpleErrorHandler errorHandler = new SimpleErrorHandler();
    private Map<ErrorKind, List<ErrorDesciption>> errorMap;
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        XMLValidator validator = new XMLValidator();
        if (args.length >= 1) {
            boolean overallValid = true;
            for (String arg : args) {
                boolean valid = validator.validate(new File(arg));
                if (!valid) {
                    System.out.println(arg);
                    validator.printErrors(System.out);
                }
                overallValid |= valid;
            }
            System.exit(overallValid ? 0 : 1);
        } else {
            System.err
                    .println("Should specify filename(s) of file(S) to be validated");
            System.exit(1);
        }

    }
 
    public boolean validate(File file)
    {
        return validate(new StreamSource(file));
    }
    
    public boolean validate(URL url)
    {
        
        try {
            final StreamSource src = new StreamSource(url.openStream());
            return validate(src);
        } catch (IOException e) {
            logger.error("failed to open url", e);
            return false;
        }
    }
    public boolean validate(Source xmlFile) 
    {
        SchemaFactory schemaFactory = SchemaFactory
                                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
  
            Validator validator = null;
            try {
                validator = schemaFactory.newSchema().newValidator();;
            } catch (SAXException e) {
                logger.error("cannot create validator", e);
                return false;
            }
            validator.setResourceResolver(new SchemaResolver());
            
            errorMap = new HashMap<>();
            validator.setErrorHandler(errorHandler);
            
            
            try {
                validator.validate(xmlFile);
                return errorMap.isEmpty();

            } catch (SAXException e) {
              ErrorDesciption d = new ErrorDesciption(e);
              put(errorMap, d.kind, d);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false; // something went wrong if it gets to here.
           
     }
    
     public boolean wasValid()
     {
         return errorMap.isEmpty();
     }
    
     public void printErrors(PrintStream printStream) {
         errorMap.forEach((kind, errors) -> {
             errors.stream().forEach(printStream::println);
         });
     }
  
    /**
     *  .
     * @author Paul Harrison (paul.harrison@manchester.ac.uk) 
     * @since 18 Aug 2021
     */
    public static class SchemaResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI,
                             String publicId, String systemId, String baseURI) {
            if(type.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI) && namespaceURI != null)
            {
            return SchemaMap.getSchemaLSInput(namespaceURI); 
            }
            else {
                logger.trace("looking up something other than expected {} {}",type,namespaceURI);
                return null;
            }
        }
    }

    public enum ErrorKind {
        Warning,
        Error,
        FatalError,
        Sax
    }
    public class ErrorDesciption {
         String desc;
         ErrorKind kind;
         int line;
         int column;
        public ErrorDesciption(ErrorKind kind, SAXParseException e) {
            this.desc = e.getMessage();
            this.kind = kind;
            this.line = e.getLineNumber();
            this.column = e.getColumnNumber();
        }
        public ErrorDesciption(SAXException e)
        {
            this.desc = e.getMessage();
            this.kind = ErrorKind.Sax;
            this.line = 0;
            this.column = 0;
        }
        /**
         * {@inheritDoc}
         * overrides @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Validation ");
            builder.append(kind);
            builder.append(" ").append(desc);
            if(line> 0) {
            builder.append(" line=");
            builder.append(line);
            if (column > 0) {
            builder.append(", column=");
            builder.append(column);
            }
            }
            return builder.toString();
        }
     
        
        
    }
    
    private class SimpleErrorHandler implements ErrorHandler
    {
        
        /**
         * {@inheritDoc}
         * overrides @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override
        public void warning(SAXParseException exception) throws SAXException {
          final ErrorKind kind = ErrorKind.Warning;
          final  ErrorDesciption err = new ErrorDesciption(kind, exception);
          logger.trace(err.toString());
          put(errorMap,kind, err);
          
        }

        /**
         * {@inheritDoc}
         * overrides @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException exception) throws SAXException {
          final ErrorKind kind = ErrorKind.Warning;
          final  ErrorDesciption err = new ErrorDesciption(kind, exception);
          logger.trace(err.toString());
          put(errorMap,kind, err);
            
        }

        /**
         * {@inheritDoc}
         * overrides @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException exception)
                throws SAXException {
          final ErrorKind kind = ErrorKind.Warning;
          final  ErrorDesciption err = new ErrorDesciption(kind, exception);
          logger.trace(err.toString());
          put(errorMap,kind, err);
        }
        
        
    }
    
   private static  <KEY, VALUE > void put (Map<KEY, List<VALUE>> map, KEY key, VALUE value) {
       map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
   }
   

}


