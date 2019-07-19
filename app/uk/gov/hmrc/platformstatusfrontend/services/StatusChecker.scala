/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.platformstatusfrontend.services

import javax.inject.Singleton
import org.mongodb.scala._
import uk.gov.hmrc.platformstatusfrontend.MongoHelpers._
import org.mongodb.scala.model.Filters._


@Singleton
class StatusChecker () {

  val baseIteration1Status = PlatformStatus(name = "iteration1",
    isWorking = true,
    description = "Service up and running in the public zone.")

  val baseIteration2Status = PlatformStatus(name = "iteration2",
    isWorking = true,
    description = "Read and write to Mongo in public zone")

  val baseIteration3Status = PlatformStatus(name = "iteration3",
    isWorking = true,
    description = "Call through to service in protected zone that can read/write to protected Mongo")

  val baseIteration4Status = PlatformStatus(name = "iteration4",
    isWorking = true,
    description = "Call through to service in protected zone that can call out to internet via squid")

  val baseIteration5Status = PlatformStatus(name = "iteration5",
    isWorking = true,
    description = "Call through to service in protected zone that can call a HOD via DES")


  def iteration1Status(): PlatformStatus = baseIteration1Status


  def iteration2Status(dbUrl: String): PlatformStatus = {
    try {
      checkMongoConnection(dbUrl)
      baseIteration2Status
    } catch {
      case ex: IllegalStateException => baseIteration2Status.copy(isWorking = false, reason = Some(ex.getMessage))
    }
  }

  def iteration3Status() = baseIteration3Status.copy(isWorking = false, reason = Some("Not yet implemented"))
  def iteration4Status() = baseIteration3Status.copy(isWorking = false, reason = Some("Not yet implemented"))
  def iteration5Status() = baseIteration3Status.copy(isWorking = false, reason = Some("Not yet implemented"))



  def checkMongoConnection(dbUrl: String): Boolean = {
    val mongoClient: MongoClient = MongoClient(dbUrl)
    val database: MongoDatabase = mongoClient.getDatabase("platform-status-frontend")
    val collection: MongoCollection[Document] = database.getCollection("status");

    val doc: Document = Document("_id" -> 0, "name" -> "MongoDB")
    try {
      collection.replaceOne(equal(fieldName = "_id", value = 0), doc).results(secondsToWait = 2)
    } catch {
      case ex: Exception => throw new IllegalStateException("Failed to connect to MongoDB", ex)
    }
    true
  }

}
