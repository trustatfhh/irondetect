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
package de.hshannover.f4.trust.irondetect.model;



import java.util.HashMap;
import java.util.Map;

/**
 * @author jvieweg
 *
 */
public class ContextParamType {

	private int typeId;
	private String name;

	public static final Map<Integer, String> MAP = new HashMap<Integer, String>(10);

	public static final String LOCATION_STRING = "LOCATION";
	public static final String DATETIME_STRING = "DATETIME";
	public static final String OTHERDEVICES_STRING = "OTHERDEVICES";
	public static final String SLIDING_STRING = "SLIDING";
	public static final String PERIOD_STRING = "PERIOD";
	public static final String ARBITRARY_STRING = "ARBITRARY";
	public static final String TRUSTLEVEL_STRING = "TRUSTLEVEL";
	public static final String LONGITUDE_STRING = "LONGITUDE";
	public static final String LATITUDE_STRING = "LATITUDE";
	public static final String GPS_DISTANCE_M_STRING = "GPS_DISTANCE_M";
	public static final String PHONENUMBER_STRING = "PHONENUMBER";

	static {
		MAP.put(new Integer(0), LOCATION_STRING);
		MAP.put(new Integer(1), DATETIME_STRING);
		MAP.put(new Integer(2), OTHERDEVICES_STRING);
		MAP.put(new Integer(3), SLIDING_STRING);
		MAP.put(new Integer(4), PERIOD_STRING);
		MAP.put(new Integer(5), ARBITRARY_STRING);
		MAP.put(new Integer(6), TRUSTLEVEL_STRING);
		MAP.put(new Integer(7), LONGITUDE_STRING);
		MAP.put(new Integer(8), LATITUDE_STRING);
		MAP.put(new Integer(9), GPS_DISTANCE_M_STRING);
		MAP.put(new Integer(10), PHONENUMBER_STRING);
	}

	public static final int LOCATION = 0;
	public static final int DATETIME = 1;
	public static final int OTHERDEVICES = 2;
	public static final int SLIDING = 3;
	public static final int PERIOD = 4;
	public static final int ARBITRARY = 5;
	public static final int TRUSTLEVEL = 6;
	public static final int LONGITUDE = 7;
	public static final int LATITUDE = 8;
	public static final int GPS_DISTANCE_M = 9;
	public static final int PHONENUMBER = 10;

	public int getTypeId() {
		return typeId;
	}
	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public ContextParamType(int typeId) {
		// check type
		if(typeId < ContextParamType.LOCATION || typeId > ContextParamType.PHONENUMBER){
			throw new RuntimeException("Invalid Context Parameter Type: " + typeId);
		}

		// set type and name
		setTypeId(typeId);
		setName(MAP.get(typeId));
	}

	public static ContextParamType valueOf(String paramType) {

		switch (paramType) {
			case LOCATION_STRING:
				return new ContextParamType(0);
			case DATETIME_STRING:
				return new ContextParamType(1);
			case OTHERDEVICES_STRING:
				return new ContextParamType(2);
			case SLIDING_STRING:
				return new ContextParamType(3);
			case PERIOD_STRING:
				return new ContextParamType(4);
			case ARBITRARY_STRING:
				return new ContextParamType(5);
			case TRUSTLEVEL_STRING:
				return new ContextParamType(6);
			case LONGITUDE_STRING:
				return new ContextParamType(7);
			case LATITUDE_STRING:
				return new ContextParamType(8);
			case GPS_DISTANCE_M_STRING:
				return new ContextParamType(9);
			case PHONENUMBER_STRING:
				return new ContextParamType(10);
			default:
				return null;
		}
	}
}
