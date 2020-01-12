package io

import zio.{ Ref, UIO }
import zio.random.Random
import zio.test.{ Sized, ZSpec }

package object pager {
  type TestScenarios = List[ZSpec[Random with Sized, Throwable, String, Unit]]

  def mkMap[K, V](map: Map[K, V]): UIO[Ref[Map[K, V]]] = Ref.make(map)
  def emptyMap[K, V]: UIO[Ref[Map[K, V]]]              = mkMap(Map.empty[K, V])
}
