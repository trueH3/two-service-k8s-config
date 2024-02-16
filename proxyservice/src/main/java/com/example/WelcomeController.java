package com.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@Slf4j
public class WelcomeController {
    @Value("${baseurl}")
    private String baseUrl;

    @Value("${value-from-cm}")
    private String valFromCm;

    @GetMapping("/test")
    public String getHello() throws URISyntaxException {
        log.info("in proxy controller");
        // please visit application.properties for the explanation for this replace
        final var formattedUrl = baseUrl.replace("tcp", "http");
        log.info("formated url:" + formattedUrl);
        final var restTemplate = new RestTemplate();
        final var responseFromInternalController = restTemplate.getForObject(new URI(formattedUrl + "/internal/test"), String.class);

        return responseFromInternalController + valFromCm;
    }
}
