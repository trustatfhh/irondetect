/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 *
 * =====================================================
 *
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 *
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 *
 * This file is part of irondetect, version 0.0.8,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2015 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.hshannover.f4.trust.irondetect;

import java.net.URL;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.engine.Processor;
import de.hshannover.f4.trust.irondetect.engine.TriggerManager;
import de.hshannover.f4.trust.irondetect.gui.ResultLoggerImpl;
import de.hshannover.f4.trust.irondetect.gui.ResultVisualizer;
import de.hshannover.f4.trust.irondetect.ifmap.ActionToIfmapMapper;
import de.hshannover.f4.trust.irondetect.ifmap.EndpointPoller;
import de.hshannover.f4.trust.irondetect.ifmap.IfmapController;
import de.hshannover.f4.trust.irondetect.ifmap.IfmapToFeatureMapper;
import de.hshannover.f4.trust.irondetect.importer.Importer;
import de.hshannover.f4.trust.irondetect.importer.YamlImporter;
import de.hshannover.f4.trust.irondetect.livechecker.rest.RestService;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.policy.publisher.PolicyPublisher;
import de.hshannover.f4.trust.irondetect.repository.FeatureBase;
import de.hshannover.f4.trust.irondetect.repository.FeatureBaseImpl;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * Main entry point for irondetect.
 *
 * @author ibente
 * @author Bastian Hellmann
 * @author jvieweg
 *
 */
public class Main {
	public static final String VERSION = "0.0.8";

	private static RestService restService;

	private static Thread restServiceThread;

	private static Logger logger = Logger.getLogger(Main.class);

	private static Properties CONFIG;

	public static void main(String[] args) {
		logger.info("irondetect " + VERSION + " is running ...");

		// initialize modules
		initModules();

		// Create and Start main processing controller
		Processor processor = Processor.getInstance();

		// Create processor and thread
		Thread processingThread = new Thread(processor, "processor-thread");
		processingThread.start();
		
		try {
			PolicyPublisher policyUpdater = new PolicyPublisher(processor.getPolicy());
			processor.setPolicyPublisher(policyUpdater);
			
			
			boolean automaticPolicyReload = CONFIG.getBoolean(Configuration.KEY_POLICY_RELOAD_FROM_GRAPH,
					Configuration.DEFAULT_VALUE_POLICY_RELOAD_FROM_GRAPH);
			if (automaticPolicyReload) {
				logger.info("Start automatic reloading of policy from IF-MAP graph");
				processor.startPolicyAutomaticReload();
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error("Error while init PolicyPublisher", e);
		} catch (IfmapErrorResult e) {
			logger.error(e.getClass().getSimpleName() + " when send policy-Graph (" + e.toString() + ")");
		} catch (IfmapException e) {
			logger.error(e.getClass().getSimpleName() + " when send policy-Graph (Message= " + e.getMessage()
			+ " |Description= " + e.getDescription() + ")");
		}

		// Add EventReceivers to FeatureBase
		FeatureBase fb = FeatureBaseImpl.getInstance();
		fb.addEventReceiver(processor);

		// Create the manager for all trigger/sliding context related
		// correlation
		TriggerManager tm = TriggerManager.getInstance();
		tm.addEventReceiver(processor);

		// Create a ResultLogger and connect the ResultVisualizer to it
		ResultLoggerImpl resultLogger = ResultLoggerImpl.getInstance();
		Thread resultLoggerThread = new Thread(resultLogger,
				"result-logger-thread");

		if (CONFIG.getBoolean(Configuration.KEY_GUI_ENABLED, Configuration.DEFAULT_VALUE_GUI_ENABLED)) {
			// Create a ResultVisualizer (displays irondetect's result with a
			// GUI)
			ResultVisualizer resultVisualizer = new ResultVisualizer();
			resultLogger.addEventReceiver(resultVisualizer);
		}

		resultLoggerThread.start();

		boolean trainingEnabled = CONFIG.getBoolean(Configuration.KEY_TRAINING_ENABLED, Configuration.DEFAULT_VALUE_TRAINING_ENABLED);
		if (trainingEnabled) {
			Importer importer = new YamlImporter();
			List<Pair<String, Pair<Feature, Boolean>>> importedTrainingData = importer
					.loadTrainingDatabases(CONFIG.getString(Configuration.KEY_TRAINING_DIRECTORY, Configuration.DEFAULT_VALUE_TRAINING_DIRECTORY));
			if (importedTrainingData != null) {
				logger.info("Imported training data was NOT null -> will train now.");
				fb.addNewFeatures(importedTrainingData, true);
			} else {
				logger.info("Imported training data was null.");
			}
		} else {
			logger.info("Training was disabled.");
		}
		
		logger.info("Will begin testing now.");
		processor.setToTesting();

		// Create new IF-MAP-to-Feature Mapper
		IfmapToFeatureMapper ifmapToFeatureMapper = new IfmapToFeatureMapper();
		EndpointPoller.getInstance().addPollResultReceiver(ifmapToFeatureMapper);

		// Create IF-MAP to Feature mapper
		Thread mapperThread = new Thread(ifmapToFeatureMapper, "ifmap-to-feature-mapper-thread");
		mapperThread.start();

		// Start ifmap subscriber for configured pdp device identifiers
		IfmapController ifmapController = new IfmapController();

		// Create a new Action-To-IF-MAP Mapper and register the IF-MAP
		// controller.
		ActionToIfmapMapper.getInstance().setIfmapController(ifmapController);

		startRestService();
	}

	private static void startRestService() {
		logger.info("Start RestService");

		String url = CONFIG.getString(Configuration.KEY_REST_URL, Configuration.DEFAULT_VALUE_REST_URL);
		restService = new RestService(url, new HashSet<Class<?>>());

		restServiceThread = new Thread(restService, "RestService-Thread");
		restServiceThread.start();
	}

	private static void initModules() {
		logger.info("initializing modules ... ");

		getConfig();
	}

	public static Properties getConfig() {
		if (CONFIG == null) {
			URL config = Main.class.getClassLoader().getResource("irondetect.yml");
			String path = config.getPath();
			logger.info("Path: " + path);
			CONFIG = new Properties(path);
			if (CONFIG == null) {
				throw new RuntimeException("Application property has not been initialized. This is not good!");
			}
		}
		
		return CONFIG;
	}
	
}
