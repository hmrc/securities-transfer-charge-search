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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.securitiestransferchargesearch.services.DashboardService
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DashboardController @Inject()(
                                     cc: ControllerComponents,
                                     dashboardService: DashboardService,
                                     val authConnector: AuthConnector
                                   )(implicit ec: ExecutionContext) extends BackendController(cc) with AuthorisedFunctions {

  def getRecentTransactions(stcId: String, dateRange: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      dashboardService.getRecentTransactions(stcId, dateRange).map { transactions =>
        Ok(Json.toJson(transactions))
      }
    }.recover(handleErrors)
  }

  def getReadyToPayTransactions(stcId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      dashboardService.getReadyToPayTransactions(stcId).map { transactions =>
        Ok(Json.toJson(transactions))
      }
    }.recover(handleErrors)
  }

  def getOverdueTransactions(stcId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      dashboardService.getOverdueTransactions(stcId).map { transactions =>
        Ok(Json.toJson(transactions))
      }
    }.recover(handleErrors)
  }

  def getContingentTransactions(stcId: String): Action[AnyContent] = Action.async { implicit request =>
    authorised() {
      dashboardService.getContingentTransactions(stcId).map { transactions =>
        Ok(Json.toJson(transactions))
      }
    }.recover(handleErrors)
  }

  private def handleErrors: PartialFunction[Throwable, play.api.mvc.Result] = {
    case _: AuthorisationException => Unauthorized(Json.obj("error" -> "User is not authorised"))
    case e: Exception              => InternalServerError(Json.obj("error" -> e.getMessage))
  }
}