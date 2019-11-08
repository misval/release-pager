package io.pager.api.http

import cats.effect.Resource
import io.circe.{ Decoder, Encoder }
import io.pager.PagerError
import io.pager.PagerError.{ MalformedUrl, NotFound }
import org.http4s.Uri
import org.http4s.client.Client
import zio._
import zio.interop.catz._
import org.http4s.circe._
import zio.interop.catz._
import org.http4s._
import org.http4s.circe._

trait HttpClient {
  val httpClient: HttpClient.Service
}

object HttpClient {
  trait Service {
    def get[T](uri: String)(implicit d: Decoder[T]): IO[PagerError, T]
  }

  trait Http4s extends HttpClient {
    def client: Resource[Task, Client[Task]]

    implicit def entityDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[Task, A] = jsonOf[Task, A]
    implicit def entityEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]

    override val httpClient: Service = new Service {
      def get[T](uri: String)(implicit d: Decoder[T]): IO[PagerError, T] = {
        def call(uri: Uri): IO[PagerError, T] =
          client
            .use(_.expect[T](uri))
            .foldM(_ => IO.fail(NotFound(uri.renderString)), ZIO.succeed)

        Uri
          .fromString(uri)
          .fold(_ => IO.fail(MalformedUrl(uri)), call)
      }
    }
  }
}