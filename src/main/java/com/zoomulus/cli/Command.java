/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zoomulus.cli;

import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

/**
 * Commands for the Zoomulus {@link CLI}.
 */
public interface Command {
    /**
     * Returns the command names.  These are the names that will be used to run the command.  For example, a command to
     * exit the program might have the name "exit" - this is what a user would type to execute the command.
     *
     * A list is returned to allow a command to provide multiple names for the command.  For example, the "exit" command
     * might also allow the names "quit" and "bye" to be used.  Any of the names provided will execute the command.
     * @return The list of command names
     */
    @NotNull List<String> getNames();

    /**
     * Returns short descriptive text that briefly describes the command.  Used when the "help" command is called.  This
     * should ideally be just one line, just a few words of text.
     *
     * @return description text.
     */
    @NotNull String getShortDescription();

    /**
     * Returns text describing the command.  Used when calling "[command-name] help".
     *
     * @return description text.
     */
    @NotNull Optional<String> getLongDescription();

    /**
     * Executes the command.
     *
     * @param commandName The command name that was used to select the command.
     * @param args The command-line arguments provided to the command.
     * @param out A {@link PrintStream} for writing standard output.
     * @param err A {@link PrintStream} for writing error output.
     * @return false if the CLI should exit; true otherwise.  Most commands will return true.
     */
    boolean run(@NotNull final String commandName,
                @NotNull final List<String> args,
                @NotNull final PrintStream out,
                @NotNull final PrintStream err);
}
