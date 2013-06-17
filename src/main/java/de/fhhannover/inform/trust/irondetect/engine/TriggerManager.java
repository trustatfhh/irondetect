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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.irondetect.model.ConditionElement;
import de.fhhannover.inform.trust.irondetect.model.Rule;
import de.fhhannover.inform.trust.irondetect.util.event.Event;
import de.fhhannover.inform.trust.irondetect.util.event.EventReceiver;
import de.fhhannover.inform.trust.irondetect.util.event.EventSender;
import de.fhhannover.inform.trust.irondetect.util.event.TriggerUpdateEvent;

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
	 * de.fhhannover.inform.trust.irondetect.util.EventSender#addEventReceiver
	 * (de.fhhannover.inform.trust.irondetect.util.EventReceiver)
	 */
	@Override
	public void addEventReceiver(EventReceiver er) {
		if (!this.eventReceiver.contains(er)) {
			this.eventReceiver.add(er);
		}
	}
}
