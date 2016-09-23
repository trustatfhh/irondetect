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
 * This file is part of irondetect, version 0.0.10, 
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
package de.hshannover.f4.trust.irondetect.ifmap;



import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.irondetect.util.Triple;

public class NewEventPoller implements Runnable {

	private Logger logger = Logger.getLogger(NewEventPoller.class);
	
	private IfmapController controller;
	private BlockingQueue<Triple<String, List<Document>, Boolean>> newEvents;

	public NewEventPoller(IfmapController controller) {
		this.controller = controller;
		this.newEvents = new LinkedBlockingQueue<Triple<String, List<Document>, Boolean>>();
	}

	@Override
	public void run() {
		Triple<String, List<Document>, Boolean> event;
		try {
			while (!Thread.currentThread().isInterrupted()) {
				event = this.newEvents.take();
				if (event != null) {
					logger.trace("Found new action event.");
					this.controller.publishEvent(event);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void put(String device, List<Document> metadataList, boolean ifmapEvent)
			throws InterruptedException {
		this.newEvents.put(new Triple<String, List<Document>, Boolean>(device, metadataList, ifmapEvent));
	}
}
