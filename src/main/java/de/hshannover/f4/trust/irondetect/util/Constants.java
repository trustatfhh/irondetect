/*
 * #%L
 * =====================================================
 * 
 *   |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *     | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *     | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *     |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                              \____/
 * 
 *  =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de/
 * 
 * This file is part of irondetect, version 0.0.5, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
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
package de.hshannover.f4.trust.irondetect.util;



public class Constants {

	public static final String OTHER_TYPE_DEFINITION = "32939:category";
	public static final String ESUKOM_NAMESPACE_URI = "http://www.esukom.de/2012/ifmap-metadata/1";
	public static final String ESUKOM_NAMESPACE_PREFIX = "esukom";

	public static final String ALERT_IDENTIFIER_NAME = "smartphone.alert";

	public static final String MATCH_LINKS_ACCESS_REQUEST = "meta:access-request-device";

	/**
	 * The match-links filter for each Smartphone TODO Namespace?
	 */
	public static final String MATCH_LINKS_SMARTPHONE = "esukom:device-category or esukom:subcategory-of";
	public static final String RESULT_FILTER_SMARTPHONE = "esukom:device-category or esukom:subcategory-of";
	
	/**
	 * The match-links filter for each Pdp
	 */
	public static final String MATCH_LINKS_PDP = "meta:authenticated-by or meta:access-request-device";

	/**
	 * The result filter for each Pdp
	 */
	public static final String RESULT_FILTER_PDP = "meta:authenticated-by or meta:access-request-device";
	
	/**
	 * Exit-codes
	 */
	public static final int RETURN_CODE_SUCCES = 0;
	public static final int RETURN_CODE_ERROR_TRUSTSTORE_LOADING_FAILED = 1;
	public static final int RETURN_CODE_ERROR_IFMAPJ_INITIALIZATION_FAILED = 2;
	public static final int RETURN_CODE_ERROR_PARSER_CONFIGURATION_FAILED = 3;
	public static final int RETURN_CODE_ERROR_IFMAPJ_EXCEPTION = 4;
	public static final int RETURN_CODE_ERROR_POLICY_NOT_FOUND = 5;
	public static final int RETURN_CODE_ERROR_POLICY_PARSER_FAILED = 6;
	
	public static final String IFMAP_EVENT_NAME = "ifmap.event.name";
	public static final String IFMAP_EVENT_MAGNITUDE = "ifmap.event.magnitude";
	public static final String IFMAP_EVENT_CONFIDENCE = "ifmap.event.confidence";
	public static final String IFMAP_EVENT_INFORMATION = "ifmap.event.information";
	public static final String IFMAP_EVENT_VULNERABILITY_URI = "ifmap.event.vulnerabilityuri";
	public static final String IFMAP_EVENT_SIGNIFICANCE = "ifmap.event.significance";
	
}
