package com.itv.chuckwagon.sbt

import com.itv.aws.lambda._
import com.itv.aws.s3._
import com.itv.chuckwagon.sbt.ChuckwagonBasePlugin.autoImport._
import com.itv.chuckwagon.sbt.LoggingUtils.logMessage
import fansi.Color.Green
import fansi.Str
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import sbt._
import sbt.Keys._
import complete.DefaultParsers._

object ChuckwagonProductionPlugin extends AutoPlugin {

  override def requires = com.itv.chuckwagon.sbt.ChuckwagonBasePlugin

  object autoImport extends Keys.Production

  import autoImport._

  override lazy val projectSettings =
    Seq(
      chuckEnvironments := chuckDefineEnvironments("blue-prd", "prd"),
      chuckPublishCopyFrom := {
        val args: Seq[String] = spaceDelimited("<arg>").parsed

        val downloadablePublishedLambda: DownloadablePublishedLambda =
          com.itv.chuckwagon.deploy
            .getDownloadablePublishedLambdaVersion(
              LambdaName(chuckLambdaName.value),
              AliasName(args.head)
            )
            .foldMap(chuckSDKFreeCompiler.value.compiler)

        val httpClient = HttpClients.createDefault()
        val responseEntity =
          httpClient.execute(new HttpGet(downloadablePublishedLambda.downloadableLocation.value)).getEntity
        val is = responseEntity.getContent

        try {
          val alias =
            com.itv.chuckwagon.deploy
              .uploadAndPublishLambdaToAlias(
                downloadablePublishedLambda.publishedLambda.lambda,
                BucketName(chuckJarStagingBucketName.value),
                PutInputStream(
                  S3Key(s"${chuckJarStagingBucketKeyPrefix.value}${chuckLambdaName.value}-copy.jar"),
                  responseEntity.getContent,
                  responseEntity.getContentLength
                ),
                chuckEnvironments.value.head.aliasName
              )
              .foldMap(chuckSDKFreeCompiler.value.compiler)

          streams.value.log.info(
            logMessage(
              (Str("Just Published Version '") ++ Green(
                alias.lambdaVersion.value.toString
              ) ++ Str("' to Environment '") ++ Green(alias.name.value) ++ Str(
                "' as '"
              ) ++ Green(alias.arn.value) ++ Str("'")).render
            )
          )
        } finally {
          is.close()
        }
      }
    )
}
