package de.fhhannover.inform.trust.irondetect.gui;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.irondetect.util.event.Event;
import de.fhhannover.inform.trust.irondetect.util.event.EventReceiver;
import de.fhhannover.inform.trust.irondetect.util.event.ResultUpdateEvent;

public class ResultLoggerImpl implements ResultLogger, Runnable {

	private Logger logger = Logger.getLogger(ResultLoggerImpl.class);
	
	private static ResultLoggerImpl instance;

	private List<EventReceiver> eventReceiver;

	private LinkedBlockingQueue<ResultObject> incomingResults;

	private ResultLoggerImpl() {
		this.eventReceiver = new ArrayList<EventReceiver>();
		this.incomingResults = new LinkedBlockingQueue<ResultObject>();
	}
	
	public synchronized static ResultLoggerImpl getInstance() {
		if (instance == null) {
			instance = new ResultLoggerImpl();
		}
		return instance;
	}

	@Override
	public void addEventReceiver(EventReceiver er) {
		if (!this.eventReceiver.contains(er)) {
			this.eventReceiver.add(er);
		}
	}

	@Override
	public void reportResultsToLogger(String device, String id, String type,
			boolean value) {

		try {
			this.incomingResults.put(new ResultObject(device, type, id, value));
			logger.trace("ResultObject was inserted");
		} catch (InterruptedException e) {
			logger.error("Could not add ResultObject to ResultLoggerImpl:"
					+ e.getMessage());
		}
	}

	@Override
	public void run() {
		logger.info(ResultLoggerImpl.class.getSimpleName() + " has started.");

		try {
			while (!Thread.currentThread().isInterrupted()) {
				ResultObject lastPollResult = incomingResults.take();
				onNewResultObject(lastPollResult);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.info("Got interrupt signal while waiting for new work, exiting ...");
		} finally {
			logger.info("Shutdown complete.");
		}
	}

	private void onNewResultObject(ResultObject lastPollResult) {
		Event e = new ResultUpdateEvent(lastPollResult);

		for (EventReceiver er : this.eventReceiver) {
			er.submitNewEvent(e);
		}
		logger.trace("ResultsObjects were send to registered receivers.");
	}

}
