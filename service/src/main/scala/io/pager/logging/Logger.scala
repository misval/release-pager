package io.pager.logging

import io.pager.ThrowableOps._
import zio.{ Has, UIO, ULayer, URLayer, ZLayer }
import zio.clock._
import zio.console.{ Console => ConsoleZIO }

object Logger {
  type Logger = Has[Service]

  trait Service {
    def trace(message: => String): UIO[Unit]

    def debug(message: => String): UIO[Unit]

    def info(message: => String): UIO[Unit]

    def warn(message: => String): UIO[Unit]

    def error(message: => String): UIO[Unit]

    def error(t: Throwable)(message: => String): UIO[Unit]
  }

  class Console(clock: Clock.Service, console: ConsoleZIO.Service) extends Service {
    def error(message: => String): UIO[Unit] = print(message)

    def warn(message: => String): UIO[Unit] = print(message)

    def info(message: => String): UIO[Unit] = print(message)

    def debug(message: => String): UIO[Unit] = print(message)

    def trace(message: => String): UIO[Unit] = print(message)

    def error(t: Throwable)(message: => String): UIO[Unit] =
      for {
        _ <- print(message)
        _ <- console.putStrLn(t.stackTrace)
      } yield ()

    private def print(message: => String): UIO[Unit] =
      for {
        timestamp <- clock.currentDateTime.orDie // TODO orDie
        _         <- console.putStrLn(s"[$timestamp] $message")
      } yield ()
  }

  val console: URLayer[Clock with ConsoleZIO, Has[Service]] =
    ZLayer.fromServices[Clock.Service, ConsoleZIO.Service, Service] { (clock, console) =>
      new Console(clock, console)
    }

  class Silent extends Service {
    def trace(message: => String): UIO[Unit]               = UIO.unit
    def debug(message: => String): UIO[Unit]               = UIO.unit
    def info(message: => String): UIO[Unit]                = UIO.unit
    def warn(message: => String): UIO[Unit]                = UIO.unit
    def error(message: => String): UIO[Unit]               = UIO.unit
    def error(t: Throwable)(message: => String): UIO[Unit] = UIO.unit
  }

  val silent: ULayer[Logger] = ZLayer.succeed(new Silent)
}
