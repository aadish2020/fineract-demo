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
package com.acme.fineract.github.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubIssueData {

    private Long id;
    private Integer number;
    private String title;
    private String body;
    private String state;
    private String url;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private UserData user;
    private List<LabelData> labels;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserData {

        private Long id;
        private String login;

        @JsonProperty("html_url")
        private String htmlUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LabelData {

        private Long id;
        private String name;
        private String color;
        private String description;
    }
}
