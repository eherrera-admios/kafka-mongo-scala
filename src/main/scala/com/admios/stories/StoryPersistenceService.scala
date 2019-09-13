package com.admios.stories

import scala.concurrent.Future

trait StoryPersistenceService {
  def saveStories(stories: List[Story])

  def getStoriesById(id: String): Future[Option[Story]]
}
