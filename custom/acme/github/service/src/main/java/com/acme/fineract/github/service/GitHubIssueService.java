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

import com.acme.fineract.github.config.GitHubProperties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubIssueService {

    private static final String GITHUB_API_BASE_URL = "https://api.github.com";

    private final RestTemplate gitHubRestTemplate;
    private final GitHubProperties gitHubProperties;

    public List<GitHubIssueData> listAllIssues() {
        List<GitHubIssueData> allIssues = new ArrayList<>();
        int page = 1;
        int perPage = 100;

        while (true) {
            String url = String.format("%s/repos/%s/%s/issues?state=all&per_page=%d&page=%d", GITHUB_API_BASE_URL,
                    gitHubProperties.getRepoOwner(), gitHubProperties.getRepoName(), perPage, page);
            log.debug("Fetching GitHub issues from: {}", url);

            GitHubIssueData[] issues = gitHubRestTemplate.getForObject(url, GitHubIssueData[].class);

            if (issues == null || issues.length == 0) {
                break;
            }

            allIssues.addAll(Arrays.asList(issues));

            if (issues.length < perPage) {
                break;
            }

            page++;
        }

        log.info("Fetched {} issues from GitHub repository {}/{}", allIssues.size(), gitHubProperties.getRepoOwner(),
                gitHubProperties.getRepoName());
        return allIssues;
    }
}
