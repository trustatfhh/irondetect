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
package de.hshannover.f4.trust.irondetect.livechecker.rest;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.hshannover.f4.trust.ifmapj.exception.UnmarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.irondetect.livechecker.ifmap.IdentifierGraphToFeatureMapper;
import de.hshannover.f4.trust.irondetect.livechecker.policy.publisher.LiveCheckerPolicyActionUpdater;
import de.hshannover.f4.trust.visitmeta.implementations.IdentifierGraphImpl;
import de.hshannover.f4.trust.visitmeta.implementations.IdentifierImpl;
import de.hshannover.f4.trust.visitmeta.implementations.LinkImpl;
import de.hshannover.f4.trust.visitmeta.implementations.MetadataImpl;
import de.hshannover.f4.trust.visitmeta.interfaces.Identifier;
import de.hshannover.f4.trust.visitmeta.interfaces.IdentifierGraph;
import de.hshannover.f4.trust.visitmeta.interfaces.Metadata;

/**
 * TODO
 *
 * @author Marcel Reichenbach
 *
 */

@Path("/")
public class LiveCheckerResource {

	private static final Logger LOGGER = Logger.getLogger(LiveCheckerResource.class);

	private ObjectMapper mObjectMapper = new ObjectMapper();

	private DocumentBuilder mBuilder;

	private DocumentBuilderFactory mBuilderFactory;

	private boolean mIncludeRawXML = true;

	private static final String TIMESTAMP = "timestamp";
	private static final String LINKS = "links";
	private static final String IDENTIFIERS = "identifiers";
	private static final String TYPENAME = "typename";
	private static final String PROPERTIES = "properties";
	private static final String METADATA = "metadata";
	private static final String DELTA_UPDATES = "updates";
	private static final String DELTA_DELETES = "deletes";
	private static final String RAW_XML = "rawData";

	/**
	 *
	 * @param jsonConnectionData
	 * @return
	 */
	@PUT
	@Path("livechecker")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response checkFeatureData(String jsonFeatureData) {
		// transform
		JsonNode rootNode = parseJson(jsonFeatureData);
		List<IdentifierGraph> identifierGraphList = extractGraphsFromJson(
				rootNode);


		//

		IdentifierGraphToFeatureMapper test = new IdentifierGraphToFeatureMapper();
		test.addNewFeaturesToFeatureBase(identifierGraphList);

		//

		Map<Identity, List<Document>> graphMap = new HashMap<Identity, List<Document>>();

		for (IdentifierGraph identifierGraph : identifierGraphList) {
			for (Identifier identifiers : identifierGraph.getIdentifiers()) {
				String identifierRawData = identifiers.getRawData();
				Document identifierDocument = buildDocument(identifierRawData);
				de.hshannover.f4.trust.ifmapj.identifier.Identifier identifier;
				try {
					identifier = Identifiers.fromElement(identifierDocument.getDocumentElement());
				} catch (UnmarshalException e) {
					return responseError("Data transform", e.toString());
				}

				// only for Identity-Identifier
				if(!(identifier instanceof Identity)){
					continue;
				}

				Identity identity = (Identity) identifier;
				List<Document> metaDataList = new ArrayList<Document>();
				if (!graphMap.containsKey(identity)) {
					graphMap.put(identity, metaDataList);
				}

				for (Metadata meta : identifiers.getMetadata()) {
					String metadataRawData = meta.getRawData();
					Document metadataDocument = buildDocument(metadataRawData);

					metaDataList.add(metadataDocument);
				}
			}
		}

		LiveCheckerPolicyActionUpdater.getInstance().submitNewMapGraph(graphMap);


		return Response.ok().entity("New FeatureData was checked").build();
	}

	private Document buildDocument(String rawData) {
		if (mBuilderFactory == null) {
			mBuilderFactory = DocumentBuilderFactory.newInstance();
			mBuilderFactory.setNamespaceAware(true);
		}
		if (mBuilder == null) {
			try {
				mBuilder = mBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				LOGGER.error(e.getMessage());
			}
		}

		Document doc = null;
		try {
			doc = mBuilder.parse(new InputSource(new StringReader(rawData)));
		} catch (SAXException | IOException e) {
			LOGGER.error("could not convert InternalMetada to Document!");
			LOGGER.error(e.getMessage());
		}

		return doc;
	}

	private Response responseError(String errorWhile, String exception) {
		String msg = "error while " + errorWhile + " | Exception: " + exception;
		LOGGER.error(msg);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
	}

	/**
	 * @throws RuntimeException
	 */
	private JsonNode parseJson(String json) {
		LOGGER.trace("Method parseJson(...) called.");
		LOGGER.trace("Parameter 'json': " + json);
		try {
			// TODO <VA>: debug only, remove later?
			ObjectMapper mapper = new ObjectMapper();
			Object jsonObject = mapper.readValue(json, Object.class);
			String jsonFormatted = mapper.defaultPrettyPrintingWriter()
					.writeValueAsString(jsonObject);
			LOGGER.debug("Parameter 'json' (formatted) in parseJson():\n"
					+ jsonFormatted);

			return mObjectMapper.readTree(json);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(
					"could not parse '" + json + "' as JSON: " + e.getMessage(),
					e);
		} catch (IOException e) {
			throw new RuntimeException("error while reading '" + json
					+ "' as JSON: " + e.getMessage(), e);
		}
	}

	/**
	 * @param json
	 * @return
	 */
	private List<IdentifierGraph> extractGraphsFromJson(JsonNode rootNode) {
		List<IdentifierGraph> graphs = new ArrayList<IdentifierGraph>();
		Iterator<JsonNode> jsonGraphs = rootNode.getElements();
		while (jsonGraphs.hasNext()) {
			JsonNode currentJsonGraph = jsonGraphs.next();
			IdentifierGraph graph = buildGraphFromJson(currentJsonGraph);
			graphs.add(graph);
		}
		return graphs;
	}

	private IdentifierGraph buildGraphFromJson(JsonNode jsonGraph) {
		LOGGER.trace("Method buildGraphFromJson(...) called.");
		LOGGER.trace("Parameter 'jsonGraph': " + jsonGraph);
		JsonNode jsonTimestamp = jsonGraph.get(TIMESTAMP);
		if (jsonTimestamp == null) {
			throw new RuntimeException(
					"timestamp for graph is missing in '" + jsonGraph + "'");
		}
		if (!jsonTimestamp.isLong()) {
			throw new RuntimeException(
					"timestamp is not a long in '" + jsonGraph + "'");
		}
		long timestamp = jsonTimestamp.getLongValue();

		IdentifierGraphImpl graph = new IdentifierGraphImpl(timestamp);

		JsonNode jsonlinkList = jsonGraph.get(LINKS);
		if (jsonlinkList == null) {
			throw new RuntimeException(
					"no links in graph '" + jsonGraph + "' found");
		}

		insertIdentifierInto(graph, jsonlinkList);

		return graph;
	}

	private void insertIdentifierInto(IdentifierGraphImpl graph,
			JsonNode jsonLinkList) {
		LOGGER.trace("Method insertIdentifierInto(...) called.");
		Iterator<JsonNode> jsonLinks = jsonLinkList.getElements();
		while (jsonLinks.hasNext()) {
			JsonNode jsonLink = jsonLinks.next();
			JsonNode identifiers = jsonLink.get(IDENTIFIERS);
			JsonNode metadata = jsonLink.get(METADATA);

			// two identifiers -> link
			if (identifiers.isArray()) {
				LOGGER.trace("JsonNode contains two identifiers");

				Iterator<JsonNode> ids = identifiers.getElements();
				List<IdentifierImpl> identifierList = new ArrayList<IdentifierImpl>();
				while (ids.hasNext()) {
					JsonNode currentIdentifier = ids.next();
					IdentifierImpl identifier = (IdentifierImpl) buildIdentifierFromJson(
							currentIdentifier);
					identifierList.add(identifier);
				}

				for (IdentifierImpl identifier : identifierList) {
					IdentifierImpl identifierFound = graph
							.findIdentifier(identifier);
					if (identifierFound == null) {
						LOGGER.trace(
								"Identifier not found in graph, inserting: "
										+ identifier);
						graph.insert(identifier);
					} else {
						identifierList.set(identifierList.indexOf(identifier),
								identifierFound);
					}
				}

				LinkImpl linkImpl = graph.connect(identifierList.get(0),
						identifierList.get(1));

				LOGGER.trace("Adding metadata to link'" + linkImpl + "'");
				List<Metadata> metadataList = extractMetadata(metadata);
				for (Metadata m : metadataList) {
					linkImpl.addMetadata(m);
				}

				LOGGER.trace("Creating link: " + linkImpl);

				// one identifier
			} else {
				LOGGER.trace("JsonNode contains one identifier");

				// convert Identifier to IdentifierImpl
				IdentifierImpl identifier = (IdentifierImpl) buildIdentifierFromJson(
						identifiers);

				// FIXME InternalIdentifier on the client side? bah?!
				// TODO <VA> Who wrote the above FIXME comment and what shall it
				// mean? Is it already fixed?

				// insert identifier into graph
				IdentifierImpl identifierFound = graph
						.findIdentifier(identifier);
				if (identifierFound == null) {
					LOGGER.trace(
							"Identifier not found, inserting: " + identifier);
					graph.insert(identifier);
				} else {
					identifier = identifierFound;
				}

				LOGGER.trace(
						"Adding metadata to identifier '" + identifier + "'");
				List<Metadata> metadataList = extractMetadata(metadata);
				for (Metadata m : metadataList) {
					identifier.addMetadata(m);
				}

			}
		}
	}

	private List<Metadata> extractMetadata(JsonNode jsonNode) {
		LOGGER.trace("Method extractMetadata(..) called");
		LOGGER.trace("Parameter 'jsonNode':" + jsonNode);
		if (jsonNode == null) {
			LOGGER.trace("No metadata found.");
			return new ArrayList<Metadata>();
		}

		Metadata metadatum = null;
		List<Metadata> metadataList = new ArrayList<Metadata>();

		// multiple metadata
		if (jsonNode.isArray()) {
			Iterator<JsonNode> metadatas = jsonNode.getElements();
			JsonNode currentMetadata = null;
			while (metadatas.hasNext()) {
				currentMetadata = metadatas.next();
				metadatum = buildMetadataFromJson(currentMetadata);
				metadataList.add(metadatum);
			}

			// only one metadatum
		} else {
			metadatum = buildMetadataFromJson(jsonNode);
			metadataList.add(metadatum);
		}

		return metadataList;
	}

	private Metadata buildMetadataFromJson(JsonNode jsonMetadata) {
		LOGGER.trace("Method buildIdentifierFromJson(...) called.");
		LOGGER.trace("Parameter 'jsonIdentifier': " + jsonMetadata);
		JsonNode jsonTypename = jsonMetadata.get(TYPENAME);
		if (jsonTypename == null) {
			throw new RuntimeException(
					"no typename found for metadata '" + jsonMetadata + "'");
		}
		String typename = jsonTypename.getValueAsText();

		JsonNode jsonProperties = jsonMetadata.get(PROPERTIES);
		if (jsonProperties == null) {
			throw new RuntimeException(
					"no properties found for metadata '" + jsonMetadata + "'");
		}

		boolean isSingleValue = false;
		long publishTimestamp = 0;
		Map<String, String> propertiesMap = new HashMap<String, String>();

		Iterator<String> properties = jsonProperties.getFieldNames();
		while (properties.hasNext()) {
			String key = properties.next();
			String value = jsonProperties.get(key).getValueAsText();

			if (key.contains("@ifmap-cardinality")) {
				isSingleValue = value.equalsIgnoreCase("singleValue") ? true
						: false;
			} else if (key.contains("[@ifmap-timestamp]")) {
				publishTimestamp = getXsdStringAsCalendar(value);
			}

			propertiesMap.put(key, value);
		}

		LOGGER.trace("Creating metadata (typename: " + typename
				+ ", isSingleValue: " + isSingleValue + ", publishTimestamp: "
				+ publishTimestamp + ")");
		MetadataImpl metadata = new MetadataImpl(typename, isSingleValue,
				publishTimestamp);
		for (String key : propertiesMap.keySet()) {
			LOGGER.trace("Adding property to metadata: " + key + " = "
					+ propertiesMap.get(key));
			metadata.addProperty(key, propertiesMap.get(key));
		}

		if (mIncludeRawXML) {
			JsonNode jsonRawXML = jsonMetadata.get(RAW_XML);
			if (jsonRawXML != null) {
				metadata.setRawData(jsonRawXML.getValueAsText());
			}
		}

		return metadata;
	}

	private Identifier buildIdentifierFromJson(JsonNode jsonIdentifier) {
		LOGGER.trace("Method buildIdentifierFromJson(...) called.");
		LOGGER.trace("Parameter 'jsonIdentifier': " + jsonIdentifier);
		JsonNode jsonTypename = jsonIdentifier.get(TYPENAME);
		if (jsonTypename == null) {
			throw new RuntimeException("no typename found for identifier '"
					+ jsonIdentifier + "'");
		}
		String typename = jsonTypename.getValueAsText();
		IdentifierImpl identifier = new IdentifierImpl(typename);

		JsonNode jsonProperties = jsonIdentifier.get(PROPERTIES);
		if (jsonProperties == null) {
			throw new RuntimeException("no properties found for identifier '"
					+ jsonIdentifier + "'");
		}
		Iterator<String> properties = jsonProperties.getFieldNames();
		while (properties.hasNext()) {
			String key = properties.next();
			String value = jsonProperties.get(key).getValueAsText();

			identifier.addProperty(key, value);
		}

		if (mIncludeRawXML) {
			JsonNode jsonRawXML = jsonIdentifier.get(RAW_XML);
			if (jsonRawXML != null) {
				identifier.setRawData(jsonRawXML.getValueAsText());
			}
		}

		return identifier;
	}

	/**
	 * Transforms a given xsd:DateTime {@link String} (e.g.
	 * 2003-05-31T13:20:05-05:00) to a Java {@link Calendar} object. Uses
	 * DatetypeConverter to parse the {@link String} object.
	 *
	 * @param xsdDateTime
	 *            - the xsd:DateTime that is to be transformed.
	 * @return a {@link Calendar} object representing the given xsd:DateTime
	 */
	private Long getXsdStringAsCalendar(String xsdDateTime) {
		assert xsdDateTime != null && !xsdDateTime.isEmpty();
		try {
			if (xsdDateTime.contains("+")) {
				int idxTz = xsdDateTime.lastIndexOf("+");
				int idxLastColon = xsdDateTime.lastIndexOf(":");
				if (idxLastColon < idxTz) {
					String p1 = xsdDateTime.substring(0, idxTz + 3);
					String p2 = xsdDateTime.substring(idxTz + 3,
							xsdDateTime.length());
					xsdDateTime = p1 + ":" + p2;
				}
			}
			if (xsdDateTime.contains(":")) { // if the String contains an ':'
				// literal, we try to interpret
				// it as a xsdDateTime-string
				return DatatypeConverter.parseDateTime(xsdDateTime)
						.getTimeInMillis();
			} else { // try to parse a time in milliseconds to a Calendar object
				Calendar tmp = new GregorianCalendar();
				tmp.setTimeInMillis(Long.parseLong(xsdDateTime));
				return tmp.getTimeInMillis();
			}
		} catch (IllegalArgumentException e) {
			LOGGER.error("Illegal data/time format found (incoming String was: "
					+ xsdDateTime + "); setting to current date/time.");
			return new GregorianCalendar().getTimeInMillis();
		}
	}

}
