package com.github.savaki.finagle.cash.builder

import com.github.savaki.finagle.cash.HashFunction
import com.twitter.finagle.{CodecFactory, Service}
import com.twitter.finagle.builder.{ClientBuilder, IncompleteSpecification}
import scala.annotation.implicitNotFound

/**
 * @author matt
 */

object CashBuilder {
  def apply() = new CashBuilder(new CashConfig())
}

@implicitNotFound("Builder is not fully configured: Codec: ${HasCodec}, HasHashFunction: ${HasHashFunction}")
private[builder] trait
CashConfigEvidence[HasCodec, HasHashFunction]

private[builder] object CashConfigEvidence {

  implicit object FullyConfigured extends CashConfigEvidence[CashConfig.Yes, CashConfig.Yes]

}

class CashBuilder[IN, OUT, HasCodec, HasHashFunction] private[cash](config: CashConfig[IN, OUT, HasCodec, HasHashFunction]) {

  import CashConfig._

  // Convenient aliases.
  type FullySpecifiedConfig = FullySpecified[IN, OUT]
  type ThisConfig = CashConfig[IN, OUT, HasCodec, HasHashFunction]
  type This = CashBuilder[IN, OUT, HasCodec, HasHashFunction]

  def hash[IN1](hashFunction: HashFunction) = {
    val newConfig: CashConfig[IN1, OUT, HasCodec, Yes] = config.asInstanceOf[CashConfig[IN1, OUT, HasCodec, Yes]].copy(hashFunction = Option(hashFunction))
    new CashBuilder[IN1, OUT, HasCodec, Yes](newConfig)
  }

  def codec[IN1, OUT1](codecFactory: CodecFactory[IN1, OUT1]) = {
    val newConfig: CashConfig[IN1, OUT1, Yes, HasHashFunction] = config.asInstanceOf[CashConfig[IN1, OUT1, Yes, HasHashFunction]].copy(codecFactory = Option(codecFactory))
    new CashBuilder[IN1, OUT1, Yes, HasHashFunction](newConfig)
  }

  def build()(implicit CONSISTENT_HASH_BUILDER_IS_NOT_FULLY_SPECIFIED_SEE_ClientBuilder_DOCUMENTATION: CashConfigEvidence[HasCodec, HasHashFunction]): Service[IN, OUT] = {
    ClientBuilder()
      .codec(config.codecFactory.get)
      .hostConnectionLimit(1024)
      .hosts("www.yahoo.com:80")
      .build()
  }
}

private[builder] final case class CashConfig[IN, OUT, HasCodec, HasHashFunction](
                                                                                            hashFunction: Option[HashFunction] = None,
                                                                                            codecFactory: Option[CodecFactory[IN, OUT]] = None
                                                                                            ) {

  import CashConfig._

  def validated: FullySpecified[IN, OUT] = {
    hashFunction.getOrElse(throw new IncompleteSpecification("No hash function was specified"))
    codecFactory.getOrElse(throw new IncompleteSpecification("No codec was specified"))
    this.asInstanceOf[FullySpecified[IN, OUT]]
  }
}

object CashConfig {

  sealed abstract trait Yes

  type FullySpecified[IN, OUT] = CashBuilder[IN, OUT, Yes, Yes]
}
