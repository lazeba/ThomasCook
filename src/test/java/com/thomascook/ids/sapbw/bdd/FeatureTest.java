package com.thomascook.ids.sapbw.bdd;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Cucumber.class)
@CucumberOptions(
        glue = {"com.thomascook.ids.sapbw.bdd.steps"},
        features = {"classpath:scenarios/"},
        format = {"pretty", "html:target/html/"},
        strict = true
)
public class FeatureTest {

    static final Logger LOG = LoggerFactory.getLogger(FeatureTest.class);

    public FeatureTest() {
        LOG.debug("Starting test");
    }
}

