package de.fhhannover.inform.trust.irondetect.ifmap;

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

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.ifmapj.channel.ARC;
import de.fhhannover.inform.trust.ifmapj.exception.EndSessionException;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.exception.IfmapException;
import de.fhhannover.inform.trust.ifmapj.messages.PollResult;
import de.fhhannover.inform.trust.irondetect.util.Constants;

public class EndpointPoller implements Runnable {

	private Logger logger = Logger.getLogger(EndpointPoller.class);

	private ARC mArc;
	private IfmapToFeatureMapper mMapper;

	public EndpointPoller(IfmapToFeatureMapper mapper, ARC arc) {
		mMapper = mapper;
		mArc = arc;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				PollResult pollResult = mArc.poll();
				mMapper.submitNewSearchResult(pollResult);
			} catch (IfmapErrorResult e) {
				logger.error("Got IfmapError: " + e.getMessage() + ", " + e.getCause());
			} catch (EndSessionException e) {
				logger.error("The session with the MAP server was closed: " + e.getMessage() + ", " + e.getCause());
				System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
			} catch (IfmapException e) {
				logger.error("Error at polling the MAP server: " + e.getMessage() + ", " + e.getCause());
				System.exit(Constants.RETURN_CODE_ERROR_IFMAPJ_EXCEPTION);
			}
		}
	}
}
