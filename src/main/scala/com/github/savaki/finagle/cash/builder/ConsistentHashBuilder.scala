package com.github.savaki.finagle.cash.builder

import com.github.savaki.finagle.cash.HashFunction
import com.twitter.finagle.{Codec, CodecFactory, Service}
import com.twitter.finagle.builder.{ClientBuilder, IncompleteSpecification}
import scala.annotation.implicitNotFound

/**
 * @author matt
 */

class ConsistentHashBuilder[IN, OUT, KEY, HasCodec, HasHashFunction] private[cash](config: ConsistentHashConfig[IN, OUT, KEY, HasCodec, HasHashFunction]) {

  import ConsistentHashConfig._

  // Convenient aliases.
  type FullySpecifiedConfig = FullySpecified[IN, OUT, KEY]
  type ThisConfig = ConsistentHashConfig[IN, OUT, KEY, HasCodec, HasHashFunction]
  type This = ConsistentHashBuilder[IN, OUT, KEY, HasCodec, HasHashFunction]

  def hash[KEY1](hashFunction: HashFunction[KEY1]) = {
    new ConsistentHashBuilder[IN, OUT, KEY1, HasCodec, Yes](config.copy(hashFunction = Option(hashFunction)))
  }

  def codec[IN1, OUT1](c: Codec[IN1, OUT1]) = {
    new ConsistentHashBuilder[IN1, OUT1, KEY, Yes, HasHashFunction](config.copy(codecFactory = Option(c)))
  }

/*
  def build(implicit THE_BUILDER_IS_NOT_FULLY_SPECIFIED_SEE_ClientBuilder_DOCUMENTATION: ConsistentHashConfigEvidence[HasCodec, HasHashFunction]): Service[IN, OUT] = {
    ClientBuilder()
      .codec(config.codecFactory.get)
      .build()
  }
*/
}

object ConsistentHashBuilder {
  def apply() = new ConsistentHashBuilder(new ConsistentHashConfig())
}

private[builder] final case class ConsistentHashConfig[IN, OUT, KEY, HasCodec, HasHashFunction](
                                                                                                 hashFunction: Option[HashFunction[KEY]] = None,
                                                                                                 codecFactory: Option[Codec[IN, OUT]] = None
                                                                                                 ) {

  import ConsistentHashConfig._

  def validated: FullySpecified[IN, OUT, KEY] = {
    hashFunction.getOrElse(throw new IncompleteSpecification("No hash function was specified"))
    codecFactory.getOrElse(throw new IncompleteSpecification("No codec was specified"))
    this.asInstanceOf[FullySpecified[IN, OUT, KEY]]
  }
}

object ConsistentHashConfig {

  sealed abstract trait Yes

  type FullySpecified[IN, OUT, KEY] = ConsistentHashBuilder[IN, OUT, KEY, Yes, Yes]
}

@implicitNotFound("Builder is not fully configured: Codec: ${HasCodec}, HasHashFunction: ${HasHashFunction}")
private[builder] trait ConsistentHashConfigEvidence[HasCodec, HasHashFunction]

private[builder] object ConsistentHashConfigEvidence {

  implicit object FullyConfigured extends ConsistentHashConfigEvidence[ConsistentHashConfig.Yes, ConsistentHashConfig.Yes]

}
