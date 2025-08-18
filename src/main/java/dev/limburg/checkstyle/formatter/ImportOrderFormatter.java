/*
 * Copyright 2025 Arne Limburg.
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

import static dev.limburg.checkstyle.formatter.ImportTokenizer.everyImportOnSeparateLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.puppycrawl.tools.checkstyle.api.Violation;

import dev.limburg.checkstyle.formatter.ImportTokenizer.Token;

public class ImportOrderFormatter implements LineFormatter {

    // only the first violation applies (which is last in the list)
    @Override
    public boolean canApply(Violation violation, List<Violation> violations) {
        Violation first = null;
        for (Violation v: violations) {
            if (v.getKey().equals(violation.getKey())) {
                first = v;
            }
        }
        return violation == first;
    }

    @Override
    public List<String> format(Violation violation, List<String> lines) {
        List<Token> tokens = new ImportTokenizer(lines).tokenize();
        if (!everyImportOnSeparateLine(tokens)) {
            for (int i = 1; i < tokens.size(); i++) {
                if (tokens.get(i - 1).endLine() == tokens.get(i).startLine()) {
                    Token firstLine = tokens.get(i - 1);
                    Token secondLine = tokens.get(i);
                    return split(lines, firstLine, secondLine);
                }
            }
            return lines;
        } else {
            int lineNo = violation.getLineNo() - 1;
            Optional<Token> wrongImportToken = tokens.stream().filter(t -> t.startLine() <= lineNo && t.endLine() >= lineNo).findFirst();
            if (wrongImportToken.isEmpty()) {
                return lines;
            }
            Token wrongImportStatement = wrongImportToken.get();
            if (wrongImportStatement.isBlank()) {
                return lines;
            }
            int indexOfWrongToken = tokens.indexOf(wrongImportStatement);
            if (indexOfWrongToken == 0) {
                return lines;
            }
            List<String> modifiedLines = removeWrongImportStatement(lines, wrongImportStatement);
            Token previousImportStatement = tokens.get(indexOfWrongToken - 1);
            int insertionIndex = previousImportStatement.startLine();
            for (int i = wrongImportStatement.startLine(); i <= wrongImportStatement.endLine(); i++) {
                String line = lines.get(i);
                modifiedLines.add(insertionIndex, line);
                insertionIndex++;
            }
            return modifiedLines;
        }
    }

    private List<String> split(List<String> lines, Token firstLine, Token secondLine) {
        List<String> modifiedLines = new ArrayList<>(lines);
        int lineIndex = secondLine.startLine();
        String lineToSplit = lines.get(lineIndex);
        modifiedLines.set(lineIndex, lineToSplit.substring(0, firstLine.endColumn() + 1)).trim();
        modifiedLines.add(lineIndex + 1, lineToSplit.substring(secondLine.startColumn()).trim());
        return modifiedLines;
    }

    private List<String> removeWrongImportStatement(List<String> lines, Token wrongImportStatement) {
        List<String> modifiedLines = new ArrayList<>(lines);
        for (int i = wrongImportStatement.startLine(); i <= wrongImportStatement.endLine(); i++) {
            modifiedLines.remove(wrongImportStatement.startLine());
        }
        int newLineNo = wrongImportStatement.endLine();
        while (lines.get(newLineNo).isBlank()) {
            modifiedLines.remove(wrongImportStatement.startLine());
            newLineNo--;
        }
        return modifiedLines;
    }
}
