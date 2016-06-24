/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cla.service.github;

import java.util.List;
import java.util.Set;

import io.pivotal.cla.data.User;
import io.pivotal.cla.service.MigratePullRequestStatusRequest;

public interface GitHubApi {

	List<PullRequestStatus> createUpdatePullRequestStatuses(MigratePullRequestStatusRequest request);

	/**
	 * Gets the sha hash for a particular pull request. If
	 * {@link PullRequestStatus#getGitHubUsername()} does not match the user who
	 * submitted the pull request, then null is returned.
	 *
	 * @param commitStatus
	 * @return
	 */
	String getShaForPullRequest(PullRequestStatus commitStatus);

	void save(PullRequestStatus status);

	List<String> findRepositoryNames(String accessToken);

	List<String> createPullRequestHooks(CreatePullRequestHookRequest request);

	User getCurrentUser(CurrentUserRequest request);

	Set<String> getVerifiedEmails(String accessToken);

	List<String> getOrganizations(String username);

	ContributingUrlsResponse getContributingUrls(List<String> repositoryIds);

	String markdownToHtml(String accessToken, String markdown);

	/**
	 * Returns a set of CLA names associated with the repository.
	 *
	 * @param repoId
	 * @param accessToken
	 * @return
	 */
	Set<String> findAssociatedClaNames(String repoId, String accessToken);
}
