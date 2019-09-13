package com.admios.stories

import java.time.Duration
import java.util.Properties
import java.util.concurrent.TimeUnit

import org.mongodb.scala.MongoClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object Main extends App {

  val MONGO_URL_PROPERTY = "mongodb.url"

  def readPropertyFile(name: String): Properties = {
    val properties = new Properties()
    val kafkaPropsStream = ClassLoader.getSystemResourceAsStream(name)
    try {
      properties.load(kafkaPropsStream)
    } finally {
      kafkaPropsStream.close()
    }
    properties
  }

  val consumerProperties = readPropertyFile("kafka-stories.properties")
  val consumerService = new KafkaStoryConsumerService("stories", consumerProperties, Duration.ofMinutes(1))

  val mongoProperties = readPropertyFile("mongo-stories.properties")
  val mongoClient = MongoClient(mongoProperties.getProperty(MONGO_URL_PROPERTY))
  try {
    val persistenceService = new MongoStoryPersistenceService(mongoClient)
    consumerService.startConsumer(persistenceService.saveStories)

    val story = persistenceService.getStoriesById("twitter_98765")
    story.onComplete {
      case story@Success(value) => value match {
        case Some(value) => println(s"Story: $story")
        case None => println("No story found.")
      }
      case Failure(exception) => println(exception.getMessage)
    }
    story.wait(TimeUnit.MINUTES.toMillis(1))
  } finally {
    mongoClient.close()
  }
}
