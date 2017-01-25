package com.thomascook.ids.sapbw.lifecycle;

import com.thomascook.ids.sapbw.SapBwApplication;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

/**
 * Created by Sergii Meleshko on 1/10/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public class SapBwApplicationEnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>{
    private static final Logger LOG = LoggerFactory.getLogger(SapBwApplicationEnvironmentPreparedEventListener.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent) {
        LOG.info("Starting customer-sapbw-enrich-canonical-booking-microservice...");
        Config maskedConfig = SapBwApplication.CONFIG
                .withValue("msbConfig.brokerConfig.password", ConfigValueFactory.fromAnyRef("xxx"));
        LOG.info("Using configuration: {}", maskedConfig.root().render());
    }
}
