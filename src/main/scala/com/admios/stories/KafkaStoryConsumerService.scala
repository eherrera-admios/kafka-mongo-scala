package com.admios.stories

import java.time.Duration
import java.util.Collections.singletonList
import java.util.Properties

import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}

import scala.collection.JavaConverters.iterableAsScalaIterableConverter

class KafkaStoryConsumerService(private val topic: String,
                                private val consumerProperties: Properties,
                                private val timeout: Duration = Duration.ofMillis(Int.MaxValue))
  extends StoryConsumerService {

  override def startConsumer(callback: List[Story] => Unit): Unit = {
    val consumer = new KafkaConsumer[String, String](consumerProperties)
    consumer.subscribe(singletonList(topic))

    try {
      while (true) {
        val records: ConsumerRecords[String, String] = consumer.poll(timeout)
        val stories = records.asScala.map(record => Story(record.key(), record.value()))
        callback(stories)
      }
    } finally {
      consumer.close()
    }
  }
}
