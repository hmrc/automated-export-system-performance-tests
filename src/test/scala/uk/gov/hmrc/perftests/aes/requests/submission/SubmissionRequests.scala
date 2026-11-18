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

package uk.gov.hmrc.perftests.aes.requests.submission

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import uk.gov.hmrc.perftests.aes.config.AESConfig

object SubmissionRequests extends AESConfig {

  private val mrnUrl   = s"$aesFrontendUrl/mrn"
  private val ducrPath = s"$aesFrontendPath/ducr"

  val getHomePage: HttpRequestBuilder =
    http("[get] AES homepage")
      .get(aesHomePageUrl)
      .check(
        status.is(200),
        css(s"a.govuk-button[href='$aesFrontendPath/mrn']").exists
      )

  private val getMrnPage: HttpRequestBuilder =
    http("[get] MRN page")
      .get(mrnUrl)
      .check(
        status.is(200),
        css("input[name=csrfToken]", "value").saveAs("mrnCsrfToken")
      )

  private val postMrn: HttpRequestBuilder =
    http("[post] Submit MRN")
      .post(mrnUrl)
      .disableFollowRedirect
      .formParam("csrfToken", "#{mrnCsrfToken}")
      .formParam("value", "#{mrn}")
      .check(status.is(303), header("Location").is(ducrPath))

  val enterMrn: List[HttpRequestBuilder] = List(getMrnPage, postMrn)

  private[submission] def formStep(
    page: String,
    path: String,
    nextPath: String,
    fields: (String, String)*
  ): List[HttpRequestBuilder] = {
    val url      = s"$aesFrontendUrl$path"
    val formPath = s"$aesFrontendPath$path"

    val getPage = http(s"[get] $page")
      .get(url)
      .check(
        status.is(200),
        css(s"form[action='$formPath'] input[name=csrfToken]", "value").saveAs("formCsrfToken")
      )

    val submitPage = fields.foldLeft(
      http(s"[post] $page")
        .post(url)
        .disableFollowRedirect
        .formParam("csrfToken", "#{formCsrfToken}")
    ) { case (request, (name, value)) =>
      request.formParam(name, value)
    }

    List(
      getPage,
      submitPage.check(status.is(303), header("Location").is(s"$aesFrontendPath$nextPath"))
    )
  }

  val enterDucr =
    formStep("DUCR", "/ducr", "/part-of-consolidation", "value" -> "#{ducr}")

  val answerConsolidation =
    formStep("Part of a consolidation", "/part-of-consolidation", "/location-type", "boolean" -> "false")

  val chooseLocationType =
    formStep("Type of location", "/location-type", "/location-id", "value" -> "designatedLocation")

  val identifyLocation = formStep(
    "Identify the location",
    "/location-id",
    "/office-of-exit",
    "locationType"                 -> "authnumber",
    "unlocode"                     -> "UN123",
    "locationAdditionalIdentifier" -> "AD01",
    "authorisationReferenceNumber" -> "AUTH12345"
  )

  val chooseOfficeOfExit =
    formStep("Office of exit", "/office-of-exit", "/split-exit", "value" -> "XI000142")

  val answerSplitExit =
    formStep("Split exit", "/split-exit", "/any-discrepancies", "value" -> "false")

  val answerDiscrepancies =
    formStep("Discrepancies", "/any-discrepancies", "/check-answers", "value" -> "false")

  val getCheckYourAnswers: HttpRequestBuilder =
    http("[get] Check your answers")
      .get(s"$aesFrontendUrl/check-answers")
      .check(status.is(200), css(s"a[href='$aesFrontendPath/submit']").exists)

  private val acceptAndSubmit: HttpRequestBuilder =
    http("[get] Accept and submit")
      .get(s"$aesFrontendUrl/submit")
      .disableFollowRedirect
      .check(status.is(303), header("Location").is(s"$aesFrontendPath/confirmation"))

  private val getConfirmation: HttpRequestBuilder =
    http("[get] Initial submission confirmation")
      .get(s"$aesFrontendUrl/confirmation")
      .check(
        status.is(200),
        css("h1").is("IE507(a) pre-notification received")
      )

  val submitAndConfirm: List[HttpRequestBuilder] =
    List(acceptAndSubmit, getConfirmation)
}
