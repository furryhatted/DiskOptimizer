import kotlinx.coroutines.*
import java.io.File
import java.security.MessageDigest

typealias Result = Map<String, List<File>>

enum class Algorithm {
    MD5,
    SHA1,

    SHA256;

    private val _defaultBlockSize: Int = 2048
    fun getHash(file: File, blockSize: Int = _defaultBlockSize): String {
        val messageDigest = MessageDigest.getInstance(this.name)
        messageDigest.reset()
        file.forEachBlock(blockSize) { bytes, size ->
            messageDigest.update(bytes, 0, size)
        }
        return messageDigest
            .digest()
            .joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
            .uppercase()

    }
}

suspend fun getHashes(target: File): Map<File, String> = coroutineScope {
    val files = target.walkTopDown().filter { it.isFile }.toList()
    val hashes = files
        .map { file -> async { Algorithm.MD5.getHash(file) } }
        .toList()
    files.zip(hashes.awaitAll()).toMap()
}

suspend fun mapHashes(target: File): Result =
    getHashes(target).asSequence().groupBy({ it.value }, { it.key })

fun hashCalculationAsync(
    target: File,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
): Deferred<Result> {
    return scope.async { mapHashes(target) }
}