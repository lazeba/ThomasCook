package com.thomascook.ids.sapbw.service;


import com.typesafe.config.ConfigFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

public class SourceCodeResolverTest {

    static Map<String, String> EXPECTED_MAPPINGS = new HashMap<>();
    static {
        EXPECTED_MAPPINGS.put("01", "1");
        EXPECTED_MAPPINGS.put("10", "1");
        EXPECTED_MAPPINGS.put("13", "4");
        EXPECTED_MAPPINGS.put("15", "25");
        EXPECTED_MAPPINGS.put("19", "32");
        EXPECTED_MAPPINGS.put("22", "3");
        EXPECTED_MAPPINGS.put("25", "19");
        EXPECTED_MAPPINGS.put("29", "1");
        EXPECTED_MAPPINGS.put("34", "33");
        EXPECTED_MAPPINGS.put("41", "41");
        EXPECTED_MAPPINGS.put("42", "27");
        EXPECTED_MAPPINGS.put("48", "1");
        EXPECTED_MAPPINGS.put("49", "39");
        EXPECTED_MAPPINGS.put("70", "4");
        EXPECTED_MAPPINGS.put("71", "4");
        EXPECTED_MAPPINGS.put("72", "4");
    }

    SourceCodeResolver sourceCodeResolver = new SourceCodeResolver(ConfigFactory.load());

    @Test
    public void resolveSuccess() {
        for (String tourOperatorCode : EXPECTED_MAPPINGS.keySet()) {
            Optional<String> sourceCode = sourceCodeResolver.resolve(tourOperatorCode);
            assertTrue(sourceCode.isPresent());
            assertEquals(EXPECTED_MAPPINGS.get(tourOperatorCode), sourceCode.get());
        }
    }

    @Test
    public void resolveFail() {
        assertFalse(sourceCodeResolver.resolve("UNKNOWN").isPresent());
    }
}
