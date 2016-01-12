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
package io.pivotal.cla.webdriver.pages.admin;

import static org.assertj.core.api.Assertions.assertThat;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import io.pivotal.cla.webdriver.pages.BasePage;

public class AdminLinkClaPage extends BasePage {
	WebElement submit;

	WebElement repositories;

	WebElement claName;

	public AdminLinkClaPage(WebDriver driver) {
		super(driver);
	}

	public SelectAssert assertClaName() {
		return assertSelect(claName);
	}

	public InputAssert assertRepositories() {
		return assertInput(repositories);
	}

	public <T extends BasePage> T link(String repositoryName, String licenseName, Class<T> page) {
		Select cla = new Select(claName);

		repositories.sendKeys(repositoryName);
		cla.selectByVisibleText(licenseName);
		submit.click();
		return PageFactory.initElements(getDriver(), page);
	}

	public void assertAt() {
		assertThat(getDriver().getTitle()).endsWith("Link to CLA");
	}

	public static AdminLinkClaPage to(WebDriver driver) {
		get(driver, "/admin/cla/link");
		return PageFactory.initElements(driver, AdminLinkClaPage.class);
	}
}
