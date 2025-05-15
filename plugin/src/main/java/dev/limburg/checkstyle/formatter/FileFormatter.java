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
package dev.limburg.checkstyle.formatter;

import static dev.limburg.checkstyle.CheckstyleFormatterMojo.LINE_ENDING_PROPERTY_NAME;
import static java.util.Optional.ofNullable;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import dev.limburg.checkstyle.LineSeparator;

public class FileFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(FileFormatter.class);
    private static final Map<String, LineFormatter> FORMATTERS = new HashMap<>();

    static {
        FORMATTERS.put("final.parameter", new FinalParameterFormatter());
        FORMATTERS.put("import.unused", new UnusedImportFormatter());
    }

    public void formatEntry(Map.Entry<String, List<AuditEvent>> entry, Configuration checkstyleConfig) {
        try {
            String filename = entry.getValue().iterator().next().getFileName();
            format(filename, entry.getValue(), checkstyleConfig);
        } catch (IOException e) {
            throw new CheckstyleIoException(e);
        }
    }

    private void format(String file, List<AuditEvent> auditEvents, Configuration checkstyleConfig) throws IOException {
        List<String> lines = readFile(file);
        List<AuditEvent> sortedEvents = new ArrayList<>(auditEvents);
        sortedEvents.sort(new AuditEventComparator());

        for (AuditEvent auditEvent : sortedEvents) {
            LineFormatter formatter = ofNullable(FORMATTERS.get(auditEvent.getViolation().getKey()))
                .orElse((v, l) -> l);
            lines = formatter.format(auditEvent.getViolation(), lines);
        }
        writeFile(file, lines, extractLineSeparator(checkstyleConfig));
    }

    private List<String> readFile(String file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().toList();
        }
    }

    private void writeFile(String filename, List<String> lines, String lineSeparator) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            lines.forEach(s -> {
                writer.print(s);
                writer.print(lineSeparator);
            });
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
}
