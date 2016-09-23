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



import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.hshannover.f4.trust.ironcommon.properties.Properties;
import de.hshannover.f4.trust.irondetect.Main;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Constants;
import de.hshannover.f4.trust.irondetect.util.Helper;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author bahellma
 * 
 */
public class ActionToIfmapMapper {

	private static ActionToIfmapMapper instance;
	
	private Properties mConfig = Main.getConfig();

	private static final StandardIfmapMetadataFactory mf = IfmapJ
			.createStandardMetadataFactory();

	private IfmapController controller;

	private boolean actionAsIfmapEvent;

	private static Logger logger = Logger.getLogger(ActionToIfmapMapper.class);

	/**
	 * 
	 */
	private ActionToIfmapMapper() {
		this.controller = null;
		this.actionAsIfmapEvent = mConfig.getBoolean(Configuration.KEY_PUBLISHER_ACTIONASIFMAPEVENT, Configuration.DEFAULT_VALUE_PUBLISHER_ACTIONASIFMAPEVENT);
	}

	/**
	 * @return
	 */
	public static ActionToIfmapMapper getInstance() {
		if (instance == null) {
			instance = new ActionToIfmapMapper();
		}
		return instance;
	}

	/**
	 * @param device
	 * @param keyValuePairs
	 */
	public void addNewAction(String device,
			List<Pair<String, String>> keyValuePairs) {
		if (this.controller != null) {
			List<Document> result = null;
			if (this.actionAsIfmapEvent) {
				logger.info("Creating IF-MAP event metadata for current action.");
				result = createIfmapEvent(keyValuePairs);
				if (result != null) {
					logger.trace("Submitting new Action to IfmapController: Identifier.");
					this.controller.submitNewEvent(device, result, true);
				} else {
					logger.error("Result was null.");
				}
			}

			logger.info("Creating alert feature for current action.");
			result = createActionMetadata(keyValuePairs);
			if (result != null) {
				logger.trace("Submitting new Action to IfmapController.");
				this.controller.submitNewEvent(device, result, false);
			} else {
				logger.error("Result was null.");
			}
		}
	}

	/**
	 * @param device
	 * @param keyValuePairs
	 * @return
	 */
	private List<Document> createActionMetadata(List<Pair<String, String>> keyValuePairs) {
		List<Document> result = new ArrayList<Document>();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder;
		Document doc;
		try {
			documentBuilder = factory.newDocumentBuilder();
			
			Element idElement;
			Element typeElement;
			Element valElement;
			Element alertFeature;
			for (Pair<String, String> p : keyValuePairs) {
				doc = documentBuilder.newDocument();
				
				alertFeature = doc.createElementNS(Constants.ESUKOM_NAMESPACE_URI, Constants.ESUKOM_NAMESPACE_PREFIX
						+ ":feature");
	
				alertFeature.setAttributeNS(null, "ifmap-cardinality", "multiValue");
				alertFeature.setAttribute("ctxp-timestamp",
						Helper.getCalendarAsXsdDateTime(new GregorianCalendar()));
				alertFeature.setAttribute("ctxp-position", "TODO");
				alertFeature.setAttribute("ctxp-other-devices", "TODO");
	
				idElement = doc.createElement("id");
				idElement.setTextContent(p.getFirstElement());
				
				typeElement = doc.createElement("type");
				typeElement.setTextContent("arbitrary");

				valElement = doc.createElement("value");
				valElement.setTextContent(p.getSecondElement());

				alertFeature.appendChild(idElement);
				alertFeature.appendChild(typeElement);
				alertFeature.appendChild(valElement);

				doc.appendChild(alertFeature);
				result.add(doc);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (result != null && result.size() > 0) {
			return result;
		} else {
			return null;
		}
	}

	/**
	 * @param device
	 * @param keyValuePairs
	 * @return
	 */
	private List<Document> createIfmapEvent(List<Pair<String, String>> keyValuePairs) {
		List<Document> result = new ArrayList<Document>();

		Map<String, String> ifmapKeyValues = converKeyValuesToIfmapEventData(keyValuePairs);
		
		String name = ifmapKeyValues.get(Constants.IFMAP_EVENT_NAME);
		if (name == null) {
			Iterator<Pair<String, String>> iterator = keyValuePairs.iterator();
			while (iterator.hasNext()) {
				Pair<String, String> next = iterator.next();
				if (next.getFirstElement().toLowerCase().contains("name")) {
					name = next.getSecondElement();
				}
			}
			
			if (name == null) {
				name = "none";
			}
		}
		
		String discoveredTime = Helper.getCalendarAsXsdDateTime(new GregorianCalendar());
		String discovererId = this.controller.getPublisherId();
		
		String magnitudeString = ifmapKeyValues.get(Constants.IFMAP_EVENT_MAGNITUDE);
		if (magnitudeString == null) {
			magnitudeString = "0";
		}
		Integer magnitude = Integer.parseInt(magnitudeString);
		
		String confidenceString = ifmapKeyValues.get(Constants.IFMAP_EVENT_CONFIDENCE);
		if (confidenceString == null) {
			confidenceString = "0";
		}
		Integer confidence = Integer.parseInt(confidenceString);
		
		Significance significance = convertStringToSignificance(ifmapKeyValues.get(Constants.IFMAP_EVENT_SIGNIFICANCE));
		if (significance == null) {
			significance = Significance.informational;
		}
		
		EventType type = EventType.other;
		String otherTypeDefinition = "esukom:alert";
		
		String information = ifmapKeyValues.get(Constants.IFMAP_EVENT_INFORMATION);
		if (information == null) {
			information = "information";
		}
		
		String vulnerabilityUri = ifmapKeyValues.get(Constants.IFMAP_EVENT_VULNERABILITY_URI);
		if (vulnerabilityUri == null) {
			vulnerabilityUri = "vulnerabilityUri";
		}
		
		Document event = mf.createEvent(name, discoveredTime, discovererId,
				magnitude, confidence, significance, type, otherTypeDefinition,
				information, vulnerabilityUri);
		result.add(event);
		
		return result;
	}

	private Significance convertStringToSignificance(String string) {
		if (string == null) {
			return null;
		} else if (string.equalsIgnoreCase(Significance.critical.toString())) {
			return Significance.critical;
		} else if (string.equalsIgnoreCase(Significance.important.toString())) {
			return Significance.important;
		} else if (string.equalsIgnoreCase(Significance.informational.toString())) {
			return Significance.informational;
		} else {
			return null;
		}
	}

	private Map<String, String> converKeyValuesToIfmapEventData(
			List<Pair<String, String>> keyValuePairs) {
		Map<String, String> result = new HashMap<String, String>();
		
		String key;
		String value;
		for (Pair<String, String> keyValue : keyValuePairs) {
			key = keyValue.getFirstElement();
			value = keyValue.getSecondElement();
			
			result.put(key, value);
		}
		
		return result;
	}

	/**
	 * @param controller
	 */
	public void setIfmapController(IfmapController controller) {
		this.controller = controller;
	}

}
