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
package de.hshannover.f4.trust.irondetect.model;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.policy.parser.treeObjects.SymbolTable;

/**
 * @author jvieweg
 *
 */
public class Policy implements PolicyData {

	public static String COUNT_KEY = "#";
	public static String GET_KEY = "@";
	public static String SUBCAT_KEY = ".";
	public static String SCOPE_KEY = "!";

	private Logger logger = Logger.getLogger(Policy.class);

	private List<Rule> ruleSet;
	private SymbolTable symbols;
	private String id;
	private Map<String, Boolean> firstRun;

	public Policy() {
		this.firstRun = new HashMap<String, Boolean>();
	}

	/**
	 * @param symbols
	 */
	public void setSymbolTable(SymbolTable symbols) {
		this.symbols = symbols;
	}

	public SymbolTable getSymbolTable() {
		return symbols;
	}

	/**
	 * @return the ruleSet
	 */
	public List<Rule> getRuleSet() {
		return ruleSet;
	}

	/**
	 * @param ruleSet
	 *            the ruleSet to set
	 */
	public void setRuleSet(List<Rule> ruleSet) {
		this.ruleSet = ruleSet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder rStr = new StringBuilder("ruleSet=");
		for (Rule r : ruleSet) {
			rStr.append(r.toString());
		}
		return "Policy [" + rStr.toString() + ", " + super.toString() + "]";
	}

	/**
	 * Trigger check of policy.
	 */
	public void check(String device, Set<String> featureIds) {
		logger.trace("Checking policy for device " + device);
		if (!this.firstRun.containsKey(device) || this.firstRun.get(device)) {
			for (Rule r : ruleSet) {
				r.evaluate(device);
			}
			this.firstRun.put(device, Boolean.FALSE);
		} else {
			for (Rule r : ruleSet) {
				if (r.contains(featureIds)) {
					r.evaluate(device);
				}
			}
		}
	}

	/**
	 * Train policy.
	 *
	 * FIXME: is this needed or can we use check?
	 */
	public void train(String device) {
		logger.trace("Training policy for device " + device);
		for (Rule r : ruleSet) {
			r.evaluate(device);
		}
		//		this.firstRun = false; this causes joerg to fail
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
		if (!(other instanceof Policy)) {
			return false;
		}

		Policy otherItem = (Policy) other;

		if (!getId().equals(otherItem.getId())) {
			return false;
		}

		return true;
	}

	public Map<String, Boolean> getFirstRunMap() {
		return firstRun;
	}

	public void setFirstRunMap(Map<String, Boolean> newfirstRunMap) {
		firstRun = newfirstRunMap;
	}
}
