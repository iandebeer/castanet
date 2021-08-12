package ee.mn8.castanet
import cats.effect.*
import cats.effect.std.Dispatcher
import cats.implicits._
import ciris._
import ciris.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.collection.MinSize
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.types.net.UserPortNumber
import eu.timepit.refined.types.string.NonEmptyString 
import scala.concurrent.duration._
import cats.implicits._
import com.typesafe.config.ConfigFactory
import java.time.Period

import lt.dvim.ciris.Hocon._


case class ServiceConfig(host: NonEmptyString, port: UserPortNumber, useHttps: Boolean)
case class Rate(elements: Int, burstDuration: FiniteDuration, checkInterval: Period)


/*
service-conf {
  host = "example.com"
  port-number = 8080
  use-https = true
}


*/
object Conf extends IOApp.Simple:
  val config1 = ConfigFactory.parseString("""
                                           |rate {
                                           |  elements = 2
                                           |  burst-duration = 100 millis
                                           |  check-interval = 2 weeks
                                           |}
      """.stripMargin)
  val hocon1 = hoconAt(config1)("rate")

  val run =
    for {
      _ <- IO.println("hello")
      c1 <- (
        hocon1("elements").as[Int],
        hocon1("burst-duration").as[FiniteDuration],
        hocon1("check-interval").as[Period]
      ).parMapN(Rate.apply)
        .load[IO]
      _ <- IO.println(c1)
        
    } yield ()

  