package org.http4k.core

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.http4k.core.body.Form
import org.http4k.core.body.form
import org.http4k.core.body.toBody
import org.junit.Test

class FormTest {

    @Test
    fun `can add to request and extract it`() {
        val form: Form = listOf("a" to "b")

        val get = Request(Method.GET, "ignored").body(form.toBody())

        val actual = get.form()

        assertThat(actual, equalTo(form))
    }

    @Test
    fun `can add individual form parameters`(){
        val get = Request(Method.GET, "ignored").form("foo", "1").form("bar", "2")

        val actual = get.form()

        val form:Form = listOf("foo" to "1", "bar" to "2")
        assertThat(actual, equalTo(form))
    }
}