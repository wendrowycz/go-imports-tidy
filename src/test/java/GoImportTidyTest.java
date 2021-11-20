import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GoImportTidyTest extends GoImportTidy {

    @Before
    public void SetUp() {
        System.out.println("Setup tests");
    }

    @After
    public void Teardown() {
        System.out.println("Teardown");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void testParseFile(String input, String expected, Boolean isParsed) throws IOException {
        String expectedOutput = FileUtils.readFileToString(new File("src/test/resources/fixtures/" + expected), "utf-8");
        BufferedReader inputFile = new BufferedReader(new FileReader("src/test/resources/fixtures/" + input));
        ParsedFile parsedFile = parseFile(inputFile, "github.com/namespace");
        assertEquals(expectedOutput, parsedFile.getFileContent());
        assertEquals(isParsed, parsedFile.isParsed());
    }
}