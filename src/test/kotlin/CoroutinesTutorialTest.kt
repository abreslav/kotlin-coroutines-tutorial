
import kotlinx.coroutines.experimental.*
import org.junit.Test
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

}