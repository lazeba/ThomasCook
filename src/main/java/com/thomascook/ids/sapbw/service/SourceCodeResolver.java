package com.thomascook.ids.sapbw.service;


import com.typesafe.config.Config;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.github.tcdl.msb.config.ConfigurationUtil.getOptionalString;

/**
 * Resolves Source system code by Tour operator code
 */
@Component
public class SourceCodeResolver {

    private final static String MAPPING_CONFIG = "sourceCodeMapping";

    private Config mappings;

    public SourceCodeResolver(Config config) {
        this.mappings = config.getConfig(MAPPING_CONFIG);
    }

    public Optional<String> resolve(String tourOperatorCode) {
        return getOptionalString(mappings, tourOperatorCode);
    }
}