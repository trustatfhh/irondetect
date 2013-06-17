package de.fhhannover.inform.trust;

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
	
	static {
		MAP.put(new Integer(0), "LOCATION");
		MAP.put(new Integer(1), "DATETIME");
		MAP.put(new Integer(2), "OTHERDEVICES");
		MAP.put(new Integer(3), "SLIDING");
		MAP.put(new Integer(4), "PERIOD");
		MAP.put(new Integer(5), "ARBITRARY");
		MAP.put(new Integer(6), "TRUSTLEVEL");
		MAP.put(new Integer(7), "LONGITUDE");
		MAP.put(new Integer(8), "LATITUDE");
		MAP.put(new Integer(9), "GPS_DISTANCE_M");
		MAP.put(new Integer(10), "PHONENUMBER");
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

}
