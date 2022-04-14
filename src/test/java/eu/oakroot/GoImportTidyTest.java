package eu.oakroot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoImportTidyTest extends GoImportTidy {

    @BeforeAll
    public static void SetUp() {
        System.out.println("Setup tests");
    }

    @AfterAll
    public static void Teardown() {
        System.out.println("Teardown");
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/data.csv", numLinesToSkip = 1)
    void testParseFile(String input, String expected, Boolean isParsed) throws IOException {
        String expectedOutput = FileUtils.readFileToString(new File("src/test/resources/fixtures/" + expected), "utf-8");
        String expectedParsed = StringUtils.substringBetween(expectedOutput, "import (", ")");
        if (expectedParsed == null) {
            expectedParsed = "";
        }
        String inputFile = Files.readString(Path.of("src/test/resources/fixtures/" + input));
        String imports = findImports (inputFile);
        ArrayList<String> importsBlock = new ArrayList<>(Arrays.asList(imports.split("\n")));
        ParsedFile parsedFile = parseFile(importsBlock, "github.com/namespace");
        assertEquals(expectedParsed, parsedFile.getFileContent());
        assertEquals(isParsed, parsedFile.isParsed());
    }
}