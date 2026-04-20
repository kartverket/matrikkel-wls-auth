package no.statkart.matrikkel.auth.identity.basic

import arrow.core.Either
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.time.Duration.Companion.milliseconds

class BasicIdentityStoreRetryTest {
    @Test
    fun `retryTokenFetch retries thrown failures until success`() = runBlocking {
        var attempts = 0

        val result = retryTokenFetch(
            initialDelayMillis = 1.milliseconds,
            retries = 3
        ) {
            attempts += 1
            if (attempts < 3) {
                error("retry")
            } else {
                "access-token"
            }
        }

        assertEquals(3, attempts)
        assertEquals("access-token", result.getOrNull())
    }

    @Test
    fun `retryTokenFetch does not retry left responses`() = runBlocking {
        var attempts = 0

        val result = retryTokenFetch(
            initialDelayMillis = 1.milliseconds,
            retries = 3
        ) {
            attempts += 1
            Either.Left("fail-fast")
        }

        assertTrue(result.isRight())
        assertTrue(result.getOrNull()?.isLeft() == true)
        assertEquals(1, attempts)
    }

    @Test
    fun `retryTokenFetch rethrows after exhausting retries`() = runBlocking {
        var attempts = 0

        val result = retryTokenFetch(
            retries = 2,
            initialDelayMillis = 1.milliseconds,
        ) {
            attempts += 1
            throw IllegalStateException("boom")
        }

        assertEquals(3, attempts)
        assertTrue(result.isLeft())
        assertTrue(result.swap().getOrNull() is IllegalStateException)
    }
}
