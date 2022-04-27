package coroutines

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.RoundingMode
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class BasicConsumer(
    var filesHashed: Int = 0
)

@ExperimentalTime
suspend fun launchCounter(consumer: BasicConsumer) = coroutineScope {
    val log: Logger = LoggerFactory.getLogger("MainLogger")
    val startTime = Instant.now().toEpochMilli()
    var previousTime = startTime
    var previousRequests = 0
    while (isActive) {
        while (consumer.filesHashed - previousRequests < 10)
            delay(10)
        val totalRequests = consumer.filesHashed
        val cycleRequests = totalRequests - previousRequests
        previousRequests = totalRequests

        val currentTime = Instant.now().toEpochMilli()
        val elapsedTime = (currentTime - startTime).toDouble()
        val cycleTime = (currentTime - previousTime).toDouble()
        previousTime = currentTime


        val rps =
            if (elapsedTime > 0) (totalRequests / (elapsedTime / 1000)).toBigDecimal().setScale(2, RoundingMode.UP)
            else 0
        val avg = if (totalRequests == 0) 0 else Duration.milliseconds(
            (elapsedTime / totalRequests).toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
        )
        val currAvg = if (cycleRequests == 0) 0 else Duration.milliseconds(
            (cycleTime / cycleRequests).toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
        )
        log.info("\rElapsed: ${Duration.milliseconds(elapsedTime)} :: Total(req.): $totalRequests :: RPS: $rps :: Avg.: $avg :: Curr.:$currAvg")
    }

}