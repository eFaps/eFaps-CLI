/*
 * Copyright 2003 - 2016 The eFaps Team
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
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.efaps.cli.utils.CLISettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import de.raysha.lib.jsimpleshell.Shell;
import de.raysha.lib.jsimpleshell.builder.ShellBuilder;
import de.raysha.lib.jsimpleshell.exception.CLIException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class StartUp
{

    /** The Constant LOG. */
    final static Logger LOG = LoggerFactory.getLogger(StartUp.class);

    /**
     * Instantiates a new start up.
     */
    private StartUp()
    {

    }

    /**
     * The main method.
     *
     * @param _args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(final String[] _args)
        throws IOException
    {
        LOG.info("Startup at {}", new Date());

        final Options options = new Options()
                        .addOption(Option.builder("l")
                                    .numberOfArgs(2)
                                    .desc("set login information")
                                    .longOpt("login")
                                    .build())
                        .addOption(Option.builder("u")
                                    .numberOfArgs(1)
                                    .desc("set url")
                                    .longOpt("url")
                                    .build())
                        .addOption(Option.builder("ll")
                                    .numberOfArgs(1)
                                    .desc("set Log Level, One of 'ALL','TRACE','DEBUG','INFO','WARN','ERROR', 'OFF'")
                                    .longOpt("logLevel")
                                    .build())
                        .addOption(Option.builder("h")
                                    .desc("print this help information")
                                    .longOpt("help")
                                    .build());

        final CommandLineParser parser = new DefaultParser();
        try {
            final CommandLine cmd = parser.parse(options, _args);

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
                                .addHandler(new EQLCandidatesChooser())
                                .addHandler(new EQLFilter())
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
                        case "ll":
                            LOG.info("Setting Log level to {}", opt.getValue());
                            final ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
                                .getLogger(Logger.ROOT_LOGGER_NAME);
                            root.setLevel(Level.toLevel(opt.getValue()));
                            LOG.info("ROOT Log level set to {}", root.getLevel());
                            break;
                        default:
                            break;
                    }
                }
                shell.processLine("company " + ContextHandler.FAKECOMPANY);
                shell.commandLoop();
            }
        } catch (final ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("eFaps-CLI", options);
            LOG.error("ParseException", e);
        } catch (final CLIException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("eFaps-CLI", options);
            LOG.error("ParseException", e);
        }
    }
}
