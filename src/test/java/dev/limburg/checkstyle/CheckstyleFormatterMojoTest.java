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
package dev.limburg.checkstyle;

import org.junit.Rule;
import org.junit.Test;

public class CheckstyleFormatterMojoTest {

    @Rule
    public CheckstyleFormatterRule rule = new CheckstyleFormatterRule();

    @Test
    public void formatFinalParameter() throws Exception {
        rule.given("src/it/java/final-parameter");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/final-parameter-result");
    }

    @Test
    public void formatUnusedImport() throws Exception {
        rule.given("src/it/java/unused-import");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/unused-import-result");
    }

    @Test
    public void trailingSpaces() throws Exception {
        rule.given("src/it/java/trailing-spaces");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/trailing-spaces-result");
    }

    @Test
    public void importOrder() throws Exception {
        rule.given("src/it/java/import-order");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/import-order-result");
    }

    @Test
    public void indentation() throws Exception {
        rule.given("src/it/java/indentation");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/indentation-result");
    }

    @Test
    public void whitespaces() throws Exception {
        rule.given("src/it/java/whitespaces");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/whitespaces-result");
    }

    @Test
    public void tabCharacter() throws Exception {
        rule.given("src/it/java/tab-character");
        rule.whenExecuteFormatting();
        rule.thenResultIsSameAs("src/it/java/tab-character-result");
    }
}

