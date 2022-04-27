package coroutines


import hashCalculationAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File

internal class HashKtTest {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val rootFolder = "path\\to\\folder\\"

    @Test
    fun generalTest() = runBlocking {
        val target = File(rootFolder)
        val task = hashCalculationAsync(target)
        val result = task.await()
//        result.forEach { (hash, files) -> logger.debug("$hash -> $files") }
        result.filter { it.value.size > 1 }
            .onEach { logger.info("Identical hashes for: ${it.value}") }
            .map { it.value }
            .map { it.asSequence().drop(1) }
            .asSequence()
            .flatten()
            .forEach {
                launch(Dispatchers.IO) {
                    it.delete()
                    logger.warn("Removed ${it.absoluteFile}")
                }
            }
        joinAll()
        target
            .walkBottomUp()
            .filter { it.isDirectory }
            .filter { it.listFiles()?.isEmpty() ?: false }
            .onEach { logger.info("Empty directory ${it.absoluteFile}") }
            .forEach {
                launch(Dispatchers.IO) {
                    it.delete()
                    logger.warn("Removed ${it.absoluteFile}")
                }
            }
    }
}