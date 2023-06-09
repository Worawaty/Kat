/*
 * Copyright 2017-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json

import kotlinx.serialization.Serializable
import kotlin.test.Test

class JsonHugeDataSerializationTest : JsonTestBase() {

    @Serializable
    private data class Node(
        val children: List<Node>
    )

    private fun createNodes(count: Int, depth: Int): List<Node> {
        val ret = mutableListOf<Node>()
        if (depth == 0) return ret
        for (i in 0 until count) {
            ret.add(Node(createNodes(1, depth - 1)))
        }
        return ret
    }

    @Test
    fun test() {
        // create some huge instance
        val rootNode = Node(createNodes(1000, 10))

        val expectedJson = Json.encodeToString(Node.serializer(), rootNode)

        /*
          The assertJsonFormAndRestored function, when checking the encoding, will call Json.encodeToString(...) for `JsonTestingMode.STREAMING`
          since the string `expectedJson` was generated by the same function, the test will always consider
          the encoding to the `STREAMING` mode is correct, even if there was actually an error there. So only TREE, JAVA_STREAMS and OKIO are actually being tested here
         */
        assertJsonFormAndRestored(Node.serializer(), rootNode, expectedJson)
    }
}
