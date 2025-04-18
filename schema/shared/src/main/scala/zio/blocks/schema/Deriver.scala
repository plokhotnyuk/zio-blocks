package zio.blocks.schema

import zio.blocks.schema.binding.{Binding, HasBinding}

final case class Instances[TC[_], A](automatic: () => TC[A], custom: Option[() => TC[A]]) {
  def derivation: TC[A] = custom.getOrElse(automatic)()
}

object Instances {}

trait HasDerivation[F[_, _], TC[_]] {
  def derivation[T, A](fa: F[T, A]): Instances[TC, A]
}

trait Deriver[TC[_]] { self =>
  type HasDerivation[F[_, _]] = zio.blocks.schema.HasDerivation[F, TC]

  final def binding[F[_, _], T, A](fa: F[T, A])(implicit F: HasBinding[F]): Binding[T, A] = F.binding(fa)

  final def derivation[F[_, _], T, A](fa: F[T, A])(implicit D: HasDerivation[F]): Instances[TC, A] = D.derivation(fa)

  def derivePrimitive[F[_, _], A](prim: Reflect.Primitive[F, A]): TC[A]

  def deriveRecord[F[_, _], A](
    fields: Seq[Term[F, A, ?]],
    typeName: TypeName[A],
    doc: Doc,
    modifiers: Seq[Modifier.Record]
  )(implicit F: HasBinding[F]): TC[A]

  def deriveVariant[F[_, _], A](
    cases: Seq[Term[F, A, ?]],
    typeName: TypeName[A],
    doc: Doc,
    modifiers: Seq[Modifier.Variant]
  )(implicit B: HasBinding[F], D: HasDerivation[F]): TC[A]

  def deriveSeq[F[_, _], C[_], A](
    element: Reflect[F, A],
    typeName: TypeName[C[A]],
    doc: Doc,
    modifiers: Seq[Modifier.Seq]
  )(implicit B: HasBinding[F], D: HasDerivation[F]): TC[C[A]]

  def deriveMap[F[_, _], M[_, _], K, V](
    key: Reflect[F, K],
    value: Reflect[F, V],
    typeName: TypeName[M[K, V]],
    doc: Doc,
    modifiers: Seq[Modifier.Map]
  )(implicit B: HasBinding[F], D: HasDerivation[F]): TC[M[K, V]]
}
