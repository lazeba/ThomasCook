package com.thomascook.ids.sapbw.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SapBwHealthCheck extends HealthCheck {

    @Autowired
    private ODataCommunicationClient oDataCommunicationClient;

    @Override
    protected Result check() throws Exception {
        try {
            oDataCommunicationClient.ping();
            return Result.healthy();
        } catch (Exception e) {
            return Result.unhealthy("SAP-BW ping fail: " + e.getMessage());
        }
    }
}