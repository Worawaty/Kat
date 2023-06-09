/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.features

import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.test.*
import kotlin.test.Test
import kotlin.test.assertEquals

class BinaryPayloadExampleTest {
    @Serializable(BinaryPayload.Companion::class)
    class BinaryPayload(val req: ByteArray, val res: ByteArray) {
        companion object : KSerializer<BinaryPayload> {
            override val descriptor: SerialDescriptor = buildClassSerialDescriptor("BinaryPayload") {
                element("req", ByteArraySerializer().descriptor)
                element("res", ByteArraySerializer().descriptor)
            }

            override fun serialize(encoder: Encoder, value: BinaryPayload) {
                val compositeOutput = encoder.beginStructure(descriptor)
                compositeOutput.encodeStringElement(descriptor, 0, InternalHexConverter.printHexBinary(value.req))
                compositeOutput.encodeStringElement(descriptor, 1, InternalHexConverter.printHexBinary(value.res))
                compositeOutput.endStructure(descriptor)
            }

            override fun deserialize(decoder: Decoder): BinaryPayload {
                val dec: CompositeDecoder = decoder.beginStructure(descriptor)
                var req: ByteArray? = null // consider using flags or bit mask if you
                var res: ByteArray? = null // need to read nullable non-optional properties
                loop@ while (true) {
                    when (val i = dec.decodeElementIndex(descriptor)) {
                        CompositeDecoder.DECODE_DONE -> break@loop
                        0 -> req = InternalHexConverter.parseHexBinary(dec.decodeStringElement(descriptor, i))
                        1 -> res = InternalHexConverter.parseHexBinary(dec.decodeStringElement(descriptor, i))
                        else -> throw SerializationException("Unknown index $i")
                    }
                }
                dec.endStructure(descriptor)
                return BinaryPayload(
                    req ?: throw SerializationException("MFE: req"),
                    res ?: throw SerializationException("MFE: res")
                )
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as BinaryPayload

            if (!req.contentEquals(other.req)) return false
            if (!res.contentEquals(other.res)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = req.contentHashCode()
            result = 31 * result + res.contentHashCode()
            return result
        }
    }

    @Test
    fun payloadEquivalence() {
        val payload1 = BinaryPayload(byteArrayOf(0, 0, 0), byteArrayOf(127, 127))
        val s = Json.encodeToString(BinaryPayload.serializer(), payload1)
        assertEquals("""{"req":"000000","res":"7F7F"}""", s)
        val payload2 = Json.decodeFromString(BinaryPayload.serializer(), s)
        assertEquals(payload1, payload2)
    }
}
