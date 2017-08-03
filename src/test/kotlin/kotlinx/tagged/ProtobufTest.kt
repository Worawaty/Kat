package kotlinx.tagged

import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.WordSpec
import kotlinx.serialization.*

/**
 *  @author Leonid Startsev
 *          sandwwraith@gmail.com
 */

@Serializable
data class Test1(@SerialId(1) @ProtoType(ProtoNumberType.SIGNED) val a: Int)

@Serializable
data class Test2(@SerialId(1) val a: List<Int>)

@Serializable
data class Test3(@SerialId(2) val b: String)

@Serializable
data class Test4(@SerialId(3) val a: Test1)

@Serializable
data class Test5(@SerialId(42) val b: Int, @SerialId(2) val c: String)

class ProtobufTest : WordSpec() {

    init {
        val t1 = Test1(-150)
        val t2 = Test2(listOf(150, 228, 1337))
        val t3 = Test3("testing")
        val t4 = Test4(t1)
        val t5 = Test5(42, "testing")
        "Protobuf writer" should {
            "write signed integer" {
                ProtoBuf.dumps(t1).toLowerCase() shouldBe "08ab02"
            }
            "write list of varint integers" {
                ProtoBuf.dumps(t2).toUpperCase() shouldBe "08960108E40108B90A"
            }
            "write string" {
                ProtoBuf.dumps(t3).toUpperCase() shouldBe "120774657374696E67"
            }
            "write inner object" {
                ProtoBuf.dumps(t4).toLowerCase() shouldBe "1a0308ab02"
            }
            "write object with unordered tags" {
                ProtoBuf.dumps(t5).toUpperCase() shouldBe "D0022A120774657374696E67"
            }
        }

        "Protobuf reader" should {
            "read simple object" {
                ProtoBuf.loads<Test1>("08ab02") shouldBe t1
            }
            "read object with string" {
                ProtoBuf.loads<Test3>("120774657374696E67") shouldBe t3
            }
            "read object with list" {
                ProtoBuf.loads<Test2>("08960108E40108B90A") shouldBe t2
            }
            "read inner object" {
                ProtoBuf.loads<Test4>("1a0308ab02") shouldBe t4
            }
            "read object with unordered tags" {
                ProtoBuf.loads<Test5>("120774657374696E67D0022A") shouldBe t5
            }
        }
    }
}