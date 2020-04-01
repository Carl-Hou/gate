/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.gate;

import org.apache.commons.cli.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.gate.common.config.GateProps;
import org.gate.common.util.GateUtils;
import org.gate.gui.GuiPackage;
import org.gate.gui.common.OptionPane;
import org.gate.gui.tree.GateTreeSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GateMain {

    static Logger log = LogManager.getLogger();

	private static final String OPT_HELP = "h";// $NON-NLS-1$
	private static final String OPT_GATE_PROPERTY = "G";// $NON-NLS-1$
	private static final String OPT_NON_GUI = "n";// $NON-NLS-1$
	private static final String OPT_TEST_FILE = "t";// $NON-NLS-1$
	private static final String OPT_SUITE_PREFIX = "S";// $NON-NLS-1$ need to be single char
	private static final String OPT_CASE_PREFIX = "C";// $NON-NLS-1$

	public GateMain(){
	}

	public static void main(String[] args)  {
		initializeProperties();
		GateProps.getProperties().stringPropertyNames().forEach(name ->{
			if(name.startsWith("log_level")){
				String logName=name.substring(10);
				String logLevel=GateProps.getProperty(name);
				log.info("Log level of logger: {} set to {}", logName, logLevel);
				Configurator.setAllLevels(logName, Level.valueOf(logLevel));
			}
		});

		runGate(args);
	}

	static void runGate(String[] args)  {
		Options options = new Options();
		options.addOption(OPT_HELP,false,"print usage information and command line options and exit");
		options.addOption(OPT_NON_GUI,false,"run Gate in non-gui mode");
		options.addOption(OPT_TEST_FILE,true,"Gate test file to run");
		options.addOption(OPT_SUITE_PREFIX,true,"Filter test suites name prefix to execute. Separate with comma");
		options.addOption(OPT_CASE_PREFIX,true,"Filter test cases name prefix to execute. Separate with comma");

		options.addOption(Option.builder(OPT_GATE_PROPERTY).desc("Define Gate properties \n\t\te.g. -GvarName=value -GsomePort=8081")
				.hasArgs().numberOfArgs(2).valueSeparator().build());

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "gate", options );
			e.printStackTrace();
			return;
		}

		boolean gui = !cmd.hasOption(OPT_NON_GUI);

		boolean nonGuiOnly =  cmd.hasOption(OPT_SUITE_PREFIX)
				|| cmd.hasOption(OPT_CASE_PREFIX);
		if (gui && nonGuiOnly) {
		    log.warn("-S -C only effect in non-GUI mode");

		}

		if(cmd.hasOption(OPT_HELP)){
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "gate", options );
			return;
		}

		for (Option option: cmd.getOptions()) {
			switch (option.getOpt()) {
				case OPT_GATE_PROPERTY:
					if (option.getValues().length > 1) { // Set it
						log.trace("Setting Gate property: " + option.getValue(0) + "=" + option.getValue(1));
						GateProps.setProperty(option.getValue(0), option.getValue(1));
					} else { // Reset it
						log.warn("Removing Gate property: " + option.getValue(0));
						GateProps.removeProperty(option.getValue(0));
					}
					break;
			}
		}

		// start Gate by specified mode
        if(cmd.hasOption(OPT_NON_GUI)){
            log.info("Start CMD Mode");
			// CMD mode is default. don't need to set it.
			String testFile = cmd.getOptionValue(OPT_TEST_FILE).trim();
			String suiteNamePrefix = cmd.getOptionValue(OPT_SUITE_PREFIX, "").trim();
			String caseNamePrefix = cmd.getOptionValue(OPT_CASE_PREFIX, "").trim();
            GateCMDLauncher launcher = new GateCMDLauncher();
            launcher.launch(testFile, suiteNamePrefix, caseNamePrefix);
        }else {
		    log.info("Start GUI Mode");
			GateProps.setGuiMode();
			GuiPackage.initInstance();
			GuiPackage.getIns().getMainFrame().setVisible(true);
            if(cmd.hasOption(OPT_TEST_FILE)){
				String testFile = cmd.getOptionValue(OPT_TEST_FILE).trim();
				File gateTestFile = new File(testFile);
				if(gateTestFile.canRead()){
					GuiPackage.getIns().getMainFrame().setTestFile(gateTestFile.getAbsolutePath());
					GuiPackage.getIns().getMainFrame().updateTitle();
					try {
						GateTreeSupport.load(gateTestFile);
					} catch (Throwable t) {
						OptionPane.showErrorMessageDialog("fail to load test file: " + gateTestFile.getAbsolutePath(), t);
						log.fatal("fail to load test file: " + gateTestFile.getAbsolutePath(), t);
					}
				}else{
					OptionPane.showMessageDialog("Can not read test file: " + gateTestFile.getAbsolutePath());
					log.fatal("Can not read test file: " + gateTestFile.getAbsolutePath());
				}
			}
        }
	}

	static void initializeProperties(){
		loadProperties(System.getProperties(), new File("system.properties"));
		loadProperties(GateProps.getProperties(), new File("gate.properties"));
	}

	static void loadProperties(Properties properties, File file){
		FileInputStream fis = null;
		try {
			if (file.canRead()){
				log.info("Loading properties file from: " + file.getCanonicalPath());
				fis = new FileInputStream(file);
				properties.load(fis);
			}else{
				log.error("Fail to Load properties file: " + file.getCanonicalPath());
			}
		} catch (IOException e) {
			log.warn("Error loading system property file: system.properties", e);
		} finally {
			GateUtils.closeQuietly(fis);
		}
	}

}
