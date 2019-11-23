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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The Zoomulus command-line interface driver.
 *
 * This class simplifies the creation of a CLI.  Clients provide a prompt and a list of {@link Command}s to the CLI.
 * After setting up the CLI, call the {@link CLI#run()} method to execute the CLI.
 */
public class CLI {
    private static Logger LOG = LoggerFactory.getLogger(CLI.class);

    private String prompt;
    private Map<String, Command> commands = Maps.newHashMap();

    /**
     * Construct an empty CLI instance.
     */
    public CLI() {
    }

    /**
     * Construct a CLI instance with the provided prompt and commands.
     *
     * @param prompt String that will be shown as the CLI prompt.
     * @param commands List of {@link Command}s to be added to the CLI.
     */
    public CLI(@NotNull final String prompt, @NotNull final List<Command> commands) {
        this.prompt = prompt;
        for (Command command : commands) {
            addCommand(command);
        }
    }

    private void addCommand(@NotNull final Command command) {
        for (String commandName : command.getNames()) {
            commands.put(commandName, command);
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
            String className = classInfo.getName();
            try {
                Class c = classInfo.load();
                if (Command.class.isAssignableFrom(c)) {
                    LOG.info("Found command {}", c.getCanonicalName());
                    Command command = (Command) c.getDeclaredConstructor().newInstance();
                    addCommand(command);
                }
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e2) {
                LOG.debug("Unable to load class '{}' - no suitable constructor found", className, e2);
            }
        }

        return this;
    }

    /**
     * Run the CLI.
     */
    public void run() {
        while (true) {
            System.out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            String nextLine = scanner.nextLine();
            if (! Strings.isNullOrEmpty(nextLine)) {
                String[] input = nextLine.split("\\s+", 1);
                String commandName = input[0];
                List<String> args = Lists.newArrayList();
                if (input.length > 1) {
                    args.addAll(Arrays.asList(input[1].split("\\s+")));
                }
                Command command = commands.get(commandName);
                if (null == command) {
                    LOG.warn("No such command '{}' registered", commandName);
                } else {
                    if (!command.run(commandName, args)) {
                        break;
                    }
                }
            }
        }
    }
}
