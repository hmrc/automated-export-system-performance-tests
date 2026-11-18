/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.perftests.aes.requests.auth

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.perftests.aes.config.AESConfig

object AuthLoginStubRequest extends AESConfig {

  private val signInUrl = s"$baseUrlAuthLoginStub/auth-login-stub/gg-sign-in"

  private val getAuthPage: HttpRequestBuilder =
    http("[get] Auth Login Stub sign-in")
      .get(signInUrl)
      .check(
        status.is(200),
        css("input[name=csrfToken]", "value").saveAs("authCsrfToken")
      )

  private val postAuthLogin: HttpRequestBuilder =
    http("[post] Auth Login Stub sign-in")
      .post(signInUrl)
      .disableFollowRedirect
      .formParam("authorityId", "")
      .formParam("redirectionUrl", aesHomePageUrl)
      .formParam("credentialStrength", "strong")
      .formParam("confidenceLevel", "50")
      .formParam("affinityGroup", "Organisation")
      .formParam("enrolment[0].name", "HMRC-CUS-ORG")
      .formParam("enrolment[0].taxIdentifier[0].name", "EORINumber")
      .formParam("enrolment[0].taxIdentifier[0].value", "#{userEori}")
      .formParam("enrolment[0].state", "Activated")
      .formParam("csrfToken", "#{authCsrfToken}")
      .check(status.is(303), header("Location").is(aesHomePageUrl))

  val journey: List[HttpRequestBuilder] = List(getAuthPage, postAuthLogin)
}
