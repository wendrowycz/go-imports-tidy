package eu.oakroot

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvFileSource
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

internal class GoImportTidyTest: GoImportTidy() {

    @ParameterizedTest
    @CsvFileSource(resources = ["/data.csv"], numLinesToSkip = 1)
    @Throws(IOException::class)
    fun testParseFile(input: String, expected: String, isParsed: Boolean?) {
        val executedOutput: String = FileUtils.readFileToString(File("src/test/resources/fixtures/$expected"), "utf-8")
        val expectedParsed: String = StringUtils.substringBetween(executedOutput, "import (\n", "\n)")?:""
        val inputFile: String = Files.readString(Path.of("src/test/resources/fixtures/$input"))
        val imports: String = findImports(inputFile)
        val importsBlock: ArrayList<String> = ArrayList(Arrays.asList(*imports.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
        val parsedFile: ParsedFile = parseFile(importsBlock, "github.com/namespace")
        Assertions.assertEquals(expectedParsed, parsedFile.fileContent)
        Assertions.assertEquals(isParsed, parsedFile.isParsed)

    }

}