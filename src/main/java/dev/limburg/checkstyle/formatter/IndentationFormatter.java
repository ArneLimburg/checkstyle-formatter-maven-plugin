/*
 * Copyright 2025 Steffen Pieper.
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

import java.util.ArrayList;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Violation;

public class IndentationFormatter implements LineFormatter {

    @Override
    public List<String> format(Violation violation, List<String> content) {
        List<String> lines = new ArrayList<>(content);
        int lineNo = violation.getLineNo() - 1;
        String line = lines.get(lineNo);
        lines.set(lineNo, formatLine(line, violation.getViolation(), violation.getColumnCharIndex()));
        return lines;
    }

    private String formatLine(String line, String violation, int actualIndentation) {
        int expectedIndentation = extractExpectedIndentation(violation, actualIndentation);
        if (actualIndentation < expectedIndentation) {
            return line.substring(0, actualIndentation) + ' ' + line.substring(actualIndentation);
        } else {
            return line.substring(0, actualIndentation - 1) + line.substring(actualIndentation);
        }
    }

    // unfortunately we have to parse the message, because there is no other way to get the arguments.
    private int extractExpectedIndentation(String message, int actualIndentation) {
        StringBuilder violationMessage = new StringBuilder(message);

        // remove all letters
        for (int i = 0; i < violationMessage.length(); i++) {
            if (Character.isLetter(violationMessage.charAt(i))) {
                violationMessage.deleteCharAt(i);
                i--;
            }
        }
        StringBuilder firstNumber = null;
        StringBuilder secondNumber = null;
        for (int i = 0; i < violationMessage.length(); i++) {
            if (Character.isDigit(violationMessage.charAt(i))) {
                if (firstNumber == null) {
                    firstNumber = new StringBuilder(2);
                }
                if (secondNumber == null) {
                    firstNumber.append(violationMessage.charAt(i));
                } else {
                    secondNumber.append(violationMessage.charAt(i));
                }
            } else if (firstNumber != null && secondNumber == null) {
                secondNumber = new StringBuilder(2);
            }
        }
        int first = Integer.parseInt(firstNumber.toString());
        int second = Integer.parseInt(secondNumber.toString());
        return first == actualIndentation ? second : first;
    }
}
