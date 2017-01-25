package com.thomascook.ids.sapbw;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thomascook.ids.sapbw.client.SapBwUriFactory;
import com.thomascook.ids.sapbw.healthcheck.SapBwHealthCheck;
import com.thomascook.status.client.MsbStatusClient;
import com.typesafe.config.Config;
import io.github.tcdl.msb.api.MessageTemplate;
import io.github.tcdl.msb.api.MsbContext;
import io.github.tcdl.msb.api.MsbContextBuilder;
import org.apache.commons.codec.binary.Base64;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;

import static com.thomascook.status.client.reporter.ConfigReportingUtil.configToMap;


/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@Configuration
public class SapBwConfiguration {

    private Config config = SapBwApplication.CONFIG;

    @Bean
    public Config getConfig() {
        return config;
    }

    @Bean
    public MsbContext getMsbContext() {
        return new MsbContextBuilder()
                .enableShutdownHook(true)
                .withConfig(config)
                .build();
    }

    @Bean
    public MessageTemplate getMessageTemplate() {
        return new MessageTemplate();
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper()
                .enable(SerializationFeature.WRAP_ROOT_VALUE)
                .enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
                .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
    }

    @Bean
    public HealthCheckRegistry healthCheckRegistry() {
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("SAP-BW status", new SapBwHealthCheck());
        return registry;
    }

    @Bean
    public MsbStatusClient msbStatusClient(MsbContext msbContext, HealthCheckRegistry registry) {
        return new MsbStatusClient(registry, msbContext, configToMap(config));
    }

    @Bean
    public SapBwUriFactory getUriFactory() {
        return new SapBwUriFactory(config);
    }

    @Bean
    public RestTemplate getRestTemplate(ClientHttpRequestInterceptor authenticationInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(authenticationInterceptor));
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor getInterceptor() {
        return (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            String userName = config.getString("sap-bw-de.userName");
            String password = config.getString("sap-bw-de.password");
            String plainCredentials = new StringBuilder()
                    .append(userName)
                    .append(":")
                    .append(password)
                    .toString();
            String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
            headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64Credentials);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
            return execution.execute(request, body);
        };
    }
}
