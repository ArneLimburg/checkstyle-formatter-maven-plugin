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
package dev.limburg.example;

public class App {

    public static void main(String[] args) {
        if (Integer.parseInt(args[0]) + Integer.parseInt(args[1]) < Integer.parseInt(args[2])) {
            System.out.println("The result of " + args[0] + " + " + args[1] + " is less than " + args[2]);
        } else if (Integer.parseInt(args[0]) + Integer.parseInt(args[1]) > Integer.parseInt(args[2])) {
            System.out.println("The result of " + args[0] + " + " + args[1] + " is greater than " + args[2]);
        } else {
            System.out.println("The result of " + args[0] + " + " + args[1] + " is equal to " + args[2]);
        }
        App app = new App() { };
    }
}
