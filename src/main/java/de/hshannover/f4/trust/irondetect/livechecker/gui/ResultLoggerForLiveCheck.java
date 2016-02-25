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
package de.hshannover.f4.trust.irondetect.livechecker.gui;



import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.gui.ResultObject;
import de.hshannover.f4.trust.irondetect.gui.ResultObjectType;
import de.hshannover.f4.trust.irondetect.gui.ResultVisualizer;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.ResultUpdateEvent;

public class ResultLoggerForLiveCheck implements ResultLogger, Runnable {

	private Logger logger = Logger.getLogger(ResultLoggerForLiveCheck.class);

	private static ResultLoggerForLiveCheck instance;

	private List<EventReceiver> eventReceiver;

	private ResultVisualizer mResultVisualizer;

	private LinkedBlockingQueue<ResultObject> incomingResults;

	private ResultLoggerForLiveCheck() {
		this.eventReceiver = new ArrayList<EventReceiver>();
		this.incomingResults = new LinkedBlockingQueue<ResultObject>();
	}

	public synchronized static ResultLoggerForLiveCheck getInstance() {
		if (instance == null) {
			instance = new ResultLoggerForLiveCheck();
		}
		return instance;
	}

	@Override
	public void addEventReceiver(EventReceiver er) {
		if (!this.eventReceiver.contains(er)) {
			this.eventReceiver.add(er);
		}
	}

	public void resetEventReceiver() {
		eventReceiver = null;
		eventReceiver = new ArrayList<EventReceiver>();
	}

	@Override
	public void reportResultsToLogger(String device, String id, ResultObjectType type,
			boolean value) {

		try {
			this.incomingResults.put(new ResultObject(device, type, id, value));
			logger.trace("ResultObject was inserted");
		} catch (InterruptedException e) {
			logger.error("Could not add ResultObject to ResultLoggerImpl:"
					+ e.getMessage());
		}
	}

	//	@Override
	//	public void reportResultsToLogger(String device, String ruleId, String id, String type,
	//			boolean value) {
	//
	//		try {
	//			this.incomingResults.put(new ResultObject(device, type, id, value));
	//			logger.trace("ResultObject was inserted");
	//		} catch (InterruptedException e) {
	//			logger.error("Could not add ResultObject to ResultLoggerImpl:"
	//					+ e.getMessage());
	//		}
	//	}

	@Override
	public void run() {
		logger.info(ResultLoggerForLiveCheck.class.getSimpleName() + " has started.");

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

		if (mResultVisualizer != null) {
			Event liveCheck = new ResultUpdateEvent(new ResultObjectForLiveCheck(lastPollResult));
			mResultVisualizer.submitNewEvent(liveCheck);
		}

		logger.trace("ResultsObjects were send to registered receivers.");
	}

	public void addResultVisualizer(ResultVisualizer resultVisualizer) {
		mResultVisualizer = resultVisualizer;
	}

}
