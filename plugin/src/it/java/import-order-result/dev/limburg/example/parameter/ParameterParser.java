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
package dev.limburg.example.parameter;

public class ParameterParser {

    private String[] parameters;

    public ParameterParser(String[] parameters) {
        this.parameters = parameters;
    }

    public String getHost() {
        for (int i = 0; i < parameters.length; i++) {
            if (i < parameters.length - 1 && (parameters[i].equals("host") || parameters[i].equals("--host") || parameters[i].equals("-host") || parameters[i].equals("-h"))) {
                return parameters[i + 1];
            } else if (parameters[i].startsWith("host=")) {
                return parameters[i].substring("host=".length());
            } else if (parameters[i].startsWith("--host=")) {
                return parameters[i].substring("--host=".length());
            } else if (parameters[i].startsWith("-host=")) {
                return parameters[i].substring("-host=".length());
            } else if (parameters[i].startsWith("-h=")) {
                return parameters[i].substring("-h=".length());
            } else if (parameters[i].startsWith("host")) {
                return parameters[i].substring("host".length());
            } else if (parameters[i].startsWith("--host")) {
                return parameters[i].substring("--host".length());
            } else if (parameters[i].startsWith("-host")) {
                return parameters[i].substring("-host".length());
            } else if (parameters[i].startsWith("-h")) {
                return parameters[i].substring("-h".length());
            }
        }
        return "localhost";
    }

    public int getPort() {
        for (int i = 0; i < parameters.length; i++) {
            if (i < parameters.length - 1 && (parameters[i].equals("port") || parameters[i].equals("--port") || parameters[i].equals("-port") || parameters[i].equals("-p"))) {
                return Integer.parseInt(parameters[i + 1]);
            } else if (parameters[i].startsWith("port=")) {
                return Integer.parseInt(parameters[i].substring("port=".length()));
            } else if (parameters[i].startsWith("--port=")) {
                return Integer.parseInt(parameters[i].substring("--port=".length()));
            } else if (parameters[i].startsWith("-port=")) {
                return Integer.parseInt(parameters[i].substring("-port=".length()));
            } else if (parameters[i].startsWith("-p=")) {
                return Integer.parseInt(parameters[i].substring("-p=".length()));
            } else if (parameters[i].startsWith("port")) {
                return Integer.parseInt(parameters[i].substring("port".length()));
            } else if (parameters[i].startsWith("--port")) {
                return Integer.parseInt(parameters[i].substring("--port".length()));
            } else if (parameters[i].startsWith("-port")) {
                return Integer.parseInt(parameters[i].substring("-port".length()));
            } else if (parameters[i].startsWith("-p")) {
                return Integer.parseInt(parameters[i].substring("-p".length()));
            }
        }
        return 8080;
    }
}
