package zio.blocks.schema.binding

/**
 * A {{Discriminator}} is a typeclass that can discriminate between different
 * terms of a sum type, by returning a numerical index that represents which
 * term in the sum type the value represents.
 */
trait Discriminator[-A] {
  def discriminate(a: A): Int
}

object Discriminator {
  def apply[A](implicit d: Discriminator[A]): Discriminator[A] = d

  implicit def option[A]: Discriminator[Option[A]] = _option.asInstanceOf[Discriminator[Option[A]]]

  implicit def either[L, R]: Discriminator[Either[L, R]] = _either.asInstanceOf[Discriminator[Either[L, R]]]

  implicit def `try`[A]: Discriminator[scala.util.Try[A]] = _try.asInstanceOf[Discriminator[scala.util.Try[A]]]

  private[this] val _option: Discriminator[Option[Any]] = new Discriminator[Option[Any]] {
    def discriminate(a: Option[Any]): Int = a match {
      case _: Some[_] => 0
      case _          => 1
    }
  }

  private[this] val _either: Discriminator[Either[Any, Any]] = new Discriminator[Either[Any, Any]] {
    def discriminate(a: Either[Any, Any]): Int = a match {
      case _: Left[_, _] => 0
      case _             => 1
    }
  }

  private[this] val _try: Discriminator[scala.util.Try[Any]] = new Discriminator[scala.util.Try[Any]] {
    def discriminate(a: scala.util.Try[Any]): Int = a match {
      case _: scala.util.Success[_] => 0
      case _                        => 1
    }
  }
}
