/**
 * 
 */
package de.fhhannover.inform.trust.irondetect.model;

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

import de.fhhannover.inform.trust.irondetect.util.ComparisonOperator;

/**
 * @author jvieweg
 * @author ib
 *
 */
public abstract class Evaluable {
	
	private static Logger logger = Logger.getLogger(Evaluable.class);
	
	private static double precision = 0.00001d;
	
	protected String id;

	public abstract boolean evaluate(String device);
	
	protected boolean evaluateCompOpOnNumber(ComparisonOperator operator, String actual, String desired) {		
		return evaluateCompOpOnNumber(operator, Double.parseDouble(actual), Double.parseDouble(desired));
	}
	
	protected boolean evaluateCompOpOnNumber(ComparisonOperator operator, double actual, double desired) {		
		switch (operator) {
		case EQ:
			return Math.abs(desired-actual) < precision ;
		case NE:
			return Math.abs(desired-actual) >= precision;
		case ST:
			return actual < desired;
		case GT:
			return actual > desired;
		case SE:
			return actual <= desired;
		case GE:
			return actual >= desired;
		default:
			return false;
		}
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	protected boolean evaluateCompOpOnString(ComparisonOperator operator, String actual, String desired) {		
		switch (operator) {
		case EQ:
			return actual.equals(desired);
		case NE:
			return !(actual.equals(desired));
		case ST:
			return actual.length() < desired.length();
		case GT:
			return actual.length() > desired.length();
		case SE:
			return actual.length() <= desired.length();
		case GE:
			return actual.length() >= desired.length();
		default:
			return false;
		}
	}
	
	
	
	protected void printTimedResult(Class<?> type, boolean result, long time) {
		logger.debug("finished evaluation of " +  type.getSimpleName() + " " + this.id + " with result " + result + " in " + time + "msecs");
	}
	
}
