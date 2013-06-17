/**
 * 
 */
package de.fhhannover.inform.trust.irondetect.util;

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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.repository.FeatureBaseImpl;
import de.fhhannover.inform.trust.irondetect.util.event.Event;
import de.fhhannover.inform.trust.irondetect.util.event.EventReceiver;
import de.fhhannover.inform.trust.irondetect.util.event.EventType;
import de.fhhannover.inform.trust.irondetect.util.event.FeatureBaseUpdateEvent;

/**
 * @author bahellma
 *
 */
public class FeatureBaseWatcher implements Runnable, EventReceiver {
	
	private Logger logger = Logger.getLogger(FeatureBaseWatcher.class);
	
	private LinkedBlockingQueue<Event> incomingEvents = new LinkedBlockingQueue<Event>();

	private FeatureBaseImpl featureBase;
	
	public FeatureBaseWatcher() {
		this.featureBase = FeatureBaseImpl.getInstance();
	}

	@Override
	public void run() {
		logger.info(FeatureBaseWatcher.class.getSimpleName()
				+ " has started.");

		try {
			while (!Thread.currentThread().isInterrupted()) {
				Event e = incomingEvents.take();
				onNewPollResult(e);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.info("Got interrupt signal while waiting for new work, exiting ...");
		} finally {
			logger.info("Shutdown complete.");
		}
	}

	private void onNewPollResult(Event e) {
		logger.info("Got new event: " + e.toString());
		
		if (e.getType() == EventType.FEATURE_BASE_UPDATE) {
			Map<String, Set<String>> payload = ((FeatureBaseUpdateEvent) e).getPayload();
			
			for (String device : payload.keySet()) {
				for (String featureId : payload.get(device)) {
					logger.trace("Device: " + device + ",\tQualified FeatureId: " + featureId);
				}
			}
		}
		
		List<String> allDevices = this.featureBase.getAllDevices();
		
		logger.trace("All devices:");
		for (String device : allDevices) {
			logger.trace(device);
		}
		
		if (allDevices.size() > 0) {
			logger.trace("All current features for device '" + allDevices.get(0) + "':");
			
			List<Pair<Feature, Boolean>> allCurrentFeaturesForDevice = this.featureBase.getAllCurrentFeaturesForDevice(allDevices.get(0));
			for (Pair<Feature, Boolean> pair : allCurrentFeaturesForDevice) {
				logger.trace(pair.getFirstElement().getQualifiedId() + " = " + pair.getFirstElement().getValue());
			}
			
			List<Feature> allCurrentNotDeletedFeaturesForDevice = this.featureBase.getAllCurrentNotDeletedFeaturesForDevice(allDevices.get(0));
			logger.info("Number for current, not deleted features for device '" + allDevices.get(0) + "': " + allCurrentNotDeletedFeaturesForDevice.size());
		}
	}
	
	/* (non-Javadoc)
	 * @see de.fhhannover.inform.trust.irondetect.util.EventReceiver#submitNewEvent(de.fhhannover.inform.trust.irondetect.util.EventType)
	 */
	public void submitNewEvent(Event e) {
		try {
			this.incomingEvents.put(e);
			logger.trace("Event was inserted");
		} catch (InterruptedException e1) {
			logger.error("Could not add Event to FeatureBaseWatcher:"
					+ e1.getMessage());
		}
	}
}
