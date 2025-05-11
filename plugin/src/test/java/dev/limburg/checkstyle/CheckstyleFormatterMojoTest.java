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
}

