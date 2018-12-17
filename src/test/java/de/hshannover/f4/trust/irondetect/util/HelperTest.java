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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.util;



import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

/**
 * @author bahellma
 *
 */
public class HelperTest {

	@Test
	public void convertXsdStringToCalendar() {
		String xsdDateTime = "2003-05-31T13:20:05-05:00";
		Calendar c = new GregorianCalendar(2003, Calendar.MAY, 31, 13, 20, 5);
		c.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		Calendar result = Helper.getXsdStringAsCalendar(xsdDateTime);
		assertTrue(c.compareTo(result) == 0);
		assertFalse(c.before(result));
		assertFalse(c.after(result));
	}
	
	@Test
	public void convertCalendarToXsdString() {
		String s = "2003-05-31T13:20:05-05:00";
		Calendar c = new GregorianCalendar(2003, Calendar.MAY, 31, 13, 20, 5);
		c.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		String result = Helper.getCalendarAsXsdDateTime(c);
		assertTrue(s.equals(result));
		
	}

}
