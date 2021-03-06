package com.itv.aws

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEvents
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsClientBuilder
import com.itv.aws.iam.ARN

package object events {

  val RULE_TARGET_ID = "1"

  def events: AwsClientBuilder[AmazonCloudWatchEvents] =
    configuredClientForRegion(AmazonCloudWatchEventsClientBuilder.standard())
}

package events {

  case class RuleName(value: String) extends AnyVal

  case class ScheduleExpression(value: String) extends AnyVal

  case class EventRule(name: RuleName, scheduleExpression: ScheduleExpression, description: String)
  case class CreatedEventRule(eventRule: EventRule, arn: ARN)
}
