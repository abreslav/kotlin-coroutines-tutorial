
import kotlinx.coroutines.examples.forEachLine
import kotlinx.coroutines.examples.openChannel
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.nio.aRead
import org.junit.Test
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class CoroutinesTutorialTest {
    @Test
    fun launchWithThreadSleep() {
        println("Start")

        // Start a coroutine
        launch(CommonPool) {
            delay(1000)
            println("Hello")
        }

        Thread.sleep(2000) // wait for 2 seconds
        println("Stop")
    }

    @Test
    fun launchWithRunBlocking() {
        println("Start")

        // Start a coroutine
        launch(CommonPool) {
            delay(1000)
            println("Hello")
        }

        runBlocking {
            delay(2000)
        }
        println("Stop")
    }

    @Test
    fun thousandThreads() {
        val c = AtomicInteger()

        for (i in 1..1_000)
            thread(start = true) {
                c.addAndGet(i)
            }

        println(c.get())
    }

    @Test
    fun thousandLaunches() {
        val c = AtomicInteger()

        for (i in 1..1_000)
            launch(CommonPool) {
                c.addAndGet(i)
            }

        println(c.get())
    }

    @Test
    fun millionThreads() {
        val c = AtomicInteger()

        for (i in 1..1_000_000)
            thread(start = true) {
                c.addAndGet(i)
            }

        println(c.get())
    }

    @Test
    fun millionLaunches() {
        val c = AtomicInteger()

        for (i in 1..1_000_000)
            launch(CommonPool) {
                c.addAndGet(i)
            }

        println(c.get())
    }

    @Test
    fun millionLaunchesLatch() {
        val c = AtomicInteger()
        val latch = CountDownLatch(1_000_000)

        for (i in 1..1_000_000)
            launch(CommonPool) {
                c.addAndGet(i)
                latch.countDown()
            }

        latch.await()
        println(c.get())
    }

    @Test
    fun millionDefers() {
        val deferred = (1..1_000_000).map { n ->
            defer (CommonPool) {
                n
            }
        }

        runBlocking {
            val sum = deferred.sumBy { it.await() }
            println("Sum: $sum")
        }
    }

    @Test
    fun millionDefersWithDelay() {
        val deferred = (1..1_000_000).map { n ->
            defer (CommonPool) {
                delay(1000)
                n
            }
        }

        runBlocking {
            val sum = deferred.sumBy { it.await() }
            println("Sum: $sum")
        }
    }

    suspend fun workload(n: Int): Int {
        delay(1000)
        return n
    }

    @Test
    fun millionDefersWithDelayExtractFun() {
        val deferred = (1..1_000_000).map { n ->
            defer (CommonPool) {
                workload(n)
            }
        }

        runBlocking {
            val sum = deferred.sumBy { it.await() }
            println("Sum: $sum")
        }
    }

    @Test
    fun readFile() {
        val file = Paths.get("src/test/resources/example.txt").openChannel(StandardOpenOption.READ)
        val buf = ByteBuffer.allocate(64)

        runBlocking {
            var position = 0L

            while (true) {
                val count = file.aRead(buf, position)
                if (count < 0) break
                position += count
                buf.flip()

                print(String(buf.array(), 0, count))
            }

        }

        file.close()
    }

    suspend fun AsynchronousFileChannel.aReadBytes(bufferSize: Int = 1024, handleChunk: (ByteBuffer, Int) -> Unit) {
        val buf = ByteBuffer.allocate(bufferSize)
        var position = 0L

        while (true) {
            val count = aRead(buf, position)
            if (count < 0) break
            position += count
            buf.flip()

            handleChunk(buf, count)
        }
    }

    @Test
    fun aReadBytes() {
        val file = Paths.get("src/test/resources/example.txt").openChannel(StandardOpenOption.READ)
        runBlocking {
            file.aReadBytes(64) { buf, count ->
                println(String(buf.array(), 0, count))
            }
        }
    }

    suspend fun AsynchronousFileChannel.aReadText(
            bufferSize: Int = 1024,
            builder: StringBuilder = StringBuilder()
    ): CharSequence {
        aReadBytes(bufferSize) { buf, count ->
            builder.append(String(buf.array(), 0, count))
        }
        return builder
    }

    @Test
    fun aReadText() {
        val file = Paths.get("src/test/resources/example.txt").openChannel(StandardOpenOption.READ)
        runBlocking {
            println(file.aReadText())
        }
    }

    @Test
    fun forEachLine() {
        val file = Paths.get("src/test/resources/example.txt")
        runBlocking {
            file.forEachLine {
                println("|$it|")
            }
        }
    }
}