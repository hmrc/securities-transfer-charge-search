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

import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.securitiestransferchargesearch.config.AppConfig
import uk.gov.hmrc.securitiestransferchargesearch.models.*
import uk.gov.hmrc.securitiestransferchargesearch.services.DashboardService.P1Parameters
import uk.gov.hmrc.securitiestransferchargesearch.services.DashboardService.P1Parameters.submissionDateRange

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import DashboardService.P1Parameters.*
import DashboardService.V1Parameters.*

@Singleton
class DashboardServiceImpl @Inject()(
                                      httpClient: HttpClientV2,
                                      appConfig: AppConfig
                                    )(implicit ec: ExecutionContext) extends DashboardService with Logging {

  private def baseUrl: String = appConfig.etmpBaseUrl

  private val etmpDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

  private[services] def recentSubmissionsDateRange(): String =
    List(
      LocalDate.now().minusMonths(appConfig.numberOfMonthsInRecentSubmissions),
      LocalDate.now()
    ).map(_.format(etmpDateFormatter))
      .mkString("-")

  override def getRecentTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = submissionDateRange.toString, v1 = recentSubmissionsDateRange())
  }

  override def getReadyToPayTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = utrn.toString, v1 = unpaid.toString)
  }

  override def getOverdueTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = utrn.toString, v1 = overdue.toString)
  }

  override def getContingentTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    callEtmpApi(stcId, p1 = utrn.toString, v1 = contingent.toString)
  }

  override def getSubmissionSummary(stcId: String, submissionId: String): Future[EtmpTransactionSummaryResponse] =
    callEtmpApi(stcId, p1 = P1Parameters.submissionId.toString, v1 = submissionId)

  private def callEtmpApi(stcId: String, p1: String, v1: String): Future[EtmpTransactionSummaryResponse] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val url = url"$baseUrl/RESTAdapter/stc/transaction/summary/$stcId?p1=$p1&v1=$v1"

    val correlationId = UUID.randomUUID().toString
    val receiptDate = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"))

    val etmpHeaders: Seq[(String, String)] = Seq(
      "X-Originating-System" -> "MDTP-STC",
      "X-Transmitting-System" -> "HIP",
      "X-Receipt-Date" -> receiptDate,
      "correlationid" -> correlationId
    )

    httpClient.get(url)
      .setHeader(etmpHeaders: _*)
      .execute[HttpResponse]
      .flatMap {
        case resp if resp.status == OK =>
          resp.json.validate[EtmpTransactionSummaryResponse] match {
            case JsSuccess(value, _) => Future.successful(value)
            case JsError(errors) =>
              val error = s"Dashboard service: Received 200 from ETMP but could not parse the JSON: $errors"
              logger.warn(error)
              Future.failed(new Exception(error))
          }
        case otherResp =>
          val error = s"Dashboard service: Received ${otherResp.status} response from ETMP."
          logger.warn(error)
          Future.failed(new Exception(error))
      }
    }
}
