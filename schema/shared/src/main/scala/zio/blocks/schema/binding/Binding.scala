package zio.blocks.schema.binding

import zio.blocks.schema.DynamicValue

/**
 * A binding is used to attach non-serializable Scala functions, such as
 * constructors, deconstructors, and matchers, to a reflection type.
 *
 * The {{Binding}} type is indexed by `T`, which is a phantom type that
 * represents the type of binding. The type `A` represents the type of the
 * reflection type.
 *
 * So, for example, `Binding[BindingType.Record, Int]` represents a binding for
 * the reflection type `Int` that has a record binding (and therefore, both a
 * constructor and a deconstructor).
 */
sealed trait Binding[T, A] { self =>

  /**
   * An optional generator for a default value for the type `A`.
   */
  def defaultValue: Option[() => A]

  def defaultValue(value: => A): Binding[T, A]

  /**
   * A user-defined list of example values for the type `A`, to be used for
   * testing and documentation.
   */
  def examples: List[A]

  def examples(value: A, values: A*): Binding[T, A]
}
object Binding {
  type Unused[T, A] = Nothing

  final case class Primitive[A](
    defaultValue: Option[() => A] = None,
    examples: List[A] = Nil
  ) extends Binding[BindingType.Primitive, A] {
    def defaultValue(value: => A): Primitive[A] = copy(defaultValue = Some(() => value))

    def examples(value: A, values: A*): Primitive[A] = copy(examples = value :: values.toList)
  }
  object Primitive {
    val unit: Primitive[Unit] = Primitive[Unit]()

    val boolean: Primitive[Boolean] = Primitive[Boolean]()

    val byte: Primitive[Byte] = Primitive[Byte]()

    val short: Primitive[Short] = Primitive[Short]()

    val int: Primitive[Int] = Primitive[Int]()

    val long: Primitive[Long] = Primitive[Long]()

    val float: Primitive[Float] = Primitive[Float]()

    val double: Primitive[Double] = Primitive[Double]()

    val char: Primitive[Char] = Primitive[Char]()

    val string: Primitive[String] = Primitive[String]()

    val bigInt: Primitive[BigInt] = Primitive[BigInt]()

    val bigDecimal: Primitive[BigDecimal] = Primitive[BigDecimal]()

    val dayOfWeek: Primitive[java.time.DayOfWeek] = Primitive[java.time.DayOfWeek]()

    val duration: Primitive[java.time.Duration] = Primitive[java.time.Duration]()

    val instant: Primitive[java.time.Instant] = Primitive[java.time.Instant]()

    val localDate: Primitive[java.time.LocalDate] = Primitive[java.time.LocalDate]()

    val localDateTime: Primitive[java.time.LocalDateTime] = Primitive[java.time.LocalDateTime]()

    val localTime: Primitive[java.time.LocalTime] = Primitive[java.time.LocalTime]()

    val month: Primitive[java.time.Month] = Primitive[java.time.Month]()

    val monthDay: Primitive[java.time.MonthDay] = Primitive[java.time.MonthDay]()

    val offsetDateTime: Primitive[java.time.OffsetDateTime] = Primitive[java.time.OffsetDateTime]()

    val offsetTime: Primitive[java.time.OffsetTime] = Primitive[java.time.OffsetTime]()

    val period: Primitive[java.time.Period] = Primitive[java.time.Period]()

    val year: Primitive[java.time.Year] = Primitive[java.time.Year]()

    val yearMonth: Primitive[java.time.YearMonth] = Primitive[java.time.YearMonth]()

    val zoneId: Primitive[java.time.ZoneId] = Primitive[java.time.ZoneId]()

    val zoneOffset: Primitive[java.time.ZoneOffset] = Primitive[java.time.ZoneOffset]()

    val zonedDateTime: Primitive[java.time.ZonedDateTime] = Primitive[java.time.ZonedDateTime]()

    val currency: Primitive[java.util.Currency] = Primitive[java.util.Currency]()

    val uuid: Primitive[java.util.UUID] = Primitive[java.util.UUID]()
  }

  final case class Record[A](
    constructor: Constructor[A],
    deconstructor: Deconstructor[A],
    defaultValue: Option[() => A] = None,
    examples: List[A] = Nil
  ) extends Binding[BindingType.Record, A] {
    def transform[B](f: A => B)(g: B => A): Record[B] = Record(
      constructor.map(f),
      deconstructor.contramap(g),
      defaultValue.map(thunk => () => f(thunk())),
      examples.map(f)
    )

    def defaultValue(value: => A): Record[A] = copy(defaultValue = Some(() => value))

    def examples(value: A, values: A*): Record[A] = copy(examples = value :: values.toList)
  }
  object Record {
    def apply[A](implicit r: Record[A]): Record[A] = r

    def of[A]: Record[A] = _of.asInstanceOf[Record[A]]

    def some[A]: Record[Some[A]] = Record(Constructor.of[A].map(Some(_)), Deconstructor.of[A].contramap(_.value))

    val none: Record[None.type] = Record(Constructor.none, Deconstructor.none)

    val int: Record[Int] = Record(Constructor.int, Deconstructor.int)

    def left[A, B]: Record[Left[A, B]] = _left.asInstanceOf[Record[Left[A, B]]]

    def right[A, B]: Record[Right[A, B]] = _right.asInstanceOf[Record[Right[A, B]]]

    def tuple2[A, B]: Record[(A, B)] = _tuple2.asInstanceOf[Record[(A, B)]]

    def tuple3[A, B, C]: Record[(A, B, C)] = _tuple3.asInstanceOf[Record[(A, B, C)]]

    def tuple4[A, B, C, D]: Record[(A, B, C, D)] = _tuple4.asInstanceOf[Record[(A, B, C, D)]]

    def tuple5[A, B, C, D, E]: Record[(A, B, C, D, E)] = _tuple5.asInstanceOf[Record[(A, B, C, D, E)]]

    def tuple6[A, B, C, D, E, F]: Record[(A, B, C, D, E, F)] = _tuple6.asInstanceOf[Record[(A, B, C, D, E, F)]]

    private val _of = new Record[AnyRef](Constructor.of[AnyRef], Deconstructor.of[AnyRef])

    private val _left = new Record[Left[AnyRef, AnyRef]](
      Constructor.of[AnyRef].map(Left(_)),
      Deconstructor.of[AnyRef].contramap(_.value)
    )

    private val _right = new Record[Right[AnyRef, AnyRef]](
      Constructor.of[AnyRef].map(Right(_)),
      Deconstructor.of[AnyRef].contramap(_.value)
    )

    private val _tuple2 = new Record[(AnyRef, AnyRef)](
      Constructor.tuple2[AnyRef, AnyRef](Constructor.of[AnyRef], Constructor.of[AnyRef]),
      Deconstructor.tuple2[AnyRef, AnyRef](Deconstructor.of[AnyRef], Deconstructor.of[AnyRef])
    )

    private val _tuple3 = new Record[(AnyRef, AnyRef, AnyRef)](
      Constructor
        .tuple3[AnyRef, AnyRef, AnyRef](Constructor.of[AnyRef], Constructor.of[AnyRef], Constructor.of[AnyRef]),
      Deconstructor
        .tuple3[AnyRef, AnyRef, AnyRef](Deconstructor.of[AnyRef], Deconstructor.of[AnyRef], Deconstructor.of[AnyRef])
    )

    private val _tuple4 = new Record[(AnyRef, AnyRef, AnyRef, AnyRef)](
      Constructor.tuple4[AnyRef, AnyRef, AnyRef, AnyRef](
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef]
      ),
      Deconstructor.tuple4[AnyRef, AnyRef, AnyRef, AnyRef](
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef]
      )
    )

    private val _tuple5 = new Record[(AnyRef, AnyRef, AnyRef, AnyRef, AnyRef)](
      Constructor.tuple5[AnyRef, AnyRef, AnyRef, AnyRef, AnyRef](
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef]
      ),
      Deconstructor.tuple5[AnyRef, AnyRef, AnyRef, AnyRef, AnyRef](
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef]
      )
    )

    private val _tuple6 = new Record[(AnyRef, AnyRef, AnyRef, AnyRef, AnyRef, AnyRef)](
      Constructor.tuple6[AnyRef, AnyRef, AnyRef, AnyRef, AnyRef, AnyRef](
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef],
        Constructor.of[AnyRef]
      ),
      Deconstructor.tuple6[AnyRef, AnyRef, AnyRef, AnyRef, AnyRef, AnyRef](
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef],
        Deconstructor.of[AnyRef]
      )
    )
  }
  final case class Variant[A](
    discriminator: Discriminator[A],
    matchers: Matchers[A],
    defaultValue: Option[() => A] = None,
    examples: List[A] = Nil
  ) extends Binding[BindingType.Variant, A] {
    def defaultValue(value: => A): Variant[A] = copy(defaultValue = Some(() => value))

    def examples(value: A, values: A*): Variant[A] = copy(examples = value :: values.toList)
  }
  object Variant {
    def apply[A](implicit v: Variant[A]): Variant[A] = v

    def option[A]: Variant[Option[A]] = Variant(Discriminator.option[A], Matchers(Matcher.some, Matcher.none))

    def either[L, R]: Variant[Either[L, R]] = Variant(Discriminator.either[L, R], Matchers(Matcher.left, Matcher.right))
  }

  final case class Seq[C[_], A](
    constructor: SeqConstructor[C],
    deconstructor: SeqDeconstructor[C],
    defaultValue: Option[() => C[A]] = None,
    examples: List[C[A]] = Nil
  ) extends Binding[BindingType.Seq[C], C[A]] {
    def defaultValue(value: => C[A]): Seq[C, A] = copy(defaultValue = Some(() => value))

    def examples(value: C[A], values: C[A]*): Seq[C, A] = copy(examples = value :: values.toList)
  }
  object Seq {
    def apply[C[_], A](implicit s: Seq[C, A]): Seq[C, A] = s

    def set[A]: Seq[Set, A] = Seq(SeqConstructor.setConstructor, SeqDeconstructor.setDeconstructor)

    def list[A]: Seq[List, A] = Seq(SeqConstructor.listConstructor, SeqDeconstructor.listDeconstructor)

    def vector[A]: Seq[Vector, A] = Seq(SeqConstructor.vectorConstructor, SeqDeconstructor.vectorDeconstructor)

    def array[A]: Seq[Array, A] = Seq(SeqConstructor.arrayConstructor, SeqDeconstructor.arrayDeconstructor)
  }

  final case class Map[M[_, _], K, V](
    constructor: MapConstructor[M],
    deconstructor: MapDeconstructor[M],
    defaultValue: Option[() => M[K, V]] = None,
    examples: List[M[K, V]] = Nil
  ) extends Binding[BindingType.Map[M], M[K, V]] {
    def defaultValue(value: => M[K, V]): Map[M, K, V] = copy(defaultValue = Some(() => value))

    def examples(value: M[K, V], values: M[K, V]*): Map[M, K, V] = copy(examples = value :: values.toList)
  }
  object Map {
    def map[K, V]: Map[Predef.Map, K, V] = Map(MapConstructor.map, MapDeconstructor.map)
  }

  final case class Dynamic(
    defaultValue: Option[() => DynamicValue] = None,
    examples: List[DynamicValue] = Nil
  ) extends Binding[BindingType.Dynamic, DynamicValue] {
    def defaultValue(value: => DynamicValue): Dynamic = copy(defaultValue = Some(() => value))

    def examples(value: DynamicValue, values: DynamicValue*): Dynamic = copy(examples = value :: values.toList)
  }

  implicit val bindingHasBinding: HasBinding[Binding] = new HasBinding[Binding] {
    def binding[T, A](fa: Binding[T, A]): Binding[T, A] = fa

    def updateBinding[T, A](fa: Binding[T, A], f: Binding[T, A] => Binding[T, A]): Binding[T, A] = f(fa)
  }

  implicit val bindingFromBinding: FromBinding[Binding] = new FromBinding[Binding] {
    def fromBinding[T, A](binding: Binding[T, A]): Binding[T, A] = binding
  }
}
