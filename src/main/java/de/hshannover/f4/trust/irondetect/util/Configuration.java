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
package de.hshannover.f4.trust.irondetect.util;

/**
 * @author Ralf Steuerwald
 * @author Bastian Hellmann
 *
 */
public class Configuration {
	public static final String KEY_IFMAP_AUTH_METHOD = "irondetect.ifmap.method";
	public static final String DEFAULT_VALUE_IFMAP_AUTH_METHOD = "basic";

	public static final String KEY_IFMAP_BASIC_URL = "irondetect.ifmap.basic.url";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_URL = "http://localhost:8443";
	
	public static final String KEY_IFMAP_BASIC_PDPSUBSCRIBER_USERNAME = "irondetect.ifmap.basic.pdpsubscriber.username";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_PDPSUBSCRIBER_USERNAME = "irondetect-pdp";
	public static final String KEY_IFMAP_BASIC_PDPSUBSCRIBER_PASSWORD = "irondetect.ifmap.basic.pdpsubscriber.password";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_PDPSUBSCRIBER_PASSWORD = "irondetect-pdp";
	
	public static final String KEY_IFMAP_BASIC_DEVICESUBSCRIBER_USERNAME = "irondetect.ifmap.basic.devicesubscriber.username";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_DEVICESUBSCRIBER_USERNAME = "irondetect";
	public static final String KEY_IFMAP_BASIC_DEVICESUBSCRIBER_PASSWORD = "irondetect.ifmap.basic.devicesubscriber.password";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_DEVICESUBSCRIBER_PASSWORD = "irondetect";
	
	public static final String KEY_IFMAP_BASIC_POLICYPUBLISHER_USERNAME = "irondetect.ifmap.basic.policypublisher.username";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_POLICYPUBLISHER_USERNAME = "irondetect-publisher";
	public static final String KEY_IFMAP_BASIC_POLICYPUBLISHER_PASSWORD = "irondetect.ifmap.basic.policypublisher.password";
	public static final String DEFAULT_VALUE_IFMAP_BASIC_POLICYPUBLISHER_PASSWORD = "irondetect-publisher";
	
	public static final String KEY_IFMAP_CERT_URL = "irondetect.ifmap.cert.url";
	public static final String DEFAULT_VALUE_IFMAP_CERT_URL = "http://localhost:8444";

	public static final String KEY_IFMAP_TRUSTSTORE_PATH = "irondetect.ifmap.truststore.path";
	public static final String DEFAULT_VALUE_IFMAP_TRUSTSTORE_PATH = "/irondetect.jks";
	public static final String KEY_IFMAP_TRUSTSTORE_PASSWORD = "irondetect.ifmap.truststore.password";
	public static final String DEFAULT_VALUE_IFMAP_TRUSTSTORE_PASSWORD = "irondetect";

	public static final String KEY_IFMAP_THREADSAFE = "irondetect.ifmap.threadsafe";
	public static final boolean DEFAULT_VALUE_IFMAP_THREADSAFE = true;
	public static final String KEY_IFMAP_INITIALCONNECTIONTIMEOUT = "irondetect.ifmap.initialconnectiontimeout";
	public static final int DEFAULT_VALUE_IFMAP_INITIALCONNECTIONTIMEOUT = 120000;
	public static final String KEY_IFMAP_MAXRESULTSIZE = "irondetect.ifmap.maxresultsize";
	public static final int DEFAULT_VALUE_IFMAP_MAXRESULTSIZE = 100000000;
	
	public static final String KEY_TRAINING_ENABLED = "irondetect.training.enabled";
	public static final boolean DEFAULT_VALUE_TRAINING_ENABLED = false;
	public static final String KEY_TRAINING_DIRECTORY = "irondetect.training.directory";
	public static final String DEFAULT_VALUE_TRAINING_DIRECTORY = "training-dbs";

	public static final String KEY_POLICY_FILENAME = "irondetect.policy.filename";
	public static final String DEFAULT_VALUE_POLICY_FILENAME = "/policy/MobileDevicesSzenario.pol";
	
	public static final String KEY_PROCEDURES_DIRECTORY = "irondetect.procedures.directory";
	public static final String DEFAULT_VALUE_PROCEDURES_DIRECTORY = "procedures";
	
	public static final String KEY_GUI_ENABLED = "irondetect.gui.enabled";
	public static final boolean DEFAULT_VALUE_GUI_ENABLED = true;
	
	public static final String KEY_SUBSCRIBER_DEVICENAME = "irondetect.subscriber.devicename";
	public static final String DEFAULT_VALUE_SUBSCRIBER_DEVICENAME = "freeradius-pdp";
	
	public static final String KEY_PUBLISHER_ACTIONASIFMAPEVENT = "irondetect.publisher.actionasifmapevent";
	public static final boolean DEFAULT_VALUE_PUBLISHER_ACTIONASIFMAPEVENT = false;
	public static final String KEY_PUBLISHER_NOTIFY = "irondetect.publisher.notify";
	public static final boolean DEFAULT_VALUE_PUBLISHER_NOTIFY = false;
	public static final String KEY_PUBLISHER_POLICY_ENABLED = "irondetect.publisher.policy.enabled";
	public static final boolean DEFAULT_VALUE_PUBLISHER_POLICY_ENABLED = true;
	public static final String KEY_PUBLISHER_POLICY_NOFIREDRULES = "irondetect.publisher.policy.nofiredrules";
	public static final boolean DEFAULT_VALUE_PUBLISHER_POLICY_NOFIREDRULES = false;
	public static final String KEY_PUBLISHER_POLICY_DEVICENAME = "irondetect.publisher.policy.devicename";
	public static final String DEFAULT_VALUE_PUBLISHER_POLICY_DEVICENAME = "irondetect-policy";

	public static final String KEY_REST_URL = "irondetect.rest.url";
	public static final String DEFAULT_VALUE_REST_URL = "http://127.0.0.1:8001/";
	
}
