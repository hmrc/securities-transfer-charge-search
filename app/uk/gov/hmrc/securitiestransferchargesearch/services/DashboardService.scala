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

import uk.gov.hmrc.securitiestransferchargesearch.models.EtmpTransactionSummaryResponse
import scala.concurrent.Future

trait DashboardService {
  def getRecentTransactions(stcId: String, submissionDateRange: String): Future[EtmpTransactionSummaryResponse]
  def getReadyToPayTransactions(stcId: String): Future[EtmpTransactionSummaryResponse]
  def getOverdueTransactions(stcId: String): Future[EtmpTransactionSummaryResponse]
  def getContingentTransactions(stcId: String): Future[EtmpTransactionSummaryResponse]
}