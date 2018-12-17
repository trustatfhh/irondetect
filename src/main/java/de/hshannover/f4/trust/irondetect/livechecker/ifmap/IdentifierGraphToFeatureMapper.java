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
 * Copyright (C) 2010 - 2018 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.livechecker.ifmap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ifmapj.messages.SearchResult;
import de.hshannover.f4.trust.irondetect.livechecker.repository.FeatureBaseForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Category;
import de.hshannover.f4.trust.irondetect.model.ContextParamType;
import de.hshannover.f4.trust.irondetect.model.ContextParameter;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.FeatureType;
import de.hshannover.f4.trust.irondetect.repository.FeatureBase;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.visitmeta.interfaces.Identifier;
import de.hshannover.f4.trust.visitmeta.interfaces.IdentifierGraph;
import de.hshannover.f4.trust.visitmeta.interfaces.Metadata;

/**
 * @author Marcel Reichenbach
 *
 */
public class IdentifierGraphToFeatureMapper {

	private static Logger LOGGER = Logger.getLogger(IdentifierGraphToFeatureMapper.class);

	private FeatureBaseForLiveCheck mFeatureBase;

	public IdentifierGraphToFeatureMapper() {
		this.mFeatureBase = FeatureBaseForLiveCheck.getInstance();
	}

	/**
	 * Map a new {@link SearchResult} to a new {@link Feature} instance and put it into the {@link FeatureBase}.
	 *
	 * @param lastPollResult the last {@link SearchResult} inside the incoming queue
	 */
	public Event addNewFeaturesToFeatureBase(List<IdentifierGraph> identifierGraphList) {
		//CoreComponent and deleteFlag - true if CC was deleted
		ArrayList<Pair<String, Pair<Feature, Boolean>>> coreComps = new ArrayList<Pair<String, Pair<Feature, Boolean>>>();

		for (IdentifierGraph identifierGraph : identifierGraphList) {
			LOGGER.info("New IdentifierGraph received(" + identifierGraph.getTimestamp() + ")...");

			String device = "[BLANK]";

			for (Identifier identifiers : identifierGraph.getIdentifiers()) {
				LOGGER.trace("Parsing Identifiers " + identifiers.toString());

				Category c1 = null;
				if (identifiers != null) {
					if (identifiers.getTypeName().equalsIgnoreCase("Identity")) {
						c1 = new Category(identifiers.valueFor("/identity[@name]"));
						device = identifiers.valueFor("/identity[@administrative-domain]");
					}

					if (c1 != null) {
						LOGGER.trace("resultItem root category=" + c1.getId());
					}
				}

				for (Metadata meta : identifiers.getMetadata()) {
					LOGGER.trace("Parsing metadata...");

					String id = "undefined";
					FeatureType type = new FeatureType(FeatureType.ARBITRARY);
					String value = "";
					Set<ContextParameter> ctx = new HashSet<ContextParameter>();
					LOGGER.trace("NodeName=" + meta.getTypeName());

					// feature
					if (meta.getTypeName().equalsIgnoreCase("feature")) {
						for (String propertyKey : meta.getProperties()) {
							String propertyValue = meta.valueFor(propertyKey);

							if (propertyKey.contains("ctxp-timestamp")) {
								ctx.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME),
										propertyValue));
							} else if (propertyKey.contains("ctxp-position")) {
								ctx.add(new ContextParameter(new ContextParamType(ContextParamType.LOCATION),
										propertyValue));
							} else if (propertyKey.contains("ctxp-other-devices")) {
								ctx.add(new ContextParameter(new ContextParamType(ContextParamType.OTHERDEVICES),
										propertyValue));
							} else if (propertyKey.contains("feature/id")) {
								id = propertyValue;
							} else if (propertyKey.contains("feature/type")) {
								if (propertyValue.equalsIgnoreCase("quantitive")) {
									type = new FeatureType(FeatureType.QUANTITIVE);
								} else if (propertyValue.equalsIgnoreCase("qualified")) {
									type = new FeatureType(FeatureType.QUALIFIED);
								} else {
									type = new FeatureType(FeatureType.ARBITRARY);
								}
							} else if (propertyKey.contains("feature/value")) {
								value = propertyValue;
							}

						}

						LOGGER.trace("Adding feature: dev=" + device + ",id=" + id + ",value=" + value + ", type="
								+ type + ",cat=" + c1 + ",ctx=" + ctx);

						Feature found = new Feature(id, value, type, c1, ctx);

						coreComps.add(new Pair<String, Pair<Feature, Boolean>>(device, new Pair<Feature, Boolean>(found,
								false)));
					}
				}
			}
		}


		LOGGER.debug("-----------------------Features found:----------------------");
		if (coreComps.size() > 0) {
			for (Pair<String, Pair<Feature, Boolean>> pair : coreComps) {
				Feature curr = pair.getSecondElement().getFirstElement();
				if (curr.getTrustLog() != null) {
					curr.getContextParameters().add(new ContextParameter(new ContextParamType(ContextParamType.TRUSTLEVEL), Integer.toString(curr.getTrustLog().getTrustLevel())));
				}
				LOGGER.debug("device=" + pair.getFirstElement() + ", " + pair.getSecondElement().getFirstElement());
			}
			LOGGER.debug(
					"Sending " + coreComps.size() + " features to FeatureBase");
			LOGGER.debug(
					"------------------------------------------------------------");
			return mFeatureBase.addNewFeatures(coreComps, false);
		} else {
			LOGGER.debug("No features were sent to FeatureBase.");
			LOGGER.debug(
					"------------------------------------------------------------");

			return null;
		}
	}

	private ArrayList<Integer> parseRating(String textContent) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		String[] sps = textContent.split(";");
		for (int i = 1; i <sps.length; i++) {
			String[] reA = sps[i].split(":");
			result.add(Integer.parseInt(reA[1]));
		}

		return result;
	}
}
