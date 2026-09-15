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

package uk.gov.hmrc.securitiestransferchargesearch.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.auth.core.{AuthConnector, MissingBearerToken}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.securitiestransferchargesearch.models._
import uk.gov.hmrc.securitiestransferchargesearch.services.DashboardService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures {

  private val mockDashboardService = mock[DashboardService]
  private val cc = Helpers.stubControllerComponents()
  
  private val stubAuthConnector = new AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.successful(().asInstanceOf[A])
  }
  
  private val failingAuthConnector = new AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(MissingBearerToken("Bearer token not supplied"))
  }

  private val controller = new DashboardController(cc, mockDashboardService, stubAuthConnector)
  private val unauthorisedController = new DashboardController(cc, mockDashboardService, failingAuthConnector)

  private val stcId = "XASTC0012345678"
  private val dateRange = "2026-01-01"

  private val sampleResponse = EtmpTransactionSummaryResponse(
    success = EtmpSuccessResponse(
      processingDate = "2026-09-14T09:30:47Z",
      transactionsCount = 0,
      message = None,
      transactionDetails = Some(Seq.empty),
      charges = Some(Seq.empty)
    )
  )

  "DashboardController" should {

    "return 401 Unauthorised if the user is not authenticated" in {
      val result: Future[Result] = unauthorisedController.getRecentTransactions(stcId, dateRange)(FakeRequest())

      status(result) shouldBe UNAUTHORIZED
      (contentAsJson(result) \ "error").as[String] shouldBe "User is not authorised"
    }

    "getRecentTransactions" must {
      "return 200 OK and JSON when user is authorised and service returns data" in {
        when(mockDashboardService.getRecentTransactions(any(), any()))
          .thenReturn(Future.successful(sampleResponse))

        val result: Future[Result] = controller.getRecentTransactions(stcId, dateRange)(FakeRequest())

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(sampleResponse)
      }

      "return 500 InternalServerError when service fails" in {
        when(mockDashboardService.getRecentTransactions(any(), any()))
          .thenReturn(Future.failed(new RuntimeException("API Down")))

        val result: Future[Result] = controller.getRecentTransactions(stcId, dateRange)(FakeRequest())

        status(result) shouldBe INTERNAL_SERVER_ERROR
        (contentAsJson(result) \ "error").as[String] shouldBe "API Down"
      }
    }

    "getReadyToPayTransactions" must {
      "return 200 OK and JSON on success" in {
        when(mockDashboardService.getReadyToPayTransactions(any()))
          .thenReturn(Future.successful(sampleResponse))

        val result = controller.getReadyToPayTransactions(stcId)(FakeRequest())
        status(result) shouldBe OK
      }
    }

    "getOverdueTransactions" must {
      "return 200 OK and JSON on success" in {
        when(mockDashboardService.getOverdueTransactions(any()))
          .thenReturn(Future.successful(sampleResponse))

        val result = controller.getOverdueTransactions(stcId)(FakeRequest())
        status(result) shouldBe OK
      }
    }

    "getContingentTransactions" must {
      "return 200 OK and JSON on success" in {
        when(mockDashboardService.getContingentTransactions(any()))
          .thenReturn(Future.successful(sampleResponse))

        val result = controller.getContingentTransactions(stcId)(FakeRequest())
        status(result) shouldBe OK
      }
    }
  }
}