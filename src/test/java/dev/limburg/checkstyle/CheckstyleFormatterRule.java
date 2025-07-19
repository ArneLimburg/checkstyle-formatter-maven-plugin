/*
 * Copyright 2025 Arne Limburg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.limburg.checkstyle;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.testing.MojoRule;

import com.google.common.io.Files;

public class CheckstyleFormatterRule extends MojoRule {

    private File directory;

    public void given(String sourceDirectory) throws IOException {
        File source = new File(sourceDirectory);
        assertTrue("folder " + sourceDirectory + " exists.", source.exists());
        assertTrue(sourceDirectory + " is a folder.", source.isDirectory());
        directory = new File("target", sourceDirectory);
        directory.delete();
        copyDirectory(new File(sourceDirectory), directory);
    }

    public void whenExecuteFormatting() throws Exception {
        CheckstyleFormatterMojo formatter = (CheckstyleFormatterMojo)lookupConfiguredMojo(directory, "write");
        assertNotNull(formatter);
        formatter.sourceDirectories = List.of(".");
        formatter.execute();
        File reportDirectory = new File(directory, "target");
        for (File file: reportDirectory.listFiles()) {
            file.delete();
        }
        reportDirectory.delete();
    }

    public void thenResultIsSameAs(String expectedDirectory) throws IOException {
        File expected = new File(expectedDirectory);
        compare(expected, directory);
    }

    private void copyDirectory(File sourceDirectory, File targetDirectory) throws IOException {
        targetDirectory.mkdirs();
        for (File source: sourceDirectory.listFiles()) {
            File target = new File(targetDirectory, source.getName());
            if (source.isDirectory()) {
                copyDirectory(source, target);
            } else {
                Files.copy(source, target);
            }
        }
    }

    private void compare(File expected, File actual) throws IOException {
        assertTrue(expected + " and " + actual + " should either both be directorys or both files.",
            expected.isDirectory() == actual.isDirectory());
        if (!expected.isDirectory() && !actual.isDirectory()) {
            compareFiles(expected, actual);
        } else {
            compareDirectories(expected, actual);
        }
    }

    private void compareFiles(File expectedFile, File actualFile) throws IOException, FileNotFoundException {
        try (BufferedReader expectedContent = new BufferedReader(new FileReader(expectedFile));
            BufferedReader actualContent = new BufferedReader(new FileReader(actualFile))) {
            String expectedLine;
            String actualLine;
            do {
                expectedLine = expectedContent.readLine();
                actualLine = actualContent.readLine();
            } while (expectedLine != null && actualLine != null && expectedLine.equals(actualLine));
            if (expectedLine != null || actualLine != null) {
                assertEquals("in file " + expectedFile.getName(), expectedLine, actualLine);
            }
        }
    }

    private void compareDirectories(File expectedDirectory, File actualDirectory) throws IOException {
        assertEquals("number of files in folder " + expectedDirectory.getName(),
            expectedDirectory.list().length,
            actualDirectory.list().length);
        Set<String> expectedFilenames = new HashSet<>(asList(expectedDirectory.list()));
        for (File actualFile: actualDirectory.listFiles()) {
            if (!expectedFilenames.contains(actualFile.getName())) {
                fail("unexpected file " + actualFile.getName() + " in folder " + expectedDirectory.getName());
            }
            compare(new File(expectedDirectory, actualFile.getName()), actualFile);
        }
    }
}
