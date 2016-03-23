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
 * This file is part of irondetect, version 0.0.9, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2016 Trust@HsH
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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.util;



import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.repository.FeatureBaseImpl;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.EventType;
import de.hshannover.f4.trust.irondetect.util.event.FeatureBaseUpdateEvent;

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
	 * @see de.hshannover.f4.trust.irondetect.util.EventReceiver#submitNewEvent(de.hshannover.f4.trust.irondetect.util.EventType)
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
