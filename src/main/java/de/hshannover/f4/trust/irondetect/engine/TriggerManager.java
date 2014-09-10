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
 * This file is part of irondetect, version 0.0.6, 
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2013 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.engine;



import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.Rule;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.EventSender;
import de.hshannover.f4.trust.irondetect.util.event.TriggerUpdateEvent;

/**
 * @author bahellma
 * 
 */
public class TriggerManager implements EventSender {

	private static Logger logger = Logger.getLogger(TriggerManager.class);

	private static TriggerManager instance;

	private Set<EventReceiver> eventReceiver;

	private NewTriggerPoller newTriggerPoller;

	private TriggerManager() {
		this.eventReceiver = new HashSet<EventReceiver>();

		this.newTriggerPoller = new NewTriggerPoller();
		Thread newTriggerPollerThread = new Thread(this.newTriggerPoller, "new-trigger-poller-thread");
		newTriggerPollerThread.start();
	}

	public static TriggerManager getInstance() {
		if (instance == null) {
			instance = new TriggerManager();
		}
		return instance;
	}

	/**
	 * @param timeInterval
	 * @param featureId
	 */
	public static void createNewTrigger(String device,
			Rule root, long slidingIntervall, String elementId) {
		logger.trace("Incoming new trigger for device '" + device + "'");
		Trigger t = new Trigger(device, root, slidingIntervall, elementId);
		logger.trace("Adding trigger: " + t);
		try {
			getInstance().newTriggerPoller.put(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param t
	 */
	protected static void notifyEventReceiver(Trigger t) {
		logger.trace("Sending TriggerUpdateEvent to all registered EventReceivers.");
		Event event = new TriggerUpdateEvent(t);
		for (EventReceiver e : getInstance().eventReceiver) {
			e.submitNewEvent(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.hshannover.f4.trust.irondetect.util.EventSender#addEventReceiver
	 * (de.hshannover.f4.trust.irondetect.util.EventReceiver)
	 */
	@Override
	public void addEventReceiver(EventReceiver er) {
		if (!this.eventReceiver.contains(er)) {
			this.eventReceiver.add(er);
		}
	}
}
