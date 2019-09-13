package com.admios.stories

trait StoryConsumerService {
  def startConsumer(callback: List[Story] => Unit)
}
