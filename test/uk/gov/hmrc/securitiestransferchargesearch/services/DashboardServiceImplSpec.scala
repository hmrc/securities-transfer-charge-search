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

package uk.gov.hmrc.securitiestransferchargesearch.services

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.HttpClientV2Support
import uk.gov.hmrc.securitiestransferchargesearch.config.AppConfig
import org.scalatest.concurrent.IntegrationPatience

import scala.concurrent.ExecutionContext.Implicits.global

class DashboardServiceImplSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with HttpClientV2Support
    with IntegrationPatience {

  private val wireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    configureFor("localhost", wireMockServer.port())
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetAll()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
    super.afterAll()
  }

  private val mockAppConfig = mock[AppConfig]
  private lazy val service = new DashboardServiceImpl(httpClientV2, mockAppConfig)

  private val stcId = "XASTC0012345678"
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val emptyEtmpResponse =
    """
      |{
      |  "success": {
      |    "processingDate": "2026-09-14T09:30:47Z",
      |    "transactionsCount": 0
      |  }
      |}
      |""".stripMargin

  private def stubEtmpResponse(p1: String, v1: String, responseBody: String = emptyEtmpResponse): Unit = {
    stubFor(
      get(urlEqualTo(s"/RESTAdapter/stc/transaction/summary/$stcId?p1=$p1&v1=$v1"))
        .withHeader("X-Originating-System", equalTo("MDTP-STC"))
        .withHeader("X-Transmitting-System", equalTo("HIP"))
        .withHeader("X-Receipt-Date", matching(".*Z$"))
        .withHeader("correlationid", matching(".+"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(responseBody)
        )
    )
  }

  "DashboardServiceImpl" should {

    "getRecentTransactions must pass p1=submissionDateRange and v1=[dateRange]" in {
      when(mockAppConfig.etmpBaseUrl).thenReturn(s"http://localhost:${wireMockServer.port()}")

      val dateRange = "2026-01-01"
      stubEtmpResponse(p1 = "submissionDateRange", v1 = dateRange)

      val result = service.getRecentTransactions(stcId, dateRange).futureValue
      result.success.transactionsCount shouldBe 0

      wireMockServer.verify(1, getRequestedFor(urlEqualTo(s"/RESTAdapter/stc/transaction/summary/$stcId?p1=submissionDateRange&v1=$dateRange")))
    }

    "getReadyToPayTransactions must pass p1=utrn and v1=unpaid" in {
      when(mockAppConfig.etmpBaseUrl).thenReturn(s"http://localhost:${wireMockServer.port()}")

      stubEtmpResponse(p1 = "utrn", v1 = "unpaid")

      val result = service.getReadyToPayTransactions(stcId).futureValue
      result.success.transactionsCount shouldBe 0

      wireMockServer.verify(1, getRequestedFor(urlEqualTo(s"/RESTAdapter/stc/transaction/summary/$stcId?p1=utrn&v1=unpaid")))
    }

    "getOverdueTransactions must pass p1=utrn and v1=overdue" in {
      when(mockAppConfig.etmpBaseUrl).thenReturn(s"http://localhost:${wireMockServer.port()}")

      stubEtmpResponse(p1 = "utrn", v1 = "overdue")

      val result = service.getOverdueTransactions(stcId).futureValue
      result.success.transactionsCount shouldBe 0

      wireMockServer.verify(1, getRequestedFor(urlEqualTo(s"/RESTAdapter/stc/transaction/summary/$stcId?p1=utrn&v1=overdue")))
    }

    "getContingentTransactions must pass p1=utrn and v1=contingent" in {
      when(mockAppConfig.etmpBaseUrl).thenReturn(s"http://localhost:${wireMockServer.port()}")

      stubEtmpResponse(p1 = "utrn", v1 = "contingent")

      val result = service.getContingentTransactions(stcId).futureValue
      result.success.transactionsCount shouldBe 0

      wireMockServer.verify(1, getRequestedFor(urlEqualTo(s"/RESTAdapter/stc/transaction/summary/$stcId?p1=utrn&v1=contingent")))
    }

    "correctly parse a populated raw ETMP response" in {
      when(mockAppConfig.etmpBaseUrl).thenReturn(s"http://localhost:${wireMockServer.port()}")

      val populatedEtmpResponse =
        """
          |{
          |  "success": {
          |    "processingDate": "2026-09-14T09:30:47Z",
          |    "transactionsCount": 1,
          |    "transactionDetails": [
          |      {
          |        "submissionId": "SUB123",
          |        "submissionDate": "2026-01-01",
          |        "declareeName": "John Doe",
          |        "utrn": "UTRN_MATCH",
          |        "buyerNames": "Buyer1",
          |        "companyName": "Comp1"
          |      }
          |    ],
          |    "charges": [
          |      {
          |        "utrn": "UTRN_MATCH",
          |        "chargeTypeDescription": "Desc",
          |        "chargeReference": "REF1",
          |        "chargeType": "Type1",
          |        "chargeAmountTotal": 100.00,
          |        "chargeDueDate": "2026-10-31",
          |        "chargeAmountPending": 50.00
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin

      stubEtmpResponse(p1 = "utrn", v1 = "unpaid", responseBody = populatedEtmpResponse)

      val result = service.getReadyToPayTransactions(stcId).futureValue

      result.success.transactionsCount shouldBe 1
      result.success.transactionDetails.get.head.submissionId shouldBe "SUB123"
      result.success.charges.get.head.chargeAmountTotal shouldBe 100.00
    }
  }
}