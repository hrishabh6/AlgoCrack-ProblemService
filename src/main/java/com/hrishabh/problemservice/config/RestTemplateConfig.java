package com.hrishabh.problemservice.config;

import com.hrishabh.problemservice.logging.RequestContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            String requestId = RequestContext.getRequestId();
            if (requestId != null) {
                request.getHeaders().set(RequestContext.REQUEST_ID_HEADER, requestId);
            }
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
