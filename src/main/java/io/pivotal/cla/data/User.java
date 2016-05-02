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
package io.pivotal.cla.data;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
public class User {
	@Id
	@Column(name = "github_login")
	private String githubLogin;

	private String name;

	private String accessToken;

	@JsonProperty("avatar_url")
	private String avatarUrl;

	@ElementCollection
	@Column(name = "email")
	@CollectionTable(name = "user_email",
			joinColumns = @JoinColumn(name = "github_login") )
	private Set<String> emails;

	private transient boolean admin;

	private transient boolean adminAccessRequested;

	private transient boolean isNew;

}
