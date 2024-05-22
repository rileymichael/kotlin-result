package com.github.michaelbull.result.detekt.rules

import com.github.michaelbull.result.BindingScope
import io.github.detekt.test.utils.createEnvironment
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.lintWithContext
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals


class DiscardedBindableScopeStatementTest {

    private val env = createEnvironment(
        additionalRootPaths = listOf(
            File(BindingScope::class.java.protectionDomain.codeSource.location.path),
        ),
    ).env

    @Test
    fun `no findings reported when all statements bound`() {
        val code = """
            import com.github.michaelbull.result.Ok
            import com.github.michaelbull.result.Result
            import com.github.michaelbull.result.binding

            object BindingError
            fun test() {
                fun response(): Result<Int, BindingError> = Ok(1)
                val result = binding {
                    response().bind()
                }
            }
        """.trimIndent()

        val findings = DiscardedBindableScopeStatement(Config.empty).lintWithContext(env, code)
        assertEquals(0, findings.size)
    }

    @Test
    fun `unbound statement reports findings`() {
        val code = """
            import com.github.michaelbull.result.Ok
            import com.github.michaelbull.result.Result
            import com.github.michaelbull.result.binding

            object BindingError
            fun test() {
                fun response(): Result<Int, BindingError> = Ok(1)
                val result = binding {
                    response()
                    response().bind()
                }
            }
        """.trimIndent()

        val findings = DiscardedBindableScopeStatement(Config.empty).lintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `chained statement only reports one finding`() {
        val code = """
            import com.github.michaelbull.result.Ok
            import com.github.michaelbull.result.Result
            import com.github.michaelbull.result.andThen
            import com.github.michaelbull.result.binding

            object BindingError
            fun test() {
                fun response(): Result<Int, BindingError> = Ok(1)
                val result = binding {
                    response().andThen { response() }.andThen { response() }
                    response().bind()
                }
            }
        """.trimIndent()

        val findings = DiscardedBindableScopeStatement(Config.empty).lintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `reports finding inside conditional`() {
        val code = """
            import com.github.michaelbull.result.Ok
            import com.github.michaelbull.result.Result
            import com.github.michaelbull.result.binding

            object BindingError
            fun test() {
                fun response(): Result<Int, BindingError> = Ok(1)
                val result = binding {
                    if (1 == 1) {
                        response()
                    }
                    response().bind()
                }
            }
        """.trimIndent()

        val findings = DiscardedBindableScopeStatement(Config.empty).lintWithContext(env, code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `reports finding inside receiver`() {
        val code = """
            import com.github.michaelbull.result.Ok
            import com.github.michaelbull.result.Result
            import com.github.michaelbull.result.binding

            object BindingError
            fun test() {
                fun response(): Result<Int, BindingError> = Ok(1)
                val result = binding {
                    with("some-receiver") {
                        response()
                    }
                    response().bind()
                }
            }
        """.trimIndent()

        val findings = DiscardedBindableScopeStatement(Config.empty).lintWithContext(env, code)
        assertEquals(1, findings.size)
    }
}
