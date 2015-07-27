/*                     __                                               *\
**     ________ ___   / /  ___      __ ____  Scala.js API               **
**    / __/ __// _ | / /  / _ | __ / // __/  (c) 2013-2015, LAMP/EPFL   **
**  __\ \/ /__/ __ |/ /__/ __ |/_// /_\ \    http://scala-lang.org/     **
** /____/\___/_/ |_/____/_/ | |__/ /____/                               **
**                          |/____/                                     **
\*                                                                      */

/**
 * TODO: this is copied from the current working code at EPFL, to help test type
 * unions. This is likely to be released into the main line in due course, at
 * which point this file can simply be deleted.
 */

package scala.scalajs.js

import scala.language.implicitConversions

/** Value of type A or B.
 *
 *  In a type system with union types, this would really be `A | B`. Scala does
 *  not have union types, but they are important to many interoperability
 *  scenarios, so we give an encoding via this type.
 */
@scala.scalajs.js.annotation.RawJSType // Don't do this at home!
sealed trait \/[A, B]

object \/ {
  /** Evidence that `A <: B`, taking top-level `\/`-types into account. */
  sealed trait Evidence[-A, +B]

  /** A unique (and typically dead-code-eliminated away) instance of
   *  `Evidence`.
   */
  private object ReusableEvidence extends Evidence[scala.Any, scala.Any]

  abstract sealed class EvidenceLowestPrioImplicits {
    /** If `A <: B2`, then `A <: B1 \/ B2`. */
    implicit def right[A, B1, B2](implicit ev: Evidence[A, B2]): Evidence[A, B1 \/ B2] =
      ReusableEvidence.asInstanceOf[Evidence[A, B1 \/ B2]]
  }

  abstract sealed class EvidenceLowPrioImplicits extends EvidenceLowestPrioImplicits {
    /** `Int <: Double`, because that's true in Scala.js. */
    implicit def intDouble: Evidence[Int, Double] =
      ReusableEvidence.asInstanceOf[Evidence[Int, Double]]

    /** If `A <: B1`, then `A <: B1 \/ B2`. */
    implicit def left[A, B1, B2](implicit ev: Evidence[A, B1]): Evidence[A, B1 \/ B2] =
      ReusableEvidence.asInstanceOf[Evidence[A, B1 \/ B2]]
  }

  object Evidence extends EvidenceLowPrioImplicits {
    /** `A <: A`. */
    implicit def base[A]: Evidence[A, A] =
      ReusableEvidence.asInstanceOf[Evidence[A, A]]

    /** If `A1 <: B` and `A2 <: B`, then `A1 \/ A2 <: B`. */
    implicit def allSubtypes[A1, A2, B](
        implicit ev1: Evidence[A1, B], ev2: Evidence[A2, B]): Evidence[A1 \/ A2, B] =
      ReusableEvidence.asInstanceOf[Evidence[A1 \/ A2, B]]
  }

  /** Upcast `A` to `B1 \/ B2`.
   *
   *  This needs evidence that `A <: B1 \/ B2`.
   */
  implicit def from[A, B1, B2](a: A)(implicit ev: Evidence[A, B1 \/ B2]): B1 \/ B2 =
    a.asInstanceOf[B1 \/ B2]
}