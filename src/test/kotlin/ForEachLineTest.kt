
import kotlinx.coroutines.examples.forEachLine
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.StringReader

@RunWith(Parameterized::class)
class ForEachLineTest(val multiplier: Int) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "String size multiplier = {0}")
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf<Any>(1),
                    arrayOf<Any>(1024),
                    arrayOf<Any>(4 * 1024),
                    arrayOf<Any>(8 * 1024),
                    arrayOf<Any>(16 * 1024),
                    arrayOf<Any>(239239)
            )
        }
    }

    fun doTest(text: String) = runBlocking {
        val preparedText = if (multiplier > 1) {
            buildString {
                for (c in text) {
                    if (c !in "\r\n") {
                        append("$c".repeat(multiplier))
                    } else {
                        append(c)
                    }
                }
            }
        } else {
            text
        }


        val tempFile = createTempFile("tmp")
        try {
            tempFile.writeText(preparedText)
            val lines = arrayListOf<String>()
            tempFile.toPath().forEachLine {
                lines.add(it)
            }

            val expected = StringReader(preparedText).readLines()
            
            Assert.assertEquals(expected, lines)
        } finally {
            tempFile.deleteOnExit()
        }
    }

    @Test
    fun empty() {
        doTest("")
    }

    @Test
    fun lf() {
        doTest("\n")
    }

    @Test
    fun cr() {
        doTest("\r")
    }

    @Test
    fun crlf() {
        doTest("\r\n")
    }

    @Test
    fun lfcr() {
        doTest("\n\r")
    }

    @Test
    fun crcr() {
        doTest("\r\r")
    }

    @Test
    fun lflf() {
        doTest("\n\n")
    }

    @Test
    fun singleChar() {
        doTest("a")
    }

    @Test
    fun singleLine() {
        doTest("abc")
    }

    @Test
    fun singleLineLf() {
        doTest("abc\n")
    }

    @Test
    fun singleLineCr() {
        doTest("abc\r")
    }

    @Test
    fun singleLineCrLf() {
        doTest("abc\r\n")
    }

    @Test
    fun multiLine() {
        doTest("abc\ndef")
    }

    @Test
    fun multiLineCrSep() {
        doTest("abc\rdef")
    }

    @Test
    fun multiLineCrLfSep() {
        doTest("abc\n\rdef")
    }

    @Test
    fun multiLineLf() {
        doTest("abc\ndef\n")
    }

    @Test
    fun multiLineCr() {
        doTest("abc\ndef\r")
    }

    @Test
    fun multiLineCrLf() {
        doTest("abc\ndef\r\n")
    }

    @Test
    fun lfStart() {
        doTest("\nabc")
    }

    @Test
    fun crStart() {
        doTest("\rabc")
    }

    @Test
    fun crlfStart() {
        doTest("\r\nabc")
    }

    @Test
    fun lfcrStart() {
        doTest("\n\rabc")
    }
}