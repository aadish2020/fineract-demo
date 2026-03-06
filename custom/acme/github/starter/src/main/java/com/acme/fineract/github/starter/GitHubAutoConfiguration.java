/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.acme.fineract.github.starter;

import com.acme.fineract.github.config.GitHubProperties;
import java.util.Collections;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ComponentScan(basePackages = { "com.acme.fineract.github" })
@EnableConfigurationProperties(GitHubProperties.class)
public class GitHubAutoConfiguration {

    @Bean
    public RestTemplate gitHubRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ClientHttpRequestInterceptor headersInterceptor = (request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set(HttpHeaders.USER_AGENT, "fineract-demo-github-client");
            headers.set(HttpHeaders.ACCEPT, "application/vnd.github+json");
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            return execution.execute(request, body);
        };
        restTemplate.setInterceptors(Collections.singletonList(headersInterceptor));
        return restTemplate;
    }
}
