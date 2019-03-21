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
package io.pivotal.cla.mvc;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.pivotal.cla.data.ContributorLicenseAgreement;
import io.pivotal.cla.data.CorporateSignature;
import io.pivotal.cla.data.User;
import io.pivotal.cla.data.repository.AccessTokenRepository;
import io.pivotal.cla.data.repository.ContributorLicenseAgreementRepository;
import io.pivotal.cla.data.repository.CorporateSignatureRepository;
import io.pivotal.cla.service.GitHubService;
import io.pivotal.cla.service.UpdatePullRequestStatusRequest;

@Controller
public class CclaController {
	@Autowired
	ContributorLicenseAgreementRepository clas;
	@Autowired
	CorporateSignatureRepository corporate;
	@Autowired
	GitHubService github;
	@Autowired
	AccessTokenRepository tokenRepo;

	@RequestMapping("/sign/{claName}/ccla")
	public String claForm(@AuthenticationPrincipal User user, SignCorporateClaForm signCorporateClaForm,
			Map<String, Object> model) throws Exception {
		String claName = signCorporateClaForm.getClaName();
		Integer pullRequestId = signCorporateClaForm.getPullRequestId();
		String repositoryId = signCorporateClaForm.getRepositoryId();

		List<String> organizations = github.getOrganizations(user.getGithubLogin());
		CorporateSignature signed = corporate.findSignature(claName, organizations, user.getEmails());
		ContributorLicenseAgreement cla = signed == null ? clas.findByNameAndPrimaryTrue(claName) : signed.getCla();

		if(cla == null) {
			throw new ResourceNotFoundException();
		}
		if(cla.getSupersedingCla() != null) {
			cla = cla.getSupersedingCla();
		}

		signCorporateClaForm.setSigned(signed != null);
		signCorporateClaForm.setName(user.getName());
		signCorporateClaForm.setClaId(cla.getId());
		signCorporateClaForm.setRepositoryId(repositoryId);
		signCorporateClaForm.setPullRequestId(pullRequestId);
		signCorporateClaForm.setGitHubOrganizations(github.getOrganizations(user.getGithubLogin()));

		model.put("cla", cla);

		return "cla/ccla/sign";
	}

	@RequestMapping(value = "/sign/{claName}/ccla", method = RequestMethod.POST)
	public String signCla(@AuthenticationPrincipal User user, @Valid SignCorporateClaForm signCorporateClaForm, BindingResult result, Map<String, Object> model, RedirectAttributes redirect) throws Exception {
		if(result.hasErrors()) {
			ContributorLicenseAgreement cla = clas.findOne(signCorporateClaForm.getClaId());
			model.put("cla", cla);
			signCorporateClaForm.setGitHubOrganizations(github.getOrganizations(user.getGithubLogin()));
			return "cla/ccla/sign";
		}
		ContributorLicenseAgreement cla = clas.findOne(signCorporateClaForm.getClaId());
		CorporateSignature signature = new CorporateSignature();
		signature.setCla(cla);
		signature.setEmail(signCorporateClaForm.getEmail());
		signature.setDateOfSignature(new Date());
		signature.setGithubLogin(user.getGithubLogin());
		signature.setGitHubOrganization(signCorporateClaForm.getGitHubOrganization());
		signature.setTitle(signCorporateClaForm.getTitle());
		signature.setCountry(signCorporateClaForm.getCountry());
		signature.setName(signCorporateClaForm.getName());
		signature.setMailingAddress(signCorporateClaForm.getMailingAddress());
		signature.setTelephone(signCorporateClaForm.getTelephone());
		signature.setCompanyName(signCorporateClaForm.getCompanyName());
		corporate.save(signature);

		// update github

		String repositoryId = signCorporateClaForm.getRepositoryId();
		Integer pullRequestId = signCorporateClaForm.getPullRequestId();
		redirect.addAttribute("claName", cla.getName());
		if(repositoryId == null || pullRequestId == null) {
			return "redirect:/sign/{claName}/ccla";
		}

		UpdatePullRequestStatusRequest updatePullRequest = signCorporateClaForm.createUpdatePullRequestStatus(user.getGithubLogin());
		github.save(updatePullRequest);

		redirect.addAttribute("repositoryId", repositoryId);
		redirect.addAttribute("pullRequestId", pullRequestId);

		return "redirect:/sign/{claName}/ccla";
	}
}
