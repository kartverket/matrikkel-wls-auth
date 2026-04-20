package no.statkart.matrikkel.auth.identity.basic

import arrow.core.Either
import arrow.core.raise.catch
import arrow.resilience.Schedule
import arrow.resilience.retryRaise
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

internal suspend fun <A> retryTokenFetch(
    retries: Int,
    initialDelayMillis: Duration,
    fetch: suspend () -> A,
): Either<Throwable, A> {
    val schedule = Schedule
        .fibonacci<Throwable>(initialDelayMillis)
        .and(Schedule.recurs(retries.toLong()))

    return schedule.retryRaise {
        catch({ fetch() }, ::raise)
    }
}
