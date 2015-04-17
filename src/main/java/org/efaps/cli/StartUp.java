/*
 * Copyright 2003 - 2015 The eFaps Team
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
 *
 */

package org.efaps.cli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.efaps.cli.utils.CLISettings;

import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class StartUp
{

    /**
     * @param args
     */
    @SuppressWarnings("static-access")
    public static void main(final String[] args)
        throws IOException
    {
        try {
            final Options options = new Options()
                            .addOption(OptionBuilder.hasArgs(2)
                                            .withDescription("set login information")
                                            .withLongOpt("login")
                                            .create("l"))
                            .addOption(OptionBuilder.hasArgs()
                                            .withDescription("set url")
                                            .withLongOpt("url")
                                            .create("u"))
                            .addOption(OptionBuilder.withLongOpt("help")
                                            .withDescription("print this help- information")
                                            .create("h"));
            final CommandLineParser parser = new BasicParser();
            final CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("eFaps-CLI", options);
            } else {
                final Shell shell = ShellBuilder.shell("eFaps")
                                .behavior()
                                .setHistoryFile(new File(System.getProperty("user.home") + "/.eFapsCLI", "history"))
                                .addHandler(ContextHandler.get())
                                .addHandler(new CommandHandler())
                                .addHandler(new MessageResolver())
                                .addHandler(new EQLHandler())
                                .addHandler(EQLObserver.get())
                                .build();
                shell.setAppName("\"eFaps Command Line Interface\"");

                for (final Option opt : cmd.getOptions()) {
                    switch (opt.getOpt()) {
                        case "l":
                            shell.getEnvironment().setVariable(CLISettings.USER, opt.getValue(0));
                            shell.getEnvironment().setVariable(CLISettings.PWD, opt.getValue(1));
                            break;
                        case "u":
                            shell.getEnvironment().setVariable(CLISettings.URL, opt.getValue());
                            break;
                        default:
                            break;
                    }
                }
                shell.commandLoop();
            }
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IndexOutOfBoundsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
