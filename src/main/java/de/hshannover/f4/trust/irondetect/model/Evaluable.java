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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.model;



import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.util.ComparisonOperator;

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

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + getId().hashCode();
		return result;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other == this) {
			return true;
		}
		if (!(other instanceof Evaluable)) {
			return false;
		}

		Evaluable otherItem = (Evaluable) other;

		if (!getId().equals(otherItem.getId())) {
			return false;
		}

		return true;
	}
}
