/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.pivotal.cla.mvc.github;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import io.pivotal.cla.data.CorporateSignature;
import io.pivotal.cla.data.IndividualSignature;
import io.pivotal.cla.data.User;
import io.pivotal.cla.data.repository.AccessTokenRepository;
import io.pivotal.cla.data.repository.CorporateSignatureRepository;
import io.pivotal.cla.data.repository.IndividualSignatureRepository;
import io.pivotal.cla.data.repository.UserRepository;
import io.pivotal.cla.egit.github.core.event.RepositoryPullRequestPayload;
import io.pivotal.cla.mvc.util.UrlBuilder;
import io.pivotal.cla.service.CommitStatus;
import io.pivotal.cla.service.GitHubService;

@RestController
@PreAuthorize("@githubSignature.check(#request.getHeader('X-Hub-Signature'), #body)")
public class GithubHooksController {

	@Autowired
	AccessTokenRepository tokenRepo;

	@Autowired
	UserRepository userRepo;

	@Autowired
	IndividualSignatureRepository individualRepo;

	@Autowired
	CorporateSignatureRepository corporate;

	@Autowired
	GitHubService github;

	@RequestMapping(value = "/github/hooks/pull_request/{cla}", headers = "X-GitHub-Event=ping")
	public String pullRequestPing(HttpServletRequest request, @RequestBody String body, @PathVariable String cla) throws Exception {
		return "SUCCESS";
	}

	/**
	 *
	 * @param request
	 * @param body
	 * @param cla
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/github/hooks/pull_request/{cla}")
	public String pullRequest(HttpServletRequest request, @RequestBody String body, @PathVariable String cla) throws Exception {
		Gson gson = GsonUtils.createGson();
		RepositoryPullRequestPayload pullRequestPayload = gson.fromJson(body, RepositoryPullRequestPayload.class);

		Repository repository = pullRequestPayload.getRepository();
		RepositoryId repoId = RepositoryId.createFromId(repository.getOwner().getLogin() + "/" + repository.getName());

		PullRequest pullRequest = pullRequestPayload.getPullRequest();
		String githubLogin = pullRequest.getUser().getLogin();

		User user = userRepo.findOne(githubLogin);
		if(user == null) {
			user = new User();
			user.setGithubLogin(githubLogin);
			user.setEmails(new HashSet<>());
		}

		boolean success = hasSigned(user, cla);

		CommitStatus status = new CommitStatus();
		status.setGithubUsername(githubLogin);
		status.setPullRequestId(pullRequest.getNumber());
		status.setRepoId(repoId.generateId());
		status.setSha(pullRequest.getHead().getSha());
		status.setSuccess(success);
		String signUrl = UrlBuilder.signUrl()
			.request(request)
			.claName(cla)
			.repositoryId(status.getRepoId())
			.pullRequestId(status.getPullRequestId())
			.build();
		status.setUrl(signUrl);

		github.save(status);

		return "FAIL";
	}

	private boolean hasSigned(User user, String claName) throws IOException {
		if(claName == null) {
			return false;
		}
		IndividualSignature signedIndividual = individualRepo.findSignaturesFor(user, claName);

		if(signedIndividual != null) {
			return true;
		}

		List<String> organizations = github.getOrganizations(user.getGithubLogin());
		CorporateSignature corporateSignature = corporate.findSignature(claName, organizations, user.getEmails());
		return corporateSignature != null;
	}
}
