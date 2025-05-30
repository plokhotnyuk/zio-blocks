package zio.blocks.schema

import monocle.{Focus, PLens, POptional}
import monocle.macros.GenPrism
import org.openjdk.jmh.annotations._
import zio.blocks.schema.binding.Binding

class LensGetBenchmark extends BaseBenchmark {
  import LensDomain._

  var a: A = A(B(C(D(E("test")))))

  @Benchmark
  def direct: String = a.b.c.d.e.s

  @Benchmark
  def monocle: String = A_.b_c_d_e_s_monocle.get(a)

  @Benchmark
  def zioBlocks: String = A.b_c_d_e_s.get(a)
}

class LensReplaceBenchmark extends BaseBenchmark {
  import LensDomain._

  var a: A = A(B(C(D(E("test")))))

  @Benchmark
  def direct: A = {
    val a = this.a
    val b = a.b
    val c = b.c
    val d = c.d
    a.copy(b = b.copy(c = c.copy(d = d.copy(e = d.e.copy(s = "test2")))))
  }

  @Benchmark
  def monocle: A = A_.b_c_d_e_s_monocle.replace("test2").apply(a)

  @Benchmark
  def quicklens: A = {
    import com.softwaremill.quicklens._

    A_.b_c_d_e_s_quicklens.apply(a).setTo("test2")
  }

  @Benchmark
  def zioBlocks: A = A.b_c_d_e_s.replace(a, "test2")
}

class OptionalGetOptionBenchmark extends BaseBenchmark {
  import OptionalDomain._

  var a1: A1 = A1(B1(C1(D1(E1("test")))))

  @Benchmark
  def direct: Option[String] = {
    a1.b match {
      case b1: B1 =>
        b1.c match {
          case c1: C1 =>
            c1.d match {
              case d1: D1 =>
                d1.e match {
                  case e1: E1 => return new Some(e1.s)
                  case _      =>
                }
              case _ =>
            }
          case _ =>
        }
      case _ =>
    }
    None
  }

  @Benchmark
  def monocle: Option[String] = A1_.b_b1_c_c1_d_d1_e_e1_s_monocle.getOption(a1)

  @Benchmark
  def zioBlocks: Option[String] = A1.b_b1_c_c1_d_d1_e_e1_s.getOption(a1)
}

class OptionalReplaceBenchmark extends BaseBenchmark {
  import OptionalDomain._

  var a1: A1 = A1(B1(C1(D1(E1("test")))))

  @Benchmark
  def direct: A1 = {
    val a1 = this.a1
    a1.b match {
      case b1: B1 =>
        b1.c match {
          case c1: C1 =>
            c1.d match {
              case d1: D1 =>
                d1.e match {
                  case e1: E1 => return a1.copy(b = b1.copy(c = c1.copy(d = d1.copy(e = e1.copy(s = "test2")))))
                  case _      =>
                }
              case _ =>
            }
          case _ =>
        }
      case _ =>
    }
    a1
  }

  @Benchmark
  def monocle: A1 = A1_.b_b1_c_c1_d_d1_e_e1_s_monocle.replace("test2").apply(a1)

  @Benchmark
  def quicklens: A1 = {
    import com.softwaremill.quicklens._

    A1_.b_b1_c_c1_d_d1_e_e1_s_quicklens.apply(a1).setTo("test2")
  }

  @Benchmark
  def zioBlocks: A1 = A1.b_b1_c_c1_d_d1_e_e1_s.replace(a1, "test2")
}

class TraversalFoldBenchmark extends BaseBenchmark {
  import TraversalDomain._

  @Param(Array("1", "10", "100", "1000", "10000"))
  var size: Int = 10

  var ai: Array[Int] = (1 to size).toArray

  @Setup
  def setup(): Unit = ai = (1 to size).toArray

  @Benchmark
  def direct: Int = {
    var res = 0
    var i   = 0
    while (i < ai.length) {
      res += ai(i)
      i += 1
    }
    res
  }

  @Benchmark
  def zioBlocks: Int = a_i.fold[Int](ai)(0, _ + _)
}

class TraversalModifyBenchmark extends BaseBenchmark {
  import TraversalDomain._

  @Param(Array("1", "10", "100", "1000", "10000"))
  var size: Int = 10

  var ai: Array[Int] = (1 to size).toArray

  @Setup
  def setup(): Unit = ai = (1 to size).toArray

  @Benchmark
  def direct: Array[Int] = {
    val res = new Array[Int](ai.length)
    var i   = 0
    while (i < ai.length) {
      res(i) = ai(i) + 1
      i += 1
    }
    res
  }

  @Benchmark
  def quicklens: Array[Int] = {
    import com.softwaremill.quicklens._

    a_i_quicklens.apply(ai).using(_ + 1)
  }

  @Benchmark
  def zioBlocks: Array[Int] = a_i.modify(ai, _ + 1)
}

object LensDomain {
  case class E(s: String)

  object E extends CompanionOptics[E] {
    implicit val schema: Schema[E] = Schema.derived
  }

  case class D(e: E)

  object D extends CompanionOptics[D] {
    implicit val schema: Schema[D] = Schema.derived
  }

  case class C(d: D)

  object C extends CompanionOptics[C] {
    implicit val schema: Schema[C] = Schema.derived
  }

  case class B(c: C)

  object B extends CompanionOptics[B] {
    implicit val schema: Schema[B] = Schema.derived
  }

  case class A(b: B)

  object A extends CompanionOptics[A] {
    implicit val schema: Schema[A] = Schema.derived
    val b_c_d_e_s: Lens[A, String] = optic(_.b.c.d.e.s)
  }

  object A_ {
    import com.softwaremill.quicklens._

    val b_c_d_e_s_quicklens: A => PathModify[A, String] =
      (modify(_: A)(_.b))
        .andThenModify(modify(_: B)(_.c))
        .andThenModify(modify(_: C)(_.d))
        .andThenModify(modify(_: D)(_.e))
        .andThenModify(modify(_: E)(_.s))
    val b_c_d_e_s_monocle: PLens[A, A, String, String] =
      Focus[A](_.b).andThen(Focus[B](_.c)).andThen(Focus[C](_.d)).andThen(Focus[D](_.e)).andThen(Focus[E](_.s))
  }
}

object OptionalDomain {
  sealed trait E

  object E extends CompanionOptics[E] {
    implicit val schema: Schema[E] = Schema.derived
  }

  case class E1(s: String) extends E

  object E1 extends CompanionOptics[E1] {
    implicit val schema: Schema[E1] = Schema.derived
  }

  case class E2(i: Int) extends E

  object E2 extends CompanionOptics[E2] {
    implicit val schema: Schema[E2] = Schema.derived
  }

  sealed trait D

  object D extends CompanionOptics[D] {
    implicit val schema: Schema[D] = Schema.derived
  }

  case class D1(e: E) extends D

  object D1 extends CompanionOptics[D1] {
    implicit val schema: Schema[D1] = Schema.derived
  }

  case class D2(i: Int) extends D

  object D2 extends CompanionOptics[D2] {
    implicit val schema: Schema[D2] = Schema.derived
  }

  sealed trait C

  object C extends CompanionOptics[C] {
    implicit val schema: Schema[C] = Schema.derived
  }

  case class C1(d: D) extends C

  object C1 extends CompanionOptics[C1] {
    implicit val schema: Schema[C1] = Schema.derived
  }

  case class C2(i: Int) extends C

  object C2 extends CompanionOptics[C2] {
    implicit val schema: Schema[C2] = Schema.derived
  }

  sealed trait B

  object B extends CompanionOptics[B] {
    implicit val schema: Schema[B] = Schema.derived
  }

  case class B1(c: C) extends B

  object B1 extends CompanionOptics[B1] {
    implicit val schema: Schema[B1] = Schema.derived
  }

  case class B2(i: Int) extends B

  object B2 extends CompanionOptics[B2] {
    implicit val schema: Schema[B2] = Schema.derived
  }

  case class A1(b: B)

  object A1 extends CompanionOptics[A1] {
    implicit val schema: Schema[A1]                 = Schema.derived
    val b_b1_c_c1_d_d1_e_e1_s: Optional[A1, String] = optic(_.b.when[B1].c.when[C1].d.when[D1].e.when[E1].s)
  }

  object A1_ {
    import com.softwaremill.quicklens._

    val b_b1_c_c1_d_d1_e_e1_s_quicklens: A1 => PathModify[A1, String] =
      (modify(_: A1)(_.b))
        .andThenModify(modify(_: B)(_.when[B1]))
        .andThenModify(modify(_: B1)(_.c))
        .andThenModify(modify(_: C)(_.when[C1]))
        .andThenModify(modify(_: C1)(_.d))
        .andThenModify(modify(_: D)(_.when[D1]))
        .andThenModify(modify(_: D1)(_.e))
        .andThenModify(modify(_: E)(_.when[E1]))
        .andThenModify(modify(_: E1)(_.s))
    val b_b1_c_c1_d_d1_e_e1_s_monocle: POptional[A1, A1, String, String] =
      Focus[A1](_.b)
        .andThen(GenPrism[B, B1])
        .andThen(Focus[B1](_.c))
        .andThen(GenPrism[C, C1])
        .andThen(Focus[C1](_.d))
        .andThen(GenPrism[D, D1])
        .andThen(Focus[D1](_.e))
        .andThen(GenPrism[E, E1])
        .andThen(Focus[E1](_.s))
  }
}

object TraversalDomain {
  import com.softwaremill.quicklens._

  val a_i: Traversal[Array[Int], Int]                          = Traversal.arrayValues(Reflect.int[Binding])
  val a_i_quicklens: Array[Int] => PathModify[Array[Int], Int] = modify(_: Array[Int])(_.each)
}
