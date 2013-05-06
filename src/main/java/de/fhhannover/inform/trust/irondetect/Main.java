package de.fhhannover.inform.trust.irondetect;

/*
 * #%L
 * ====================================================
 *   _____                _     ____  _____ _   _ _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \|  ___| | | | | | |
 *    | | | '__| | | / __| __|/ / _` | |_  | |_| | |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _| |  _  |  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_|   |_| |_|_| |_|
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
 * Website: http://trust.inform.fh-hannover.de/
 * 
 * This file is part of irongui, version 0.0.3, implemented by the Trust@FHH 
 * research group at the Hochschule Hannover, a program to visualize the content
 * of a MAP Server (MAPS), a crucial component within the TNC architecture.
 * 
 * The development was started within the bachelor
 * thesis of Tobias Ruhe at Hochschule Hannover (University of
 * Applied Sciences and Arts Hannover). irongui is now maintained
 * and extended within the ESUKOM research project. More information
 * can be found at the Trust@FHH website.
 * %%
 * Copyright (C) 2010 - 2013 Trust@FHH
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

import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.database.Db4oToFeatureMapper;
import de.fhhannover.inform.trust.irondetect.engine.Processor;
import de.fhhannover.inform.trust.irondetect.engine.TriggerManager;
import de.fhhannover.inform.trust.irondetect.gui.ResultLoggerImpl;
import de.fhhannover.inform.trust.irondetect.gui.ResultVisualizer;
import de.fhhannover.inform.trust.irondetect.ifmap.ActionToIfmapMapper;
import de.fhhannover.inform.trust.irondetect.ifmap.IfmapController;
import de.fhhannover.inform.trust.irondetect.ifmap.IfmapToFeatureMapper;
import de.fhhannover.inform.trust.irondetect.repository.FeatureBase;
import de.fhhannover.inform.trust.irondetect.repository.FeatureBaseImpl;
import de.fhhannover.inform.trust.irondetect.util.Configuration;
import de.fhhannover.inform.trust.irondetect.util.Pair;

/**
 * Main entry point for irondetect.
 * 
 * @author ibente
 * @author Bastian Hellmann
 * @author jvieweg
 *
 */
public class Main
{
	private static final String VERSION = "0.0.3";
	
	private static Logger logger = Logger.getLogger(Main.class);
	
    public static void main(String[] args) {        
        // init log4j
    	URL resource = Main.class.getResource("/log4j.properties");
    	if (resource != null) {    		
    		System.out.println("URL of log4j properties file: " + resource.toString());
    		PropertyConfigurator.configure(resource);
    	} else {
    		System.out.println("Couldn't load log4j properties file.");
    	}
    	
    	logger.info("irondetect " + VERSION + " is running ...");
        		
    	// initialize modules
        initModules();
        
        // Create and Start main processing controller
        Processor processor = Processor.getInstance();

        // Add EventReceivers to FeatureBase
        FeatureBase fb = FeatureBaseImpl.getInstance();
        fb.addEventReceiver(processor);
        
        // Create the manager for all trigger/sliding context related correlation
        TriggerManager tm = TriggerManager.getInstance();
        tm.addEventReceiver(processor);
        
        // Create a ResultLogger and connect the ResultVisualizer to it
        ResultLoggerImpl resultLogger = ResultLoggerImpl.getInstance();
        Thread resultLoggerThread = new Thread(resultLogger, "result-logger-thread");        
        
        if (Configuration.loadGUI()) {
        	// Create a ResultVisualizer (displays irondetect's result with a GUI)
        	ResultVisualizer resultVisualizer = new ResultVisualizer();
        	resultLogger.addEventReceiver(resultVisualizer);
        }
        
        resultLoggerThread.start();
        
        // Create processor and thread
        Thread processingThread = new Thread(processor, "processor-thread");
        processingThread.start();
        
        List<Pair<String, Pair<Feature, Boolean>>> importedTrainingData = Db4oToFeatureMapper.loadTrainingDatabases();
        if (importedTrainingData != null) {
        	logger.info("Imported training data was NOT null -> will train now.");
        	fb.addNewFeatures(importedTrainingData, true);
        } else {
        	logger.info("Imported training data was null -> will begin testing now.");
        	processor.setToTesting();
        }
        
        // Create new IF-MAP-to-Feature Mapper
        IfmapToFeatureMapper ifmapToFeatureMapper = new IfmapToFeatureMapper();        
        
        // Create IF-MAP to Feature mapper
        Thread mapperThread = new Thread(ifmapToFeatureMapper, "ifmap-to-feature-mapper-thread");
        mapperThread.start();
        
        // Start ifmap subscriber for configured pdp device identifiers
        IfmapController ifmapController = new IfmapController(ifmapToFeatureMapper);
        
        // Create a new Action-To-IF-MAP Mapper and register the IF-MAP controller.
        ActionToIfmapMapper.getInstance().setIfmapController(ifmapController);
        
    }

	private static void initModules() {
		logger.info( "initializing modules ... " );
	}
}
