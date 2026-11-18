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

package uk.gov.hmrc.perftests.aes.simulations
import io.gatling.core.Predef._
import uk.gov.hmrc.perftests.aes.requests.auth.AuthLoginStubRequest
import uk.gov.hmrc.perftests.aes.requests.submission.DiscrepanciesRequests._
import uk.gov.hmrc.perftests.aes.requests.submission.SubmissionRequests._
import uk.gov.hmrc.perftests.aes.utils.TestDataGenerators
import uk.gov.hmrc.performance.simulation.PerformanceTestRunner

class AesUiSimulation extends PerformanceTestRunner {

  private val prepareTestData = exec { session =>
    session
      .set("userEori", "GB12345679")
      .set("mrn", TestDataGenerators.generateMrn())
      .set("ducr", "5GB00000000000012345")
  }

  setup("web-test-prep", "Prepare AES test data").withActions(prepareTestData.actionBuilders: _*)
  setup("login", "Sign in with a CDS enrolment").withRequests(AuthLoginStubRequest.journey: _*)
  setup("view-aes-homepage", "View AES homepage").withRequests(getHomePage)
  setup("enter-mrn", "Enter MRN").withRequests(enterMrn: _*)
  setup("enter-ducr", "Enter DUCR").withRequests(enterDucr: _*)
  setup("identify-location", "Identify the location").withRequests(identifyLocation: _*)
  setup("answer-split-exit", "Answer split exit question").withRequests(answerSplitExit: _*)

  setup("answer-consolidation", "Answer consolidation question").withRequests(answerConsolidation: _*)
  setup("choose-location-type", "Choose location type").withRequests(chooseLocationType: _*)
  setup("choose-office-of-exit", "Choose office of exit").withRequests(chooseOfficeOfExit: _*)
  setup("answer-discrepancies", "Any discrepancies? No").withRequests(answerDiscrepancies: _*)

  setup("consolidate-with-mucr", "Enter consolidation MUCR").withRequests(enterConsolidation: _*)
  setup("choose-approved-place", "Choose approved place").withRequests(chooseApprovedPlace: _*)
  setup("choose-larne", "Choose Larne office of exit").withRequests(chooseLarne: _*)
  setup("answer-yes-discrepancies", "Any discrepancies? Yes").withRequests(confirmDiscrepancies: _*)
  setup("enter-transport-equipment", "Enter transport equipment").withRequests(enterTransportEquipment: _*)
  setup("enter-seal", "Enter seal details").withRequests(enterSeal: _*)
  setup("enter-goods-reference", "Enter goods reference").withRequests(enterGoodsReference: _*)
  setup("enter-border-transport", "Enter border transport details").withRequests(enterBorderTransport: _*)
  setup("enter-transport-document", "Enter and review transport document").withRequests(enterTransportDocument: _*)
  setup("enter-changed-details", "Enter changed goods details").withRequests(enterChangedDetails: _*)
  setup("enter-packing-details", "Enter and review packaging details").withRequests(enterPackingDetails: _*)

  setup("check-your-answers", "Check your answers").withRequests(getCheckYourAnswers)
  setup("submit-and-confirm", "Accept and submit through initial confirmation").withRequests(submitAndConfirm: _*)

  runSimulation()
}
