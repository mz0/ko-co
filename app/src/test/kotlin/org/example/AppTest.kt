package org.example

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AppTest {

    @Test
    fun testSum() {
        val expected = 42
        assertThat(expected).isEqualTo(40 + 2)
    }
}
