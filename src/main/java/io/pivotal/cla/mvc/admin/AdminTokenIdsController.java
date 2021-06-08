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

package io.pivotal.cla.mvc.admin;

import io.pivotal.cla.data.repository.AccessTokenRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController

@Controller
@PreAuthorize("hasRole('CLA_AUTHOR')")
public class AdminTokenIdsController {
	final AccessTokenRepository accessTokenRepository;

	public AdminTokenIdsController(AccessTokenRepository accessTokenRepository) {
		this.accessTokenRepository = accessTokenRepository;
	}

	@GetMapping("/admin/token/ids")
	Iterable<String> tokenIds() {
		List<String> result = new ArrayList<>();
		this.accessTokenRepository.findAll().forEach(token -> result.add(token.getId()));
		return result;
	}
}
