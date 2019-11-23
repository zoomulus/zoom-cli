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
import java.util.*;

public class CLI {
    private static Logger LOG = LoggerFactory.getLogger(CLI.class);

    private String prompt;
    private Map<String, Command> commands = Maps.newHashMap();

    public CLI() {
    }

    public CLI withPrompt(@NotNull final String prompt) {
        this.prompt = prompt;
        return this;
    }

    @SuppressWarnings("unchecked")
    public CLI withCommandsRecursive(@NotNull final String packageName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (ClassPath.ClassInfo classInfo : ClassPath.from(classLoader).getTopLevelClassesRecursive(packageName)) {
            String className = classInfo.getName();
            try {
                Class c = classInfo.load();
                if (Command.class.isAssignableFrom(c)) {
                    LOG.info("Found command {}", c.getCanonicalName());
                    Command command = (Command) c.getDeclaredConstructor().newInstance();
                    for (String commandName : command.getNames()) {
                        commands.put(commandName, command);
                    }
                }
            }
            catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e2) {
                LOG.debug("Unable to load class '{}' - no suitable constructor found", className, e2);
            }
        }

        return this;
    }

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
                    if (!command.run(args)) {
                        break;
                    }
                }
            }
        }
    }
}
