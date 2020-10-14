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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * The Zoomulus command-line interface driver.
 *
 * This class simplifies the creation of a CLI.  Clients provide a prompt and a list of {@link Command}s to the CLI.
 * After setting up the CLI, call the {@link CLI#run()} method to execute the CLI.
 */
public class CLI {
    private static Logger LOG = LoggerFactory.getLogger(CLI.class);

    private String prompt;
    private Set<String> helpStrings = Sets.newHashSet("?", "help");
    private Map<String, Command> commands = Maps.newHashMap();
    private CommandContext commandContext;

    private final Injector injector;

    /**
     * Construct an empty CLI instance.
     */
    @Inject
    public CLI(@NotNull final Injector injector) {
        this.injector = injector;
    }

    private void addCommand(@NotNull final Command command) {
        for (String commandName : command.getNames()) {
            commands.put(commandName.toLowerCase(), command);
        }
    }

    /**
     * Sets the prompt that will be shown as the CLI prompt.
     *
     * @param prompt the prompt
     * @return the CLI instance
     */
    public CLI withPrompt(@NotNull final String prompt) {
        this.prompt = prompt;
        return this;
    }

    /**
     * Adds the supplied {@link Command}s to the CLI.
     *
     * @param commands {@link List} of {@link Command}s to add.
     * @return the CLI instance
     */
    public CLI withCommands(@NotNull final List<Command> commands) {
        for (Command command : commands) {
            addCommand(command);
        }
        return this;
    }

    /**
     * Adds the supplied {@link Command} to the CLI.
     *
     * @param command {@link Command} to add.
     * @return the CLI instance
     */
    public CLI withCommand(@NotNull final Command command) {
        addCommand(command);
        return this;
    }

    public CLI withConmmandContext(@NotNull final CommandContext commandContext) {
        this.commandContext = commandContext;
        return this;
    }

    /**
     * Recursively searches for classes implementing {@link Command} under the supplied package name and adds them as
     * supported CLI commands.
     *
     * Note:  This is a very convenient but perhaps not-entirely-stable way to add commands to the CLI.  It relies on an
     * unstable API, {@link ClassPath.ClassInfo}.  Use at your own risk.
     *
     * @param packageName the name of the Java package to search for @{link Command} commands.
     * @return the CLI instance
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public CLI findCommandsRecursive(@NotNull final String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName)) {
            Class c = classInfo.load();
            if (Command.class.isAssignableFrom(c)) {
                LOG.info("Found command {}", c.getCanonicalName());
                Command command = (Command) injector.getInstance(c);
                addCommand(command);
            }
        }

        return this;
    }

    /**
     * Run the CLI.
     */
    public void run() {
        @NotNull final PrintStream out = System.out;
        @NotNull final PrintStream err = System.err;
        while (true) {
            out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            String nextLine = scanner.nextLine();
            if (! Strings.isNullOrEmpty(nextLine)) {
                String[] input = nextLine.split("\\s+", 2);
                String commandName = input[0].toLowerCase();
                List<String> args = Lists.newArrayList();
                if (input.length > 1) {
                    args.addAll(Arrays.asList(input[1].split("\\s+")));
                }
                if (helpStrings.contains(commandName)) {
                    printHelp(out);
                    continue;
                }

                Command command = commands.get(commandName);
                if (null == command) {
                    LOG.warn("No such command '{}' registered", commandName);
                }
                else {
                    if (args.size() > 0 && helpStrings.contains(args.get(0).toLowerCase().trim())) {
                        out.println(String.format("%s command help:\n", commandName));
                        out.println("Command forms: " + Joiner.on(", ").join(command.getNames()));
                        if (command.getLongDescription().isPresent()) {
                            out.println(command.getLongDescription().get());
                        }
                    }
                    else if (!command.run(commandContext.update(commandName, args), out, err)) {
                        break;
                    }
                }
            }
        }
        shutdown();
    }

    /**
     * Called when the CLI is shutting down.
     *
     * Override this method to perform cleanup as the application is shutting down.
     */
    public void shutdown() {

    }

    private void printHelp(@NotNull final PrintStream out) {
        OutputTable outputTable = new OutputTable(out).withLines(false);
        for (Command command : Sets.newHashSet(commands.values())) {
            outputTable.addRow(Joiner.on(", ").join(command.getNames()), command.getShortDescription());
        }
        outputTable.print();
    }
}
