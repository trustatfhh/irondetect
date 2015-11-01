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
 * This file is part of visitmeta-dataservice, version 0.5.1,
 * implemented by the Trust@HsH research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2012 - 2015 Trust@HsH
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
package de.hshannover.f4.trust.irondetect.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.log.IfmapJLog;
import de.hshannover.f4.trust.ifmapj.metadata.Cardinality;
import de.hshannover.f4.trust.ifmapj.metadata.EnforcementAction;
import de.hshannover.f4.trust.ifmapj.metadata.EventType;
import de.hshannover.f4.trust.ifmapj.metadata.LocationInformation;
import de.hshannover.f4.trust.ifmapj.metadata.Significance;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.hshannover.f4.trust.ifmapj.metadata.WlanSecurityType;
import de.hshannover.f4.trust.irondetect.policy.publisher.util.PolicyStrings;

/**
 * 
 * @author Marcel Reichenbach
 *
 */
public class MetadataMock {

	public static final String XMLNS_PREFIX = "xmlns:";

	public static final String TIMESTAMP = "ifmap-timestamp";

	public static final String TIMESTAMP_FRACTION = "ifmap-timestamp-fraction";

	public static final String IFMAP_PUBLISHER_ID = "ifmap-publisher-id";

	public static final String ESUKOM_FEATURE_TYPE_QUANTITIVE = "quantitive";

	public static final String ESUKOM_FEATURE_TYPE_QUALIFIED = "qualified";

	public static final String TYPE_ARBITRARY = "arbitrary";

	public static final String ESUKOM_FEATURE_NAMESPACE = "http://www.esukom.de/2012/ifmap-metadata/1";

	public static final String ESUKOM_FEATURE_NAMESPACE_PREFIX = "esukom";

	private static final SimpleDateFormat XSD_DATE_TIME_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final SimpleDateFormat XSD_DATETIME_TIMEZONE_FORMATTER = new SimpleDateFormat("Z");
	
	private static final StandardIfmapMetadataFactory IFMAP_METADATA_FACTORY = IfmapJ.createStandardMetadataFactory();

	public static final String mPublisherID = "IfmapjMock";

	private static final DocumentBuilder mDocumentBuilder;

	static {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			mDocumentBuilder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			IfmapJLog.error("Could not get DocumentBuilder instance [" + e.getMessage() + "]");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simulated the irond(v. 0.5.1) and set default metadata attributes.
	 * 
	 * @param doc
	 * @param timestamp
	 */
	private static void setDocumentXmlAttributes(Document doc, Date timestamp) {
		String timezone = XSD_DATETIME_TIMEZONE_FORMATTER.format(timestamp);
		String timeValue = XSD_DATE_TIME_FORMATTER.format(timestamp) + timezone.substring(0, 3) + ":"
				+ timezone.substring(3);

		Element mXmlElement = (Element) doc.getFirstChild();

		String elementPrefix = doc.getDocumentElement().getPrefix();
		String elementUrl = doc.getDocumentElement().getNamespaceURI();

		// try to use the Document Element prefix and url for the xmlns attribute
		if (elementUrl != null && elementPrefix != null && elementUrl.length() > 0 && elementPrefix.length() > 0) {
			mXmlElement.setAttribute(XMLNS_PREFIX + elementPrefix, elementUrl);
		} else {
			// or set std
			mXmlElement.setAttribute(XMLNS_PREFIX + IfmapStrings.STD_METADATA_PREFIX, IfmapStrings.STD_METADATA_NS_URI);
		}

		mXmlElement.setAttribute(IFMAP_PUBLISHER_ID, mPublisherID);
		mXmlElement.setAttribute(TIMESTAMP_FRACTION, getSecondFraction(timestamp) + "");
		mXmlElement.setAttribute(TIMESTAMP, timeValue);
	}

	/**
	 * Copied from the irond(v. 0.5.1) sources (de.hshannover.f4.trust.iron.mapserver.utils.TimestampFraction)
	 * 
	 * Return the decimal fraction of a second of the given {@link Date}.
	 *
	 * @param dt the {@link Date} from with to extract the fraction
	 * @return a double containing the decimal fraction of a second
	 */
	private static double getSecondFraction(Date dt) {
		return dt.getTime() % 1000 / 1000.0;
	}

	public static Document createIpMac(String startTime, String endTime, String dhcpServer, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createIpMac(startTime, endTime, dhcpServer);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createIpMac(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createIpMac();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createArMac(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createArMac();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createArDev(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createArDev();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createArIp(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createArIp();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createAuthAs(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createAuthAs();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createAuthBy(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createAuthBy();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createDevIp(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createDevIp();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createDiscoveredBy(Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createDiscoveredBy();
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createRole(String name, String administrativeDomain, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createRole(name, administrativeDomain);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createRole(String name, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createRole(name);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createDevAttr(String name, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createDevAttr(name);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createCapability(String name, String administrativeDomain, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createCapability(name, administrativeDomain);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createCapability(String name, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createCapability(name);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createDevChar(String manufacturer, String model, String os, String osVersion,
			String deviceType, String discoveredTime, String discovererId, String discoveryMethod, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createDevChar(manufacturer, model, os, osVersion, deviceType, discoveredTime, discovererId, discoveryMethod);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createEnforcementReport(EnforcementAction enforcementAction, String otherTypeDefinition,
			String enforcementReason, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createEnforcementReport(enforcementAction, otherTypeDefinition, enforcementReason);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createEvent(String name, String discoveredTime, String discovererId, Integer magnitude,
			Integer confidence, Significance significance, EventType type, String otherTypeDefinition,
			String information, String vulnerabilityUri, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createEvent(name, discoveredTime, discovererId, magnitude, confidence,
				significance, type, otherTypeDefinition, information, vulnerabilityUri);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document create(String elementName, String qualifiedName, String uri, Cardinality cardinality,
			Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.create(elementName, qualifiedName, uri, cardinality);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document create(String elementName, String qualifiedName, String uri, Cardinality cardinality,
			String attrName, String attrValue, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.create(elementName, qualifiedName, uri, cardinality, attrName, attrValue);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document create(String elementName, String qualifiedName, String uri, Cardinality cardinality,
			HashMap<String, String> attributes, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.create(elementName, qualifiedName, uri, cardinality, attributes);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createLayer2Information(Integer vlan, String vlanName, Integer port,
			String administrativeDomain, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createLayer2Information(vlan, vlanName, port, administrativeDomain);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createLocation(List<LocationInformation> locationInformation, String discoveredTime,
			String discovererId, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createLocation(locationInformation, discoveredTime, discovererId);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createRequestForInvestigation(String qualifier, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createRequestForInvestigation(qualifier);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createWlanInformation(String ssid, List<WlanSecurityType> ssidUnicastSecurity,
			WlanSecurityType ssidGroupSecurity, List<WlanSecurityType> ssidManagementSecurity,
			Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createWlanInformation(ssid, ssidUnicastSecurity, ssidGroupSecurity, ssidManagementSecurity);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createUnexpectedBehavior(String discoveredTime, String discovererId, Integer magnitude,
			Integer confidence, Significance significance, String type, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createUnexpectedBehavior(discoveredTime, discovererId, magnitude, confidence, significance, type);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createClientTime(String time, Date timestamp) {
		Document doc = IFMAP_METADATA_FACTORY.createClientTime(time);
		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

	public static Document createEsukomFeature(String ctxpOtherDevice, String ctxpPosition, Date ctxpTimeStamp,
			String id, String type, String value, Date timestamp) {
		String timezone = XSD_DATETIME_TIMEZONE_FORMATTER.format(ctxpTimeStamp);
		String timeValue = XSD_DATE_TIME_FORMATTER.format(ctxpTimeStamp) + timezone.substring(0, 3) + ":"
				+ timezone.substring(3);

		Document doc = mDocumentBuilder.newDocument();
		Element feature = doc.createElementNS(ESUKOM_FEATURE_NAMESPACE, ESUKOM_FEATURE_NAMESPACE_PREFIX + ":feature");

		feature.setAttributeNS(null, PolicyStrings.IFMAP_CARDINALITY, Cardinality.multiValue.toString());
		feature.setAttribute("ctxp-timestamp", timeValue);
		feature.setAttribute("ctxp-position", ctxpPosition);
		feature.setAttribute("ctxp-other-devices", ctxpOtherDevice);

		Element idElement = doc.createElementNS(null, "id");
		idElement.setTextContent(id);

		Element typeElement = doc.createElementNS(null, "type");
		typeElement.setTextContent(type);

		Element valueElement = doc.createElementNS(null, "value");
		valueElement.setTextContent(value);

		feature.appendChild(idElement);
		feature.appendChild(typeElement);
		feature.appendChild(valueElement);

		doc.appendChild(feature);

		setDocumentXmlAttributes(doc, timestamp);
		return doc;
	}

}
