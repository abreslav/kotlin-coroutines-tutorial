
import kotlinx.coroutines.examples.forEachLine
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert
import org.junit.Test
import java.io.StringReader

class ForEachLineTest {
    fun doTest(text: String) = runBlocking {
        val tempFile = createTempFile("tmp")
        try {
            tempFile.writeText(text)
            val lines = arrayListOf<String>()
            tempFile.toPath().forEachLine {
                lines.add(it)
            }

            val expected = StringReader(text).readLines()
            
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
}