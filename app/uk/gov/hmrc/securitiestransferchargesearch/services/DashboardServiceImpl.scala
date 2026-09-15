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

import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.securitiestransferchargesearch.config.AppConfig
import uk.gov.hmrc.securitiestransferchargesearch.models.*

import java.time.format.DateTimeFormatter
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardServiceImpl @Inject()(
                                      httpClient: HttpClientV2,
                                      appConfig: AppConfig
                                    )(implicit ec: ExecutionContext) extends DashboardService {

  private def baseUrl: String = appConfig.etmpBaseUrl

  override def getRecentTransactions(stcId: String, submissionDateRange: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = "submissionDateRange", v1 = submissionDateRange)
  }

  override def getReadyToPayTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = "utrn", v1 = "unpaid")
  }

  override def getOverdueTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = "utrn", v1 = "overdue")
  }

  override def getContingentTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = "utrn", v1 = "contingent")
  }

  private def callEtmpApi(stcId: String, p1: String, v1: String): Future[EtmpTransactionSummaryResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val url = url"$baseUrl/RESTAdapter/stc/transaction/summary/$stcId?p1=$p1&v1=$v1"

    val correlationId = UUID.randomUUID().toString
    val receiptDate = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))

    httpClient.get(url)
      .setHeader("X-Originating-System" -> "MDTP-STC")
      .setHeader("X-Transmitting-System" -> "HIP")
      .setHeader("X-Receipt-Date" -> receiptDate)
      .setHeader("correlationid" -> correlationId)
      .execute[EtmpTransactionSummaryResponse]
  }
}