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

import uk.gov.hmrc.securitiestransferchargesearch.models.*

import javax.inject.{Inject, Named, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait DashboardDataCache:
  def store(stcId: String, p1: String, v1: String, resp: EtmpTransactionSummaryResponse): Future[Unit]
  def retrieve(stcId: String, p1: String, v1: String): Future[Option[EtmpTransactionSummaryResponse]]

@Singleton
class DashboardServiceCache @Inject()(
                                      @Named("etmp") service: DashboardService,
                                      dashboardServiceCache: DashboardDataCache
                                    )(implicit ec: ExecutionContext) extends DashboardService {


  private type ServiceFunction = () => Future[EtmpTransactionSummaryResponse]

  override def getRecentTransactions(stcId: String, submissionDateRange: String): Future[EtmpTransactionSummaryResponse] = {
    val serviceFunction = () => service.getRecentTransactions(stcId, submissionDateRange)
    checkCache(stcId, "submissionDateRange", submissionDateRange, serviceFunction)
  }

  override def getReadyToPayTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    val serviceFunction = () => service.getReadyToPayTransactions(stcId)
    checkCache(stcId, "utrn", "unpaid", serviceFunction)
  }

  override def getOverdueTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    val serviceFunction = () => service.getOverdueTransactions(stcId)
    checkCache(stcId, p1 = "utrn", v1 = "overdue", serviceFunction)
  }

  override def getContingentTransactions(stcId: String): Future[EtmpTransactionSummaryResponse] = {
    val serviceFunction = () => service.getContingentTransactions(stcId)
    checkCache(stcId, p1 = "utrn", v1 = "contingent", serviceFunction)
  }

  def checkCache(stcId: String, p1: String, v1: String, callService: ServiceFunction): Future[EtmpTransactionSummaryResponse] = {
    dashboardServiceCache
      .retrieve(stcId, p1, v1)
      .flatMap {
        case Some(resp) => Future.successful(resp)
        case None       => for {
          etmpResponse <- callService()
        } yield {
          dashboardServiceCache.store(stcId, p1, v1, etmpResponse)
          etmpResponse
        }
      }
  }


}
