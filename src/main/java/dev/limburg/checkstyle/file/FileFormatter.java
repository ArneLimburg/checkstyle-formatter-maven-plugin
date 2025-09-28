/*
 * Copyright 2025 Arne Limburg, Steffen Pieper.
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
package dev.limburg.checkstyle.file;

import static dev.limburg.checkstyle.CheckstyleFormatterMojo.LINE_ENDING_PROPERTY_NAME;
import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.Violation;

import dev.limburg.checkstyle.LineSeparator;
import dev.limburg.checkstyle.formatter.FinalParameterFormatter;
import dev.limburg.checkstyle.formatter.ImportGroupsFormatter;
import dev.limburg.checkstyle.formatter.ImportOrderFormatter;
import dev.limburg.checkstyle.formatter.ImportSeparationFormatter;
import dev.limburg.checkstyle.formatter.IndentationFormatter;
import dev.limburg.checkstyle.formatter.LineFormatter;
import dev.limburg.checkstyle.formatter.RedundantImportFormatter;
import dev.limburg.checkstyle.formatter.TabCharacterFormatter;
import dev.limburg.checkstyle.formatter.TrailingSpacesFormatter;
import dev.limburg.checkstyle.formatter.UnusedImportFormatter;
import dev.limburg.checkstyle.formatter.WhitespaceFormatter;

public class FileFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(FileFormatter.class);
    private static final String FINAL_PARAMETER_KEY = "final.parameter";
    private static final String UNUSED_IMPORT_KEY = "import.unused";
    private static final String TRAILING_SPACES_KEY = "Line has trailing spaces.";
    private static final String IMPORT_ORDERING_KEY = "import.ordering";
    private static final String IMPORT_SEPARATION_KEY = "import.separation";
    private static final String IMPORT_GROUPS_KEY = "import.groups.separated.internally";
    private static final String INDENTATION_ERROR_KEY = "indentation.error";
    private static final String INDENTATION_CHILD_ERROR_KEY = "indentation.child.error";
    private static final String INDENTATION_ERROR_MULTI_KEY = "indentation.error.multi";
    private static final String INDENTATION_CHILD_ERROR_MULTI_KEY = "indentation.child.error.multi";
    private static final String WHITESPACE_NOT_PRECEDED_KEY = "ws.notPreceded";
    private static final String WHITESPACE_NOT_FOLLOWED_KEY = "ws.notFollowed";
    private static final String WHITESPACE_PRECEDED_KEY = "ws.preceded";
    private static final String WHITESPACE_FOLLOWED_KEY = "ws.followed";
    private static final String CONTAINS_TAB_KEY = "containsTab";
    private static final String FILE_CONTAINS_TAB_KEY = "file.containsTab";
    private static final String REDUNDANT_IMPORT_KEY = "import.duplicate";
    private static final String REDUNDANT_LANG_KEY = "import.lang";
    private static final Map<String, LineFormatter> FORMATTERS = new HashMap<>();

    static {
        FORMATTERS.put(FINAL_PARAMETER_KEY, new FinalParameterFormatter());
        FORMATTERS.put(UNUSED_IMPORT_KEY, new UnusedImportFormatter());
        FORMATTERS.put(TRAILING_SPACES_KEY, new TrailingSpacesFormatter());
        FORMATTERS.put(IMPORT_ORDERING_KEY, new ImportOrderFormatter());
        FORMATTERS.put(IMPORT_SEPARATION_KEY, new ImportSeparationFormatter());
        FORMATTERS.put(IMPORT_GROUPS_KEY, new ImportGroupsFormatter());
        FORMATTERS.put(INDENTATION_ERROR_KEY, new IndentationFormatter());
        FORMATTERS.put(INDENTATION_CHILD_ERROR_KEY, new IndentationFormatter());
        FORMATTERS.put(INDENTATION_ERROR_MULTI_KEY, new IndentationFormatter());
        FORMATTERS.put(INDENTATION_CHILD_ERROR_MULTI_KEY, new IndentationFormatter());
        FORMATTERS.put(WHITESPACE_NOT_PRECEDED_KEY, new WhitespaceFormatter());
        FORMATTERS.put(WHITESPACE_NOT_FOLLOWED_KEY, new WhitespaceFormatter());
        FORMATTERS.put(WHITESPACE_PRECEDED_KEY, new WhitespaceFormatter());
        FORMATTERS.put(WHITESPACE_FOLLOWED_KEY, new WhitespaceFormatter());
        FORMATTERS.put(CONTAINS_TAB_KEY, new TabCharacterFormatter());
        FORMATTERS.put(FILE_CONTAINS_TAB_KEY, new TabCharacterFormatter());
        FORMATTERS.put(REDUNDANT_IMPORT_KEY, new RedundantImportFormatter());
        FORMATTERS.put(REDUNDANT_LANG_KEY, new RedundantImportFormatter());
    }

    private FileChangedListener fileChangedListener;
    private Map<String, Set<String>> hashesPerFile = new HashMap<>();

    public void registerFileChangedListener(FileChangedListener listener) {
        fileChangedListener = listener;
    }

    public void formatEntry(Map.Entry<String, List<AuditEvent>> entry, Configuration checkstyleConfig) {
        try {
            if (!entry.getValue().isEmpty()) {
                String filename = entry.getValue().iterator().next().getFileName();
                format(filename, entry.getValue(), checkstyleConfig);
            }
        } catch (IOException e) {
            throw new CheckstyleIoException(e);
        }
    }

    private void format(String file, List<AuditEvent> auditEvents, Configuration checkstyleConfig) throws IOException {
        List<String> lines = readFile(file);
        List<AuditEvent> sortedEvents = new ArrayList<>(auditEvents);
        sortedEvents.sort(new AuditEventComparator());
        List<Violation> violations = sortedEvents.stream().map(AuditEvent::getViolation).toList();

        for (AuditEvent auditEvent : sortedEvents) {
            LineFormatter formatter = ofNullable(FORMATTERS.get(auditEvent.getViolation().getKey()))
                .orElse((v, l) -> l);
            if (formatter.canApply(auditEvent.getViolation(), violations)) {
                lines = formatter.format(auditEvent.getViolation(), lines);
            }
        }
        writeFile(file, lines, extractLineSeparator(checkstyleConfig));
    }

    private List<String> readFile(String file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toList();
        }
    }

    private void writeFile(String filename, List<String> lines, String lineSeparator) throws IOException {
        MessageDigest digest = newDigest();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            lines.forEach(s -> {
                writer.print(s);
                writer.print(lineSeparator);
                digest.update(s.getBytes());
            });
        }
        String hash = new BigInteger(1, digest.digest()).toString(16);
        Set<String> hashes = hashesPerFile.computeIfAbsent(filename, f -> new HashSet<>());
        if (!hashes.contains(hash)) {
            hashes.add(hash);
            ofNullable(fileChangedListener).ifPresent(listener -> listener.onChanged(filename));
        }
    }

    private String extractLineSeparator(Configuration checkstyleConfig) {
        try {
            return checkstyleConfig.getProperty(LINE_ENDING_PROPERTY_NAME);
        } catch (CheckstyleException e) {
            LOG.info("Could not read line ending property, use System.lineSeparator() instead");
        }
        return LineSeparator.SYSTEM.getSeparator();
    }

    private MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
