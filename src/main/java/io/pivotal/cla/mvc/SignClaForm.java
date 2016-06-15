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
package io.pivotal.cla.mvc;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper=true)
public class SignClaForm extends ClaRequest {

	@NotNull
	Long claId;

	@NotEmpty(message = "This is required")
	String name;

	@NotEmpty(message = "This is required")
	String email;

	@NotEmpty(message = "This is required")
	String mailingAddress;

	@NotEmpty(message = "This is required")
	String country;

	@NotEmpty(message = "This is required")
	String telephone;

	@AssertTrue(message = "This is required")
	Boolean confirm;

	boolean signed;


}
