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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlresolver.ResolverFeature;
import org.xmlresolver.XMLResolver;
import org.xmlresolver.XMLResolverConfiguration;
import org.xmlresolver.catalog.entry.EntryCatalog;

/**
 * Run XML validation against the locally stored IVOA Registry schema.
 * 
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
                boolean valid;
                try {
                    valid = validator.validate(new File(arg));
                } catch (FileNotFoundException e) {
                    valid = false;
                    e.printStackTrace();
                }
                if (!valid) {
                    System.out.println(arg);
                    validator.printErrors(System.out);
                }
                overallValid |= valid;
            }
            System.exit(overallValid ? 0 : 1);
        } else {
            System.err.println(
                    "Should specify filename(s) of file(S) to be validated");
            System.exit(1);
        }

    }

    public boolean validate(File file) throws FileNotFoundException {
        return validate(new StreamSource(file));
    }

    public boolean validate(URL url) {

           try {
            final StreamSource src = new StreamSource(url.openStream());
            return validate(src);
        } catch (IOException e) {
            logger.error("failed to open url", e);
            return false;
        }

    }

    public boolean validate(Source xmlFile) {
        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
       
        final XMLResolver xmlResolver = makeXMLResolver();
        schemaFactory.setResourceResolver(new LSResourceAdapter(xmlResolver));
           Validator validator = null;
            try {
                schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
                Schema schema = schemaFactory.newSchema(SchemaMap.getRegistrySchemaAsSources()); //TODO just allow against all the schema?
                validator = schema.newValidator();
            } catch (SAXException e) {
                logger.error("cannot create validator", e);
                return false;
            }
            validator.setResourceResolver(new LSResourceAdapter(xmlResolver));
            
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

 

    public boolean wasValid() {
        return errorMap.isEmpty();
    }

    public void printErrors(PrintStream printStream) {
        errorMap.forEach((kind, errors) -> {
            errors.stream().forEach(printStream::println);
        });
    }


    public enum ErrorKind {
        Warning, Error, FatalError, Sax
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

        public ErrorDesciption(SAXException e) {
            this.desc = e.getMessage();
            this.kind = ErrorKind.Sax;
            this.line = 0;
            this.column = 0;
        }

        /**
         * {@inheritDoc} overrides @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Validation ");
            builder.append(kind);
            builder.append(" ").append(desc);
            if (line > 0) {
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

    private class SimpleErrorHandler implements ErrorHandler {

        /**
         * {@inheritDoc} overrides @see
         * org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override
        public void warning(SAXParseException exception) throws SAXException {
            final ErrorKind kind = ErrorKind.Warning;
            final ErrorDesciption err = new ErrorDesciption(kind, exception);
            logger.trace(err.toString());
            put(errorMap, kind, err);

        }

        /**
         * {@inheritDoc} overrides @see
         * org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException exception) throws SAXException {
            final ErrorKind kind = ErrorKind.Warning;
            final ErrorDesciption err = new ErrorDesciption(kind, exception);
            logger.trace(err.toString());
            put(errorMap, kind, err);

        }

        /**
         * {@inheritDoc} overrides @see
         * org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException exception)
                throws SAXException {
            final ErrorKind kind = ErrorKind.Warning;
            final ErrorDesciption err = new ErrorDesciption(kind, exception);
            logger.trace(err.toString());
            put(errorMap, kind, err);
        }

    }

    public static XMLResolver makeXMLResolver() {
        XMLResolverConfiguration config = new XMLResolverConfiguration();
        config.setFeature(ResolverFeature.DEFAULT_LOGGER_LOG_LEVEL, "info");
        config.setFeature(ResolverFeature.ACCESS_EXTERNAL_DOCUMENT, "");
        config.setFeature(ResolverFeature.THROW_URI_EXCEPTIONS, true);
        config.setFeature(ResolverFeature.ALWAYS_RESOLVE, false);
        config.setFeature(ResolverFeature.PREFER_PUBLIC, false);
        config.setFeature(ResolverFeature.CLASSPATH_CATALOGS, true);
        
        org.xmlresolver.CatalogManager manager = config
                .getFeature(ResolverFeature.CATALOG_MANAGER);
        URI caturi = URI.create("https://javastro.net/xml/catalog.xml");//IMPL - not sure is this should be more obviously false.
        config.addCatalog(caturi.toString());
        EntryCatalog cat = manager.loadCatalog(caturi, SchemaMap.asXMLCatalogue());
       try {
          manager.loadCatalog(XMLResolver.class.getResource("/org/xmlresolver/catalog.xml").toURI());
       } catch (URISyntaxException e) {
          throw new RuntimeException(e);
       }

       XMLResolver resolver = new XMLResolver(config);
        return resolver;
    }

    private static <KEY, VALUE> void put(Map<KEY, List<VALUE>> map, KEY key,
            VALUE value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

}
