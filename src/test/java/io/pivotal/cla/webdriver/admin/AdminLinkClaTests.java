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
package io.pivotal.cla.webdriver.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openqa.selenium.WebDriver;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.htmlunit.webdriver.MockMvcHtmlUnitDriverBuilder;

import io.pivotal.cla.data.AccessToken;
import io.pivotal.cla.data.User;
import io.pivotal.cla.security.WithAdminUser;
import io.pivotal.cla.security.WithSigningUser;
import io.pivotal.cla.service.ContributingUrlsResponse;
import io.pivotal.cla.service.CreatePullRequestHookRequest;
import io.pivotal.cla.webdriver.BaseWebDriverTests;
import io.pivotal.cla.webdriver.pages.HomePage;
import io.pivotal.cla.webdriver.pages.admin.AdminLinkClaPage;

@WithAdminUser
public class AdminLinkClaTests extends BaseWebDriverTests {

	@Before
	public void claFormData() throws Exception {
		when(mockClaRepository.findByPrimaryTrue()).thenReturn(Arrays.asList(cla,cla));
		when(mockGithub.findRepositoryNames(anyString())).thenReturn(Arrays.asList("test/this"));
	}

	@Test
	public void navigateToLinkCla() {
		HomePage homePage = HomePage.go(driver);
		AdminLinkClaPage link = homePage.link();
		link.assertAt();

		link.assertClaName().hasOptionTexts(cla.getName());
	}

	@Test
	public void linkClaValidationBothRequired() throws Exception {
		AdminLinkClaPage link = AdminLinkClaPage.to(getDriver());

		link = link.link("", "", AdminLinkClaPage.class);

		link.assertRepositories().hasRequiredError();
		link.assertClaName().hasRequiredError();

		// populates options after validation error
		link.assertClaName().hasOptionTexts(cla.getName());
	}

	@Test
	public void linkClaValidationLicenseNameRequired() throws Exception {
		AdminLinkClaPage link = AdminLinkClaPage.to(getDriver());

		link = link.link("test/this", "", AdminLinkClaPage.class);

		link.assertRepositories().hasNoErrors().hasValue("test/this");

		link.assertClaName().hasRequiredError();
	}

	@Test
	public void linkClaValidationRepositoriesRequired() throws Exception {
		AdminLinkClaPage link = AdminLinkClaPage.to(getDriver());

		link = link.link("", cla.getName(), AdminLinkClaPage.class);

		link.assertRepositories().hasRequiredError();
		link.assertClaName().hasNoErrors().hasValue(cla.getName());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void linkClaValidationRepositories() throws Exception {
		AccessToken token = new AccessToken(AccessToken.CLA_ACCESS_TOKEN_ID, "linkClaValidationRepositories_access_token_abc123");
		when(mockTokenRepo.findOne(AccessToken.CLA_ACCESS_TOKEN_ID)).thenReturn(token);
		when(mockGithub.getContributingUrls(anyList())).thenReturn(new ContributingUrlsResponse());
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		AdminLinkClaPage link = AdminLinkClaPage.to(getDriver());

		link = link.link("test/this", cla.getName(), AdminLinkClaPage.class);

		link.assertRepositories().hasNoErrors();
		link.assertClaName().hasNoErrors();

		ArgumentCaptor<CreatePullRequestHookRequest> requestCaptor = ArgumentCaptor.forClass(CreatePullRequestHookRequest.class);
		verify(mockGithub).createPullRequestHooks(requestCaptor.capture());
		CreatePullRequestHookRequest request = requestCaptor.getValue();
		assertThat(request.getAccessToken()).isEqualTo(user.getAccessToken());
		assertThat(request.getRepositoryIds()).containsOnly("test/this");
		assertThat(request.getGithubEventUrl()).isEqualTo("http://localhost/github/hooks/pull_request/"+cla.getName());
		assertThat(request.getSecret()).isEqualTo(token.getToken());
		assertThat(driver.getPageSource()).doesNotContain(token.getToken());
	}

	@WithSigningUser
	@Test
	public void methodSecurity() {
		WebDriver driver = MockMvcHtmlUnitDriverBuilder.webAppContextSetup(wac).build();

		assertThatThrownBy(() -> { AdminLinkClaPage.to(driver); }).hasRootCauseExactlyInstanceOf(AccessDeniedException.class);
	}
}
