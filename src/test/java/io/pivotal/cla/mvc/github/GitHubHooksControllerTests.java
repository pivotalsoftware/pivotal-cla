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
package io.pivotal.cla.mvc.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import io.pivotal.cla.data.AccessToken;
import io.pivotal.cla.data.User;
import io.pivotal.cla.security.GitHubSignature;
import io.pivotal.cla.security.WithSigningUserFactory;
import io.pivotal.cla.service.github.PullRequestStatus;
import io.pivotal.cla.webdriver.BaseWebDriverTests;

public class GitHubHooksControllerTests extends BaseWebDriverTests {
	final String PAYLOAD = "{\"action\":\"reopened\",\"number\":2,\"pull_request\":{\"url\":\"https://api.github.com/repos/rwinch/176_test/pulls/2\",\"id\":54991094,\"html_url\":\"https://github.com/rwinch/176_test/pull/2\",\"diff_url\":\"https://github.com/rwinch/176_test/pull/2.diff\",\"patch_url\":\"https://github.com/rwinch/176_test/pull/2.patch\",\"issue_url\":\"https://api.github.com/repos/rwinch/176_test/issues/2\",\"number\":2,\"state\":\"open\",\"locked\":false,\"title\":\"Create a\",\"user\":{\"login\":\"robwinch\",\"id\":11235608,\"avatar_url\":\"https://avatars.githubusercontent.com/u/11235608?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/robwinch\",\"html_url\":\"https://github.com/robwinch\",\"followers_url\":\"https://api.github.com/users/robwinch/followers\",\"following_url\":\"https://api.github.com/users/robwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/robwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/robwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/robwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/robwinch/orgs\",\"repos_url\":\"https://api.github.com/users/robwinch/repos\",\"events_url\":\"https://api.github.com/users/robwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/robwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"body\":\"\",\"created_at\":\"2016-01-04T16:11:27Z\",\"updated_at\":\"2016-01-04T16:53:10Z\",\"closed_at\":null,\"merged_at\":null,\"merge_commit_sha\":\"71ee89465ebc1ffa832da3f354b5aef814be2381\",\"assignee\":null,\"milestone\":null,\"commits_url\":\"https://api.github.com/repos/rwinch/176_test/pulls/2/commits\",\"review_comments_url\":\"https://api.github.com/repos/rwinch/176_test/pulls/2/comments\",\"review_comment_url\":\"https://api.github.com/repos/rwinch/176_test/pulls/comments{/number}\",\"comments_url\":\"https://api.github.com/repos/rwinch/176_test/issues/2/comments\",\"statuses_url\":\"https://api.github.com/repos/rwinch/176_test/statuses/a6befb598a35c1c206e1bf7bbb3018f4403b9610\",\"head\":{\"label\":\"robwinch:master\",\"ref\":\"master\",\"sha\":\"a6befb598a35c1c206e1bf7bbb3018f4403b9610\",\"user\":{\"login\":\"robwinch\",\"id\":11235608,\"avatar_url\":\"https://avatars.githubusercontent.com/u/11235608?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/robwinch\",\"html_url\":\"https://github.com/robwinch\",\"followers_url\":\"https://api.github.com/users/robwinch/followers\",\"following_url\":\"https://api.github.com/users/robwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/robwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/robwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/robwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/robwinch/orgs\",\"repos_url\":\"https://api.github.com/users/robwinch/repos\",\"events_url\":\"https://api.github.com/users/robwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/robwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"repo\":{\"id\":49006662,\"name\":\"176_test\",\"full_name\":\"robwinch/176_test\",\"owner\":{\"login\":\"robwinch\",\"id\":11235608,\"avatar_url\":\"https://avatars.githubusercontent.com/u/11235608?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/robwinch\",\"html_url\":\"https://github.com/robwinch\",\"followers_url\":\"https://api.github.com/users/robwinch/followers\",\"following_url\":\"https://api.github.com/users/robwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/robwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/robwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/robwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/robwinch/orgs\",\"repos_url\":\"https://api.github.com/users/robwinch/repos\",\"events_url\":\"https://api.github.com/users/robwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/robwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"private\":false,\"html_url\":\"https://github.com/robwinch/176_test\",\"description\":\"\",\"fork\":true,\"url\":\"https://api.github.com/repos/robwinch/176_test\",\"forks_url\":\"https://api.github.com/repos/robwinch/176_test/forks\",\"keys_url\":\"https://api.github.com/repos/robwinch/176_test/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/robwinch/176_test/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/robwinch/176_test/teams\",\"hooks_url\":\"https://api.github.com/repos/robwinch/176_test/hooks\",\"issue_events_url\":\"https://api.github.com/repos/robwinch/176_test/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/robwinch/176_test/events\",\"assignees_url\":\"https://api.github.com/repos/robwinch/176_test/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/robwinch/176_test/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/robwinch/176_test/tags\",\"blobs_url\":\"https://api.github.com/repos/robwinch/176_test/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/robwinch/176_test/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/robwinch/176_test/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/robwinch/176_test/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/robwinch/176_test/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/robwinch/176_test/languages\",\"stargazers_url\":\"https://api.github.com/repos/robwinch/176_test/stargazers\",\"contributors_url\":\"https://api.github.com/repos/robwinch/176_test/contributors\",\"subscribers_url\":\"https://api.github.com/repos/robwinch/176_test/subscribers\",\"subscription_url\":\"https://api.github.com/repos/robwinch/176_test/subscription\",\"commits_url\":\"https://api.github.com/repos/robwinch/176_test/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/robwinch/176_test/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/robwinch/176_test/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/robwinch/176_test/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/robwinch/176_test/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/robwinch/176_test/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/robwinch/176_test/merges\",\"archive_url\":\"https://api.github.com/repos/robwinch/176_test/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/robwinch/176_test/downloads\",\"issues_url\":\"https://api.github.com/repos/robwinch/176_test/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/robwinch/176_test/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/robwinch/176_test/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/robwinch/176_test/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/robwinch/176_test/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/robwinch/176_test/releases{/id}\",\"created_at\":\"2016-01-04T15:49:23Z\",\"updated_at\":\"2016-01-04T15:49:24Z\",\"pushed_at\":\"2016-01-04T15:49:40Z\",\"git_url\":\"git://github.com/robwinch/176_test.git\",\"ssh_url\":\"git@github.com:robwinch/176_test.git\",\"clone_url\":\"https://github.com/robwinch/176_test.git\",\"svn_url\":\"https://github.com/robwinch/176_test\",\"homepage\":null,\"size\":272,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"JavaScript\",\"has_issues\":false,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":0,\"mirror_url\":null,\"open_issues_count\":0,\"forks\":0,\"open_issues\":0,\"watchers\":0,\"default_branch\":\"master\"}},\"base\":{\"label\":\"rwinch:master\",\"ref\":\"master\",\"sha\":\"ee95fe926e8bf1b1ff144c6adec37e999c154302\",\"user\":{\"login\":\"rwinch\",\"id\":362503,\"avatar_url\":\"https://avatars.githubusercontent.com/u/362503?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/rwinch\",\"html_url\":\"https://github.com/rwinch\",\"followers_url\":\"https://api.github.com/users/rwinch/followers\",\"following_url\":\"https://api.github.com/users/rwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/rwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/rwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/rwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/rwinch/orgs\",\"repos_url\":\"https://api.github.com/users/rwinch/repos\",\"events_url\":\"https://api.github.com/users/rwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/rwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"repo\":{\"id\":32933867,\"name\":\"176_test\",\"full_name\":\"rwinch/176_test\",\"owner\":{\"login\":\"rwinch\",\"id\":362503,\"avatar_url\":\"https://avatars.githubusercontent.com/u/362503?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/rwinch\",\"html_url\":\"https://github.com/rwinch\",\"followers_url\":\"https://api.github.com/users/rwinch/followers\",\"following_url\":\"https://api.github.com/users/rwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/rwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/rwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/rwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/rwinch/orgs\",\"repos_url\":\"https://api.github.com/users/rwinch/repos\",\"events_url\":\"https://api.github.com/users/rwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/rwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"private\":false,\"html_url\":\"https://github.com/rwinch/176_test\",\"description\":\"\",\"fork\":false,\"url\":\"https://api.github.com/repos/rwinch/176_test\",\"forks_url\":\"https://api.github.com/repos/rwinch/176_test/forks\",\"keys_url\":\"https://api.github.com/repos/rwinch/176_test/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/rwinch/176_test/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/rwinch/176_test/teams\",\"hooks_url\":\"https://api.github.com/repos/rwinch/176_test/hooks\",\"issue_events_url\":\"https://api.github.com/repos/rwinch/176_test/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/rwinch/176_test/events\",\"assignees_url\":\"https://api.github.com/repos/rwinch/176_test/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/rwinch/176_test/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/rwinch/176_test/tags\",\"blobs_url\":\"https://api.github.com/repos/rwinch/176_test/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/rwinch/176_test/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/rwinch/176_test/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/rwinch/176_test/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/rwinch/176_test/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/rwinch/176_test/languages\",\"stargazers_url\":\"https://api.github.com/repos/rwinch/176_test/stargazers\",\"contributors_url\":\"https://api.github.com/repos/rwinch/176_test/contributors\",\"subscribers_url\":\"https://api.github.com/repos/rwinch/176_test/subscribers\",\"subscription_url\":\"https://api.github.com/repos/rwinch/176_test/subscription\",\"commits_url\":\"https://api.github.com/repos/rwinch/176_test/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/rwinch/176_test/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/rwinch/176_test/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/rwinch/176_test/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/rwinch/176_test/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/rwinch/176_test/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/rwinch/176_test/merges\",\"archive_url\":\"https://api.github.com/repos/rwinch/176_test/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/rwinch/176_test/downloads\",\"issues_url\":\"https://api.github.com/repos/rwinch/176_test/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/rwinch/176_test/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/rwinch/176_test/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/rwinch/176_test/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/rwinch/176_test/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/rwinch/176_test/releases{/id}\",\"created_at\":\"2015-03-26T14:58:31Z\",\"updated_at\":\"2015-11-12T21:52:59Z\",\"pushed_at\":\"2016-01-04T16:11:27Z\",\"git_url\":\"git://github.com/rwinch/176_test.git\",\"ssh_url\":\"git@github.com:rwinch/176_test.git\",\"clone_url\":\"https://github.com/rwinch/176_test.git\",\"svn_url\":\"https://github.com/rwinch/176_test\",\"homepage\":null,\"size\":328,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"JavaScript\",\"has_issues\":false,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":1,\"mirror_url\":null,\"open_issues_count\":1,\"forks\":1,\"open_issues\":1,\"watchers\":0,\"default_branch\":\"master\"}},\"_links\":{\"self\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/pulls/2\"},\"html\":{\"href\":\"https://github.com/rwinch/176_test/pull/2\"},\"issue\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/issues/2\"},\"comments\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/issues/2/comments\"},\"review_comments\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/pulls/2/comments\"},\"review_comment\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/pulls/comments{/number}\"},\"commits\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/pulls/2/commits\"},\"statuses\":{\"href\":\"https://api.github.com/repos/rwinch/176_test/statuses/a6befb598a35c1c206e1bf7bbb3018f4403b9610\"}},\"merged\":false,\"mergeable\":true,\"mergeable_state\":\"clean\",\"merged_by\":null,\"comments\":0,\"review_comments\":0,\"commits\":1,\"additions\":1,\"deletions\":0,\"changed_files\":1},\"repository\":{\"id\":32933867,\"name\":\"176_test\",\"full_name\":\"rwinch/176_test\",\"owner\":{\"login\":\"rwinch\",\"id\":362503,\"avatar_url\":\"https://avatars.githubusercontent.com/u/362503?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/rwinch\",\"html_url\":\"https://github.com/rwinch\",\"followers_url\":\"https://api.github.com/users/rwinch/followers\",\"following_url\":\"https://api.github.com/users/rwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/rwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/rwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/rwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/rwinch/orgs\",\"repos_url\":\"https://api.github.com/users/rwinch/repos\",\"events_url\":\"https://api.github.com/users/rwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/rwinch/received_events\",\"type\":\"User\",\"site_admin\":false},\"private\":false,\"html_url\":\"https://github.com/rwinch/176_test\",\"description\":\"\",\"fork\":false,\"url\":\"https://api.github.com/repos/rwinch/176_test\",\"forks_url\":\"https://api.github.com/repos/rwinch/176_test/forks\",\"keys_url\":\"https://api.github.com/repos/rwinch/176_test/keys{/key_id}\",\"collaborators_url\":\"https://api.github.com/repos/rwinch/176_test/collaborators{/collaborator}\",\"teams_url\":\"https://api.github.com/repos/rwinch/176_test/teams\",\"hooks_url\":\"https://api.github.com/repos/rwinch/176_test/hooks\",\"issue_events_url\":\"https://api.github.com/repos/rwinch/176_test/issues/events{/number}\",\"events_url\":\"https://api.github.com/repos/rwinch/176_test/events\",\"assignees_url\":\"https://api.github.com/repos/rwinch/176_test/assignees{/user}\",\"branches_url\":\"https://api.github.com/repos/rwinch/176_test/branches{/branch}\",\"tags_url\":\"https://api.github.com/repos/rwinch/176_test/tags\",\"blobs_url\":\"https://api.github.com/repos/rwinch/176_test/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/rwinch/176_test/git/tags{/sha}\",\"git_refs_url\":\"https://api.github.com/repos/rwinch/176_test/git/refs{/sha}\",\"trees_url\":\"https://api.github.com/repos/rwinch/176_test/git/trees{/sha}\",\"statuses_url\":\"https://api.github.com/repos/rwinch/176_test/statuses/{sha}\",\"languages_url\":\"https://api.github.com/repos/rwinch/176_test/languages\",\"stargazers_url\":\"https://api.github.com/repos/rwinch/176_test/stargazers\",\"contributors_url\":\"https://api.github.com/repos/rwinch/176_test/contributors\",\"subscribers_url\":\"https://api.github.com/repos/rwinch/176_test/subscribers\",\"subscription_url\":\"https://api.github.com/repos/rwinch/176_test/subscription\",\"commits_url\":\"https://api.github.com/repos/rwinch/176_test/commits{/sha}\",\"git_commits_url\":\"https://api.github.com/repos/rwinch/176_test/git/commits{/sha}\",\"comments_url\":\"https://api.github.com/repos/rwinch/176_test/comments{/number}\",\"issue_comment_url\":\"https://api.github.com/repos/rwinch/176_test/issues/comments{/number}\",\"contents_url\":\"https://api.github.com/repos/rwinch/176_test/contents/{+path}\",\"compare_url\":\"https://api.github.com/repos/rwinch/176_test/compare/{base}...{head}\",\"merges_url\":\"https://api.github.com/repos/rwinch/176_test/merges\",\"archive_url\":\"https://api.github.com/repos/rwinch/176_test/{archive_format}{/ref}\",\"downloads_url\":\"https://api.github.com/repos/rwinch/176_test/downloads\",\"issues_url\":\"https://api.github.com/repos/rwinch/176_test/issues{/number}\",\"pulls_url\":\"https://api.github.com/repos/rwinch/176_test/pulls{/number}\",\"milestones_url\":\"https://api.github.com/repos/rwinch/176_test/milestones{/number}\",\"notifications_url\":\"https://api.github.com/repos/rwinch/176_test/notifications{?since,all,participating}\",\"labels_url\":\"https://api.github.com/repos/rwinch/176_test/labels{/name}\",\"releases_url\":\"https://api.github.com/repos/rwinch/176_test/releases{/id}\",\"created_at\":\"2015-03-26T14:58:31Z\",\"updated_at\":\"2015-11-12T21:52:59Z\",\"pushed_at\":\"2016-01-04T16:11:27Z\",\"git_url\":\"git://github.com/rwinch/176_test.git\",\"ssh_url\":\"git@github.com:rwinch/176_test.git\",\"clone_url\":\"https://github.com/rwinch/176_test.git\",\"svn_url\":\"https://github.com/rwinch/176_test\",\"homepage\":null,\"size\":328,\"stargazers_count\":0,\"watchers_count\":0,\"language\":\"JavaScript\",\"has_issues\":false,\"has_downloads\":true,\"has_wiki\":true,\"has_pages\":false,\"forks_count\":1,\"mirror_url\":null,\"open_issues_count\":1,\"forks\":1,\"open_issues\":1,\"watchers\":0,\"default_branch\":\"master\"},\"sender\":{\"login\":\"robwinch\",\"id\":11235608,\"avatar_url\":\"https://avatars.githubusercontent.com/u/11235608?v=3\",\"gravatar_id\":\"\",\"url\":\"https://api.github.com/users/robwinch\",\"html_url\":\"https://github.com/robwinch\",\"followers_url\":\"https://api.github.com/users/robwinch/followers\",\"following_url\":\"https://api.github.com/users/robwinch/following{/other_user}\",\"gists_url\":\"https://api.github.com/users/robwinch/gists{/gist_id}\",\"starred_url\":\"https://api.github.com/users/robwinch/starred{/owner}{/repo}\",\"subscriptions_url\":\"https://api.github.com/users/robwinch/subscriptions\",\"organizations_url\":\"https://api.github.com/users/robwinch/orgs\",\"repos_url\":\"https://api.github.com/users/robwinch/repos\",\"events_url\":\"https://api.github.com/users/robwinch/events{/privacy}\",\"received_events_url\":\"https://api.github.com/users/robwinch/received_events\",\"type\":\"User\",\"site_admin\":false}}";

	AccessToken accessToken;

	@Autowired
	GitHubSignature oauth;

	@Before
	public void setupAccessToken() {
		accessToken = new AccessToken(AccessToken.CLA_ACCESS_TOKEN_ID, "GitHubHooksControllerTests_access_token");
		when(mockTokenRepo.findOne(AccessToken.CLA_ACCESS_TOKEN_ID)).thenReturn(accessToken);
	}

	@Test
	public void ping() throws Exception {
		mockMvc.perform(hookRequest().header("X-GitHub-Event", "ping").content(PAYLOAD))
				.andExpect(status().is2xxSuccessful());
	}

	@Test
	public void pingNoToken() throws Exception {
		accessToken = null;

		mockMvc.perform(hookRequest().header("X-GitHub-Event", "ping").content(PAYLOAD))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void userNeverAuthenticated() throws Exception {
		when(mockTokenRepo.findOne("rwinch/176_test"))
				.thenReturn(new AccessToken("rwinch/176_test", "mock_access_token_value"));

		mockMvc.perform(hookRequest().content(PAYLOAD))
			.andExpect(status().isOk());

		ArgumentCaptor<PullRequestStatus> statusCaptor = ArgumentCaptor.forClass(PullRequestStatus.class);
		verify(mockGitHub).save(statusCaptor.capture());

		PullRequestStatus status = statusCaptor.getValue();
		assertThat(status.getRepoId()).isEqualTo("rwinch/176_test");
		assertThat(status.getAccessToken()).isEqualTo("mock_access_token_value");
		assertThat(status.getPullRequestId()).isEqualTo(2);
		assertThat(status.getSha()).isEqualTo("a6befb598a35c1c206e1bf7bbb3018f4403b9610");
		assertThat(status.getUrl()).isEqualTo("http://localhost/sign/pivotal?repositoryId=rwinch/176_test&pullRequestId=2");
		assertThat(status.getFaqUrl()).endsWith("/faq");
		assertThat(status.getSyncUrl()).contains("/sync/pivotal?repositoryId=rwinch/176_test&pullRequestId=2");
		assertThat(status.isSuccess()).isFalse();
	}

	@Test
	public void userNeverAuthenticatedNoToken() throws Exception {
		accessToken = null;

		when(mockTokenRepo.findOne("rwinch/176_test"))
			.thenReturn(new AccessToken("rwinch/176_test", "mock_access_token_value"));

		mockMvc.perform(hookRequest().content(PAYLOAD))
			.andExpect(status().isUnauthorized());
	}

	@Test
	public void gitHubAccessTokenIsNull() throws Exception {
		mockMvc.perform(hookRequest()
				.content(PAYLOAD))
				.andExpect(status().isOk());
	}

	@Test
	public void gitHubAccessTokenIsNullNoToken() throws Exception {
		accessToken = null;

		mockMvc.perform(hookRequest()
				.content(PAYLOAD))
				.andExpect(status().isUnauthorized());
	}

	@Test
	public void markCommitStatusSuccessIndividual() throws Exception {
		User user = WithSigningUserFactory.create();
		when(mockUserRepo.findOne(anyString())).thenReturn(user);
		when(mockTokenRepo.findOne("rwinch/176_test"))
			.thenReturn(new AccessToken("rwinch/176_test", "mock_access_token_value"));
		when(mockIndividualSignatureRepository.findSignaturesFor(any(), any(), anyString())).thenReturn(Arrays.asList(individualSignature));

		mockMvc.perform(hookRequest().content(PAYLOAD))
			.andExpect(status().isOk());

		ArgumentCaptor<PullRequestStatus> statusCaptor = ArgumentCaptor.forClass(PullRequestStatus.class);
		verify(mockGitHub).save(statusCaptor.capture());

		PullRequestStatus status = statusCaptor.getValue();
		assertThat(status.getRepoId()).isEqualTo("rwinch/176_test");
		assertThat(status.getAccessToken()).isEqualTo("mock_access_token_value");
		assertThat(status.getPullRequestId()).isEqualTo(2);
		assertThat(status.getSha()).isEqualTo("a6befb598a35c1c206e1bf7bbb3018f4403b9610");
		assertThat(status.getUrl()).isEqualTo("http://localhost/sign/pivotal?repositoryId=rwinch/176_test&pullRequestId=2");
		assertThat(status.isSuccess()).isTrue();
		assertThat(status.getGitHubUsername()).isEqualTo(user.getGitHubLogin());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void markCommitStatusSuccessCorporate() throws Exception {
		User user = WithSigningUserFactory.create();
		when(mockUserRepo.findOne(anyString())).thenReturn(user);
		when(mockTokenRepo.findOne("rwinch/176_test"))
			.thenReturn(new AccessToken("rwinch/176_test", "mock_access_token_value"));
		when(mockGitHub.getOrganizations(anyString())).thenReturn(Arrays.asList("organization"));
		when(mockCorporateSignatureRepository.findSignature(anyString(), anySet(), anyCollectionOf(String.class))).thenReturn(corporateSignature);

		mockMvc.perform(hookRequest().content(PAYLOAD))
			.andExpect(status().isOk());

		ArgumentCaptor<PullRequestStatus> statusCaptor = ArgumentCaptor.forClass(PullRequestStatus.class);
		verify(mockGitHub).save(statusCaptor.capture());

		PullRequestStatus status = statusCaptor.getValue();
		assertThat(status.getRepoId()).isEqualTo("rwinch/176_test");
		assertThat(status.getAccessToken()).isEqualTo("mock_access_token_value");
		assertThat(status.getPullRequestId()).isEqualTo(2);
		assertThat(status.getSha()).isEqualTo("a6befb598a35c1c206e1bf7bbb3018f4403b9610");
		assertThat(status.getUrl()).isEqualTo("http://localhost/sign/pivotal?repositoryId=rwinch/176_test&pullRequestId=2");
		assertThat(status.isSuccess()).isTrue();
		assertThat(status.getGitHubUsername()).isEqualTo(user.getGitHubLogin());
	}

	@Test
	public void markCommitStatusBadRequest() throws Exception {

		mockMvc.perform(hookRequest().content(""))
			.andExpect(status().isBadRequest());
	}

	private MockHttpServletRequestBuilder hookRequest() {
		MockHttpServletRequestBuilder post = post("/github/hooks/pull_request/pivotal").with(new RequestPostProcessor() {

			@Override
			public MockHttpServletRequest postProcessRequest(MockHttpServletRequest request) {
				if(accessToken != null) {
					try {
						String signature = getSignature(request);
						request.addHeader("X-Hub-Signature", signature);
					} catch(Exception e) {
						throw new RuntimeException(e);
					}
				}
				return request;
			}

			private String getSignature(MockHttpServletRequest request)
					throws IOException, UnsupportedEncodingException, Exception {
				String body = IOUtils.toString(request.getReader());
				String signature = oauth.create(body, accessToken.getToken());
				return signature;
			}
		});

		return post;
	}


}
