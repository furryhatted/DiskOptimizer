import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.random.Random


val log = LoggerFactory.getLogger("Main")
val counter = AtomicInteger(0)
val channel = Channel<Int>(RENDEZVOUS)

suspend fun showSpinner() =
    withContext(coroutineContext) {
        val charArray = arrayOf("\\", "|", "/", "-")
        var currChar = -1
        while (isActive) {
            currChar = (currChar + 1) % charArray.size
            print("\rRunning... [${charArray[currChar]}]")
            delay(200)
        }

    }

suspend fun startListener() =
    withContext(NonCancellable) {
        while (isActive)
            println(channel.receive())

    }


fun main(): Unit = runBlocking {
    launch(Dispatchers.Default) { showSpinner() }
    val job = launch(Dispatchers.Default) { startListener() }
    repeat(1000) { channel.send(Random.nextInt(1000)) }
    println("Done")

}