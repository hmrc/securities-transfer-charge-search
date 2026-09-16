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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}

@Singleton
class EtmpStubController @Inject()(cc: ControllerComponents) extends BackendController(cc):

  def getTransactionSummary(stcId: String, p1: Option[String], v1: Option[String]): Action[AnyContent] = Action { request =>

    (p1, v1) match {
      case (Some("submissionDateRange"), Some(_)) =>
        Ok(recentTransactionsJson)
        
      case (Some("utrn"), Some("unpaid")) =>
        Ok(readyToPayJson)
        
      case (Some("utrn"), Some("overdue")) =>
        Ok(overdueJson)

      case (Some("utrn"), Some("contingent")) =>
        Ok(contingentJson)
        
      case _ =>
        BadRequest(Json.parse(
          """
            |{
            |  "error": {
            |    "code": "400",
            |    "message": "Invalid or missing p1/v1 parameters in stub",
            |    "logID": "00000000000000000000000000000000"
            |  }
            |}
            |""".stripMargin
        ))
    }
  }

  private val recentTransactionsJson: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2026-09-15T09:30:47Z",
      |    "transactionsCount": 2,
      |    "transactionDetails": [
      |      {
      |        "submissionId": "123456789012",
      |        "submissionDate": "2026-09-01",
      |        "clientReference": "AGRN1212091",
      |        "declareeName": "James White",
      |        "utrn": "900459020010",
      |        "buyerNames": "John White",
      |        "sellerNames": "Mary Philips",
      |        "companyName": "Apple Ltd"
      |      },
      |      {
      |        "submissionId": "123456789013",
      |        "submissionDate": "2026-09-10",
      |        "clientReference": "AGRN1212092",
      |        "declareeName": "James White",
      |        "utrn": "900459020011",
      |        "buyerNames": "John White,Dan J Philips",
      |        "sellerNames": "Peter White,Mary Philips",
      |        "companyName": "Apple Ltd"
      |      }
      |    ],
      |    "charges": [
      |      {
      |        "utrn": "900459020010",
      |        "chargeTypeDescription": "STC Non-contingent charges",
      |        "chargeReference": "XY007000075424",
      |        "chargeType": "LFP",
      |        "chargeAmountTotal": 350.78,
      |        "chargeDueDate": "2026-10-01",
      |        "chargeAmountPending": 0.00
      |      },
      |      {
      |        "utrn": "900459020011",
      |        "chargeTypeDescription": "STC Non-contingent charges",
      |        "chargeReference": "XY007000075425",
      |        "chargeType": "LFP",
      |        "chargeAmountTotal": 450.78,
      |        "chargeDueDate": "2026-10-10",
      |        "chargeAmountPending": 225.96
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  private val readyToPayJson: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2026-09-15T09:30:47Z",
      |    "transactionsCount": 1,
      |    "transactionDetails": [
      |      {
      |        "submissionId": "123456789012",
      |        "submissionDate": "2026-08-28",
      |        "clientReference": "AGRN1212091",
      |        "declareeName": "James White",
      |        "utrn": "900459020010",
      |        "buyerNames": "John White",
      |        "sellerNames": "Mary Philips",
      |        "companyName": "Apple Ltd"
      |      }
      |    ],
      |    "charges": [
      |      {
      |        "utrn": "900459020010",
      |        "chargeTypeDescription": "STC Non-contingent charges",
      |        "chargeReference": "XY007000075424",
      |        "chargeType": "LFP",
      |        "chargeAmountTotal": 350.78,
      |        "chargeDueDate": "2026-10-31",
      |        "chargeAmountPending": 350.78
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  private val overdueJson: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2026-09-15T09:30:47Z",
      |    "transactionsCount": 1,
      |    "transactionDetails": [
      |      {
      |        "submissionId": "123456789013",
      |        "submissionDate": "2026-06-01",
      |        "clientReference": "AGRN1212092",
      |        "declareeName": "James White",
      |        "utrn": "900459020011",
      |        "buyerNames": "John White,Dan J Philips",
      |        "sellerNames": "Peter White,Mary Philips",
      |        "companyName": "Apple Ltd"
      |      }
      |    ],
      |    "charges": [
      |      {
      |        "utrn": "900459020011",
      |        "chargeTypeDescription": "STC Non-contingent charges",
      |        "chargeReference": "XY007000075425",
      |        "chargeType": "LFP",
      |        "chargeAmountTotal": 450.78,
      |        "chargeDueDate": "2026-07-31",
      |        "chargeAmountPending": 450.78
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  private val contingentJson: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2026-09-15T09:30:47Z",
      |    "transactionsCount": 1,
      |    "transactionDetails": [
      |      {
      |        "submissionId": "123456789014",
      |        "submissionDate": "2026-09-05",
      |        "clientReference": "AGRN1212093",
      |        "declareeName": "James White",
      |        "utrn": "900459020012",
      |        "buyerNames": "John White",
      |        "sellerNames": "Mary Philips",
      |        "companyName": "Apple Ltd"
      |      }
      |    ],
      |    "charges": [
      |      {
      |        "utrn": "900459020012",
      |        "chargeTypeDescription": "STC Contingent charges",
      |        "chargeReference": "XY007000075426",
      |        "chargeType": "LFP",
      |        "chargeAmountTotal": 1000.00,
      |        "chargeDueDate": "2026-12-31",
      |        "chargeAmountPending": 1000.00
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )