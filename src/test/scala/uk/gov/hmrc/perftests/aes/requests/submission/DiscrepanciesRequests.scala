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

object DiscrepanciesRequests extends AESConfig {

  import SubmissionRequests.formStep

  val enterConsolidation: List[HttpRequestBuilder] = formStep(
    "Consolidation with MUCR",
    "/part-of-consolidation",
    "/location-type",
    "boolean" -> "true",
    "mucr"    -> "GB/000000000000-12345"
  )

  val chooseApprovedPlace: List[HttpRequestBuilder] =
    formStep("Approved place", "/location-type", "/location-id", "value" -> "approvedPlace")

  val chooseLarne: List[HttpRequestBuilder] =
    formStep("Larne office of exit", "/office-of-exit", "/split-exit", "value" -> "XI005220")

  val confirmDiscrepancies: List[HttpRequestBuilder] =
    formStep("Any discrepancies?", "/any-discrepancies", "/discrepancy-consignment", "value" -> "true")

  private def summary(page: String, path: String, continueTo: String): HttpRequestBuilder =
    http(s"[get] $page")
      .get(s"$aesFrontendUrl$path")
      .check(status.is(200), css(s"a.govuk-button[href='$aesFrontendPath$continueTo']").exists)

  private val railModeForm =
    formStep(
      "Mode of transport at border",
      "/discrepancy-consignment",
      "/discrepancy-transport/1",
      "value" -> "#{railMode}"
    )

  private val railOption =
    "select[name=value] option[value='2'], select[name=value] option[value=rail], input[name=value][value=rail]"

  val enterTransportEquipment: List[HttpRequestBuilder] =
    List(railModeForm.head.check(css(railOption, "value").saveAs("railMode"))) ++ railModeForm.tail ++
      formStep(
        "Container details",
        "/discrepancy-transport/1",
        "/transport-equipment-check-answers/1",
        "containerId"   -> "CONT1234567890123",
        "numberOfSeals" -> "1"
      ) ++
      List(
        summary(
          "Transport equipment check answers",
          "/transport-equipment-check-answers/1",
          "/add-another-transport-equipment"
        )
      ) ++
      formStep(
        "Add another transport equipment",
        "/add-another-transport-equipment",
        "/discrepancy-seals/1",
        "value" -> "false"
      )

  val enterSeal: List[HttpRequestBuilder] =
    formStep("Seal identifier", "/discrepancy-seals/1", "/seals-check-answers/1", "value" -> "SEAL123") ++
      List(summary("Seal check answers", "/seals-check-answers/1", "/add-another-seal")) ++
      formStep("Add another seal", "/add-another-seal", "/discrepancy-reference/1", "value" -> "false")

  val enterGoodsReference: List[HttpRequestBuilder] =
    formStep(
      "Declaration goods reference",
      "/discrepancy-reference/1",
      "/goods-reference-check-answers/1",
      "value" -> "123"
    ) ++
      List(
        summary(
          "Goods reference check answers",
          "/goods-reference-check-answers/1",
          "/add-another-goods-reference"
        )
      ) ++
      formStep(
        "Add another goods reference",
        "/add-another-goods-reference",
        "/discrepancy-transport-means",
        "value" -> "false"
      )

  val enterBorderTransport: List[HttpRequestBuilder] =
    formStep(
      "Transport across the border",
      "/discrepancy-transport-means",
      "/discrepancy-transport-doc/1",
      "transportType"         -> "10",
      "transportIdNumber"     -> "IDNUMBER123",
      "countryOfRegistration" -> "GB"
    )

  val enterTransportDocument: List[HttpRequestBuilder] =
    formStep(
      "Transport document details",
      "/discrepancy-transport-doc/1",
      "/discrepancy-transport-doc-check-answers/1",
      "documentType"    -> "1",
      "referenceNumber" -> "1234"
    ) ++
      List(
        summary(
          "Transport document check answers",
          "/discrepancy-transport-doc-check-answers/1",
          "/add-another-transport-doc"
        )
      ) ++
      formStep("Add another transport document", "/add-another-transport-doc", "/discrepancy-goods", "value" -> "false")

  val enterChangedDetails: List[HttpRequestBuilder] =
    formStep(
      "Changed goods details",
      "/discrepancy-goods",
      "/discrepancy-packing/1",
      "declarationGoodsItemNumber"            -> "4",
      "declarationUniqueConsignmentReference" -> "#{ducr}",
      "newGrossMass"                          -> "30",
      "newNetMass"                            -> "28"
    )

  private def packing(
    index: Int,
    code: String,
    packages: String,
    marks: String,
    addAnother: Boolean
  ): List[HttpRequestBuilder] = {
    val checkAnswers   = s"/packaging-details-check-answers/$index"
    val addAnotherPath = "/add-another-packaging-detail"
    val nextPath       = if (addAnother) s"/discrepancy-packing/${index + 1}" else "/check-answers"

    formStep(
      s"Packing details $index",
      s"/discrepancy-packing/$index",
      checkAnswers,
      "packagingCode"    -> code,
      "numberOfPackages" -> packages,
      "shippingMarks"    -> marks
    ) ++
      List(summary(s"Packaging check answers $index", checkAnswers, addAnotherPath)) ++
      formStep(
        s"Add another packaging detail after item $index",
        addAnotherPath,
        nextPath,
        "value" -> addAnother.toString
      )
  }

  val enterPackingDetails: List[HttpRequestBuilder] =
    packing(1, "BX", "1", "MARKS123", addAnother = true) ++
      packing(2, "CT", "3", "MARKS456", addAnother = false)
}
