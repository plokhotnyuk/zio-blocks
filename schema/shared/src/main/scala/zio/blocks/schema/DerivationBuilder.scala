package zio.blocks.schema

/**
 * A {{DerivationBuilder}} is capable of deriving a type class instance for any
 * data type that has a schema. Though instances for all substructures can be
 * derived automatically, you can also specify instances for specific
 * substructures using the `withOverride` method.
 *
 * TODO: Add support for attaching other things here.
 *
 * {{ val personSchema = Schema.derive[Person]
 *
 * val personEq: Eq[Person] = personSchema .deriving[Eq] .instance(Person.age,
 * Eq[Int]) .modifier(Person.name, Term.transient) .derive }}
 */
final case class DerivationBuilder[TC[_], A](
  schema: Schema[A],
  deriver: Deriver[TC],
  instanceOverrides: IndexedSeq[DeriveOverride[TC, A, ?]],
  modifierOverrides: IndexedSeq[ModifierOverride[A, ?]]
) {
  def instance[B](optic: Optic.Bound[A, B], instance: TC[B]): DerivationBuilder[TC, A] = {
    val override_ = DeriveOverride(optic, instance)
    copy(instanceOverrides = instanceOverrides :+ override_)
  }

  def modifier[B](optic: Optic.Bound[A, B], modifier: Modifier): DerivationBuilder[TC, A] = {
    val override_ = ModifierOverride(optic, modifier)
    copy(modifierOverrides = modifierOverrides :+ override_)
  }

  def derive: TC[A] =
    ???
}
