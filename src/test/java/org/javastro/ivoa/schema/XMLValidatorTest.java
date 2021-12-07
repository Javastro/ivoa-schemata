/*
 * Created on 17 Aug 2021 
 * Copyright 2021 Paul Harrison (paul.harrison@manchester.ac.uk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in file LICENSE
 */ 

package org.javastro.ivoa.schema;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *  .
 * @author Paul Harrison (paul.harrison@manchester.ac.uk) 
 * @since 17 Aug 2021
 */
class XMLValidatorTest {

    /**
     * @throws java.lang.Exception
     */
    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterAll
    static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
    }

    /**
     * Test method for {@link org.javastro.ivoa.schema.XMLValidator#validate(javax.xml.transform.Source)}.
     */
    @Test
    void testValidateSourceOK() {
            XMLValidator v = new XMLValidator();
            boolean result = v.validate(this.getClass().getResource("/VOResource.xml"));
            assertTrue(result);
    }

    @Test
    void testValidateSourceBad() {
            XMLValidator v = new XMLValidator();
            boolean result = v.validate(this.getClass().getResource("/VOResource2.xml"));
            assertTrue(!result);
            v.printErrors(System.out);
    }
    
    
    @Test
    void testValidateRandom() {
            XMLValidator v = new XMLValidator();
            boolean result = v.validate(this.getClass().getResource("/noschemarandom.xml"));
            assertTrue(!result);
            v.printErrors(System.out);
    }

}


