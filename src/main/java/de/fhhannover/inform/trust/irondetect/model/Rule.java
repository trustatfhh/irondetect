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

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.engine.Processor;
import de.fhhannover.inform.trust.irondetect.gui.ResultLogger;
import de.fhhannover.inform.trust.irondetect.gui.ResultLoggerImpl;

/**
 * @author jvieweg
 *
 */
public class Rule {
	
	private Logger logger = Logger.getLogger(this.getClass());
        private ResultLogger rlogger = ResultLoggerImpl.getInstance();
	
	private Condition condition;
	private List<Action> actions;
	private String id;
	private Set<String> featureIds;

	
	/**
	 * checks the condition, if it is true all actions will be performed
	 */
	public void evaluate(String device) {
		
		logger.info("checking rule " + getId());
		this.condition.setParent(this);
		boolean result = this.condition.evaluate(device);
		logger.info("rule " + getId() + " result was " + result);
		rlogger.reportResultsToLogger(device, this.id, this.getClass().getSimpleName(), result);
		// we only perform actions when in testing mode
		if(result && Processor.getInstance().isTesting()) {
			for (Action a : actions) {
				a.perform(device);
			}
		}
	}
	
	/**
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	/**
	 * @return the actions
	 */
	public List<Action> getActions() {
		return actions;
	}

	/**
	 * @param actions the actions to set
	 */
	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder actionStr = new StringBuilder("actions=");
		if(actions != null) {
			for (Action a : actions) {
				actionStr.append(a.toString());
			}
		}
		return "Rule [condition=" + (condition != null ? condition : " ") + ", " + actionStr.toString()
				+ ", " + super.toString() + "]";
	}

	/**
	 * 
	 * @param featureIds
	 * @return true if one of the given featureIds is used by this rule
	 */
	public boolean contains(Set<String> changedfeatureIds) {
		assert this.featureIds != null : "Feature set saved in rule shouldn't be null!";
		for (String id : this.featureIds) {
			if (id.startsWith(Policy.COUNT_KEY)) {
				id = id.substring(1);
			}
			if (changedfeatureIds.contains(id)) {
				return true;
			}
		}
		return false;
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

	/**
	 * @param featureIds the featureIds to set
	 */
	public void setFeatureIds(Set<String> featureIds) {
		this.featureIds = featureIds;
	}

}
