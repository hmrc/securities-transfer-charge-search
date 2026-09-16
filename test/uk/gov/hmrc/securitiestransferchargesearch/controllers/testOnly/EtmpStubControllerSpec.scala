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

package uk.gov.hmrc.securitiestransferchargesearch.controllers.testOnly

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers.*
import play.api.test.{FakeRequest, Helpers}

class EtmpStubControllerSpec extends AnyWordSpec with Matchers {

  private val cc = Helpers.stubControllerComponents()
  private val controller = new EtmpStubController(cc)

  "EtmpStubController" should {

    "return recent transactions json when p1=submissionDateRange" in {
      val result = controller.getTransactionSummary("stcId", Some("submissionDateRange"), Some("2026"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("123456789012")
      contentAsString(result) should include("123456789013")
    }

    "return ready to pay json when p1=utrn and v1=unpaid" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("unpaid"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("123456789012")
      contentAsString(result) should include("2026-10-31")
    }

    "return overdue json when p1=utrn and v1=overdue" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("overdue"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("123456789013")
      contentAsString(result) should include("2026-07-31")
    }

    "return contingent json when p1=utrn and v1=contingent" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("contingent"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("123456789014")
      contentAsString(result) should include("STC Contingent charges")
    }

    "return 400 Bad Request when params are missing or invalid" in {
      val result = controller.getTransactionSummary("stcId", Some("invalid"), Some("invalid"))(FakeRequest())

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include("Invalid or missing p1/v1 parameters")
    }
  }
}