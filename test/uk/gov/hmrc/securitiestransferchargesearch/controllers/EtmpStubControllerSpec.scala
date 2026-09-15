package uk.gov.hmrc.securitiestransferchargesearch.controllers

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

class EtmpStubControllerSpec extends AnyWordSpec with Matchers {

  private val cc = Helpers.stubControllerComponents()
  private val controller = new EtmpStubController(cc)

  "EtmpStubController" should {

    "return recent transactions json when p1=submissionDateRange" in {
      val result = controller.getTransactionSummary("stcId", Some("submissionDateRange"), Some("2026"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("123456789012")
    }

    "return ready to pay json when p1=utrn and v1=unpaid" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("unpaid"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("UNPAID000001")
    }

    "return overdue json when p1=utrn and v1=overdue" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("overdue"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("OVERDUE00001")
    }

    "return contingent json when p1=utrn and v1=contingent" in {
      val result = controller.getTransactionSummary("stcId", Some("utrn"), Some("contingent"))(FakeRequest())

      status(result) shouldBe OK
      contentAsString(result) should include("CONTINGENT01")
    }

    "return 400 Bad Request when params are missing or invalid" in {
      val result = controller.getTransactionSummary("stcId", Some("invalid"), Some("invalid"))(FakeRequest())

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include("Invalid or missing p1/v1 parameters")
    }
  }
}