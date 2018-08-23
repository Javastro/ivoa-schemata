/*
 * Created on 23 Aug 2018 
 * Copyright 2018 Paul Harrison (paul.harrison@manchester.ac.uk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in file LICENSE
 */ 

package org.javastro.ivoa.schema;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that there is a schema for each of the standard namespaces .
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 
 * @since 23 Aug 2018
 */
public class NamespaceSchemaTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSchemaPresent() {
        for (String namespace : Namespaces.getNamespaceURIs()) {
           URL mapUrl = SchemaMap.getSchemaURL(namespace);
           if(!namespace.contains("www.w3.org")) // don't necessarily expect these...
             assertNotNull(namespace+" should have an associated schema", mapUrl);
        }
    }

}


