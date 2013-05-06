package de.fhhannover.inform.trust.irondetect.engine;

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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class NewTriggerPoller implements Runnable {

	private Logger logger = Logger.getLogger(NewTriggerPoller.class);
	private BlockingQueue<Trigger> newTriggers;
	private Map<String, Map<String, Thread>> triggerDeviceMap;
	
	public NewTriggerPoller() {
		this.newTriggers = new LinkedBlockingQueue<Trigger>();
		this.triggerDeviceMap = new HashMap<String, Map<String, Thread>>();
	}
	
	@Override
	public void run() {
		Trigger t;
		try {
			while (!Thread.currentThread().isInterrupted()) {
				t = this.newTriggers.take();
				if (t != null) {
					logger.trace("Found new trigger.");
					onNewTrigger(t);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void onNewTrigger(Trigger t) {
		String device = t.getDevice();
		String triggerId = t.getId();
		TriggerWorkerThread twt = new TriggerWorkerThread(t);
		
		Map<String, Thread> triggerList;
		Thread thread;
		
		if (!this.triggerDeviceMap.containsKey(device)) {
			triggerList = new HashMap<String, Thread>();
			this.triggerDeviceMap.put(device, triggerList);
			logger.info("Adding new TriggerWorkerThread to newly created list for device '" + device + "'");
		} else {	
			triggerList = this.triggerDeviceMap.get(device);
			logger.info("Found list of TriggerWorkerThreads for device '" + device + "'");
		}
		
		if (triggerList.containsKey(triggerId)) {
			triggerList.get(triggerId).interrupt();
			logger.info("Restarting trigger.");
		} else {
			thread = new Thread(twt, triggerId + "-trigger-worker-thread");
			thread.start();
			triggerList.put(triggerId, thread);
			logger.info("Adding new TriggerWorkerThread for exisiting list for device '" + device + "'");
		}		
	}

	public void put(Trigger t)
			throws InterruptedException {
		this.newTriggers.put(t);
	}
}
