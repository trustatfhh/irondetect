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
package de.hshannover.f4.trust.irondetect.livechecker.model;

import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.RULE;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.engine.Processor;
import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.livechecker.gui.ResultLoggerForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Rule;

public class RuleForLiveCheck extends Rule {

	private Logger mLogger = Logger.getLogger(RuleForLiveCheck.class);

	private ResultLogger mRlogger = ResultLoggerForLiveCheck.getInstance();

	public RuleForLiveCheck(Rule rule) {
		super.setCondition(rule.getCondition());
		super.setActions(rule.getActions());
		super.setId(rule.getId());
		super.setFeatureIds(rule.getFeatureId());
	}

	/**
	 * checks the condition, if it is true all actions will be performed
	 */
	@Override
	public void evaluate(String device) {

		mLogger.info("checking rule " + getId());
		super.condition.setParent(this);
		boolean result = super.condition.evaluate(device);
		mLogger.info("rule " + getId() + " result was " + result);
		mRlogger.reportResultsToLogger(device, super.id, RULE, result);
		// we only perform actions when in testing mode
		if (result && Processor.getInstance().isTesting()) {
			for (Action a : super.actions) {
				a.perform(device);
			}
		}
	}

}
