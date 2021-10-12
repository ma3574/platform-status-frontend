/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.platformstatusfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.platformstatusfrontend.config.AppConfig
import uk.gov.hmrc.platformstatusfrontend.services.{PlatformStatus, StatusChecker}
import uk.gov.hmrc.platformstatusfrontend.views.html.status
import play.api.libs.concurrent.Futures
import scala.concurrent.duration._
import play.api.libs.concurrent.Futures._
import PlatformStatus._
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class StatusController @Inject()(appConfig: AppConfig, mcc: MessagesControllerComponents, val statusChecker: StatusChecker, statusView: status)(implicit executionContext: ExecutionContext, futures: Futures)
  extends FrontendController(mcc) {

  implicit val config: AppConfig = appConfig

  def defaultLanding: Action[AnyContent] = Action.async { implicit request => Future.successful(Redirect(routes.StatusController.platformStatus())) }

  def platformStatus: Action[AnyContent] = Action.async { implicit request =>

    val iteration2Future = statusChecker.iteration2Status()
    val iteration3Future = statusChecker.iteration3Status()
    val iteration4Future = statusChecker.iteration4Status()
    val iteration5Future = statusChecker.iteration5Status()

    for {
      iter2 <- iteration2Future
      iter3 <- iteration3Future
      iter4 <- iteration4Future
      iter5 <- iteration5Future

      statusMap = List(statusChecker.iteration1Status(),
        iter2,
        iter3,
        iter4,
        iter5
      )
    } yield Ok(statusView(statusMap))
  }

}


