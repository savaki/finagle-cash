package com.github.savaki.finagle.cash.builder

import com.github.savaki.finagle.cash.HashFunction
import com.twitter.finagle.{CodecFactory, Service}
import com.twitter.finagle.builder.{ClientBuilder, IncompleteSpecification}
import scala.annotation.implicitNotFound

/**
 * @author matt
 */

object ConsistentHashBuilder {
  def apply() = new ConsistentHashBuilder(new ConsistentHashConfig())
}

@implicitNotFound("Builder is not fully configured: Codec: ${HasCodec}, HasHashFunction: ${HasHashFunction}")
private[builder] trait ConsistentHashConfigEvidence[HasCodec, HasHashFunction]

private[builder] object ConsistentHashConfigEvidence {

  implicit object FullyConfigured extends ConsistentHashConfigEvidence[ConsistentHashConfig.Yes, ConsistentHashConfig.Yes]

}

class ConsistentHashBuilder[IN, OUT, HasCodec, HasHashFunction] private[cash](config: ConsistentHashConfig[IN, OUT, HasCodec, HasHashFunction]) {

  import ConsistentHashConfig._

  // Convenient aliases.
  type FullySpecifiedConfig = FullySpecified[IN, OUT]
  type ThisConfig = ConsistentHashConfig[IN, OUT, HasCodec, HasHashFunction]
  type This = ConsistentHashBuilder[IN, OUT, HasCodec, HasHashFunction]

  def hash[IN1](hashFunction: HashFunction[IN1]) = {
    val newConfig: ConsistentHashConfig[IN1, OUT, HasCodec, Yes] = config.asInstanceOf[ConsistentHashConfig[IN1, OUT, HasCodec, Yes]].copy(hashFunction = Option(hashFunction))
    new ConsistentHashBuilder[IN1, OUT, HasCodec, Yes](newConfig)
  }

  def codec[IN1, OUT1](codecFactory: CodecFactory[IN1, OUT1]) = {
    val newConfig: ConsistentHashConfig[IN1, OUT1, Yes, HasHashFunction] = config.asInstanceOf[ConsistentHashConfig[IN1, OUT1, Yes, HasHashFunction]].copy(codecFactory = Option(codecFactory))
    new ConsistentHashBuilder[IN1, OUT1, Yes, HasHashFunction](newConfig)
  }

  def build()(implicit CONSISTENT_HASH_BUILDER_IS_NOT_FULLY_SPECIFIED_SEE_ClientBuilder_DOCUMENTATION: ConsistentHashConfigEvidence[HasCodec, HasHashFunction]): Service[IN, OUT] = {
    ClientBuilder()
      .codec(config.codecFactory.get)
      .hostConnectionLimit(1024)
      .hosts("www.yahoo.com:80")
      .build()
  }
}

private[builder] final case class ConsistentHashConfig[IN, OUT, HasCodec, HasHashFunction](
                                                                                            hashFunction: Option[HashFunction[IN]] = None,
                                                                                            codecFactory: Option[CodecFactory[IN, OUT]] = None
                                                                                            ) {

  import ConsistentHashConfig._

  def validated: FullySpecified[IN, OUT] = {
    hashFunction.getOrElse(throw new IncompleteSpecification("No hash function was specified"))
    codecFactory.getOrElse(throw new IncompleteSpecification("No codec was specified"))
    this.asInstanceOf[FullySpecified[IN, OUT]]
  }
}

object ConsistentHashConfig {

  sealed abstract trait Yes

  type FullySpecified[IN, OUT] = ConsistentHashBuilder[IN, OUT, Yes, Yes]
}
