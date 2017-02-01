package kotlinx.coroutines.examples

import kotlinx.coroutines.experimental.nio.aRead
import kotlinx.coroutines.experimental.nio.aWrite
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.charset.Charset
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import java.util.*
import java.util.concurrent.ExecutorService


internal const val DEFAULT_BLOCK_SIZE: Int = 4096
internal const val MINIMUM_BLOCK_SIZE: Int = 512

public fun Path.openChannel(vararg options: StandardOpenOption): AsynchronousFileChannel {
    return AsynchronousFileChannel.open(this, *options)
}

public fun Path.openChannel(
        options: Set<StandardOpenOption>,
        executor: ExecutorService? = null,
        vararg attrs: FileAttribute<*>
) = AsynchronousFileChannel.open(this, options, executor, *attrs)

// NOT asynchronous enough: arrays are copied synchronously
public suspend fun Path.aReadBytes(): ByteArray = openChannel(StandardOpenOption.READ).use { input ->
    var remaining = input.size().let {
        if (it > Int.MAX_VALUE) throw OutOfMemoryError("File $this is too big ($it bytes) to fit in memory.") else it
    }.toInt()
    val result = ByteArray(remaining)
    val buf = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)

    var offset = 0L
    while (remaining > 0) {
        val read = input.aRead(buf, offset)
        if (read < 0) break
        System.arraycopy(buf.array(), 0, result, offset.toInt(), read)
        remaining -= read
        offset += read
    }

    if (remaining == 0) result else result.copyOf(offset.toInt())
}

public suspend fun Path.aWriteBytes(array: ByteArray): Unit = openChannel(StandardOpenOption.WRITE, StandardOpenOption.CREATE).use {
    it.aWrite(ByteBuffer.wrap(array), 0)
}

public suspend fun Path.aAppendBytes(array: ByteArray): Unit = openChannel(StandardOpenOption.APPEND).use {
    it.aWrite(ByteBuffer.wrap(array), 0)
}

public suspend fun Path.aReadText(charset: Charset = Charsets.UTF_8): String = aReadBytes().toString(charset)

public suspend fun Path.aWriteText(text: String, charset: Charset = Charsets.UTF_8): Unit = aWriteBytes(text.toByteArray(charset))

public suspend fun Path.aAppendText(text: String, charset: Charset = Charsets.UTF_8): Unit = aAppendBytes(text.toByteArray(charset))

// TODO: ByteArray or ByteBuffer?
public suspend fun Path.aForEachBlock(action: (buffer: ByteArray, bytesRead: Int) -> Unit): Unit = aForEachBlock(DEFAULT_BLOCK_SIZE, action)

public suspend fun Path.aForEachBlock(blockSize: Int, action: (buffer: ByteArray, bytesRead: Int) -> Unit): Unit {
    val buf = ByteBuffer.allocate(blockSize.coerceAtLeast(MINIMUM_BLOCK_SIZE))
    openChannel(StandardOpenOption.READ).use {
        channel ->
        var position = 0L
        while (true) {
            val read = channel.aRead(buf, position)
            if (read <= 0) {
                break
            } else {
                buf.flip()
                action(buf.array(), read)
            }
            position += read
        }
    }
}

public fun Path.forEachLine(charset: Charset = Charsets.UTF_8, action: (line: String) -> Unit): Unit {
    // Note: close is called at forEachLine
    // TODO: a long implementation
}

public fun File.readLines(charset: Charset = Charsets.UTF_8): List<String> {
    val result = ArrayList<String>()
    forEachLine(charset) { result.add(it); }
    return result
}

public inline fun <T> File.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T =
        bufferedReader(charset).use { block(it.lineSequence()) }
