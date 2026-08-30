package com.rerokutosei.chimera.utils.common

import org.junit.Assert.assertEquals
import org.junit.Test

class OwnedResourceTest {

    @Test
    fun replacingResourceReleasesPreviousValue() {
        val released = mutableListOf<String>()
        val owner = OwnedResource<String>(released::add)

        owner.replace("first")
        owner.replace("second")

        assertEquals(listOf("first"), released)
    }

    @Test
    fun clearingResourceReleasesItOnlyOnce() {
        val released = mutableListOf<String>()
        val owner = OwnedResource<String>(released::add)

        owner.replace("result")
        owner.clear()
        owner.clear()

        assertEquals(listOf("result"), released)
    }

    @Test
    fun replacingWithSameInstanceKeepsResourceAlive() {
        val released = mutableListOf<Any>()
        val owner = OwnedResource<Any>(released::add)
        val result = Any()

        owner.replace(result)
        owner.replace(result)

        assertEquals(emptyList<Any>(), released)
    }
}
