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

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.util.List;

/**
 * Creates a structure for displaying tabular data to a {@link PrintStream}.
 */
public class OutputTable {
    private final PrintStream out;
    private boolean withLines = true;

    private final List<String> keys = Lists.newArrayList();
    private final List<String> values = Lists.newArrayList();
    private int longestKeyLength = 0;
    private int longestValueLength = 0;

    /**
     * Construct an OutputTable that will print to the provided {@link PrintStream};
     *
     * @param out the PrintStream to use for displaying output
     */
    public OutputTable(@NotNull final PrintStream out) {
        this.out = out;
    }

    /**
     * Specify whether the OutputTable should display table lines.  The default is true.
     *
     * @param withLines true if the OutputTable should display table lines; false if not.
     * @return the calling instance.
     */
    public OutputTable withLines(boolean withLines) {
        this.withLines = withLines;
        return this;
    }

    /**
     * Add a new row to the output table.
     *
     * @param key The key to be added.
     * @param value The value to be added.
     */
    public void addRow(@NotNull final String key, @NotNull final String value) {
        keys.add(key);
        values.add(value);
        longestKeyLength = Math.max(longestKeyLength, key.length());
        longestValueLength = Math.max(longestValueLength, value.length());
    }

    /**
     * Print the output table.
     */
    public void print() {
        int totalWidth = longestKeyLength + longestValueLength + 1;
        if (withLines) {
            totalWidth += 2;
        }

        if (withLines) {
            printHorizontalLine(totalWidth);
        }
        for (int i=0; i<keys.size(); i++) {
            if (withLines) {
                out.print("| ");
            }
            String key = keys.get(i);
            out.print(key);
            for (int j=key.length(); j<longestKeyLength; j++) {
                out.print(' ');
            }
            out.print(withLines ? " | " : " ");
            String value = values.get(i);
            out.print(value);
            for (int j=value.length(); j<longestValueLength; j++) {
                out.print(' ');
            }
            if (withLines) {
                out.print(" |");
            }
            out.println();
        }
        if (withLines) {
            printHorizontalLine(totalWidth);
        }
    }

    private void printHorizontalLine(int width) {
        StringBuilder sb = new StringBuilder();
        sb.append('+');
        for (int i=0; i<width; i++) {
            sb.append(' ');
        }
        sb.append('+');
        out.println(sb.toString());
    }
}
