package com.admios.stories

import com.mongodb.BasicDBObject
import org.mongodb.scala.MongoClient
import org.mongodb.scala.model.Filters

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MongoStoryPersistenceService(private val client: MongoClient,
                                   private val dbName: String = "stories",
                                   private val collectionName: String = "stories")
  extends StoryPersistenceService {

  override def saveStories(stories: List[Story]): Unit = {
    val db = client.getDatabase(dbName)
    val collection = db.getCollection[BasicDBObject](collectionName)
    val documents = stories.map(story => new BasicDBObject()
      .append("_id", story.id)
      .append("createdAt", story.createdAt))
    collection.insertMany(documents)
  }

  override def getStoriesById(id: String): Future[Option[Story]] = {
    val db = client.getDatabase(dbName)
    val collection = db.getCollection[BasicDBObject](collectionName)
    collection.find(Filters.equal("_id", id))
      .toFuture()
      .map(docs => docs.map(doc => Story(doc.getString("_id"), doc.getString("createAt"))))
      .map(stories => stories.headOption)
  }
}
