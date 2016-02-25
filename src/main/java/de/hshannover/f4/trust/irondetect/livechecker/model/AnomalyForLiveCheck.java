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
/**
 *
 */
package de.hshannover.f4.trust.irondetect.livechecker.model;



import static de.hshannover.f4.trust.irondetect.gui.ResultObjectType.ANOMALY;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.engine.Processor;
import de.hshannover.f4.trust.irondetect.gui.ResultLogger;
import de.hshannover.f4.trust.irondetect.livechecker.gui.ResultLoggerForLiveCheck;
import de.hshannover.f4.trust.irondetect.livechecker.repository.FeatureBaseForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Anomaly;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.Hint;
import de.hshannover.f4.trust.irondetect.model.HintExpression;
import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * Adds context to the whole thing.
 *
 * @author Marcel Reichenbach
 *
 */
public class AnomalyForLiveCheck extends Anomaly {

	private Logger mLogger = Logger.getLogger(AnomalyForLiveCheck.class);

	private ResultLogger mRlogger = ResultLoggerForLiveCheck.getInstance();

	public AnomalyForLiveCheck(Anomaly anomaly) {

		List<Pair<HintExpression, BooleanOperator>> newHintSet = new ArrayList<Pair<HintExpression, BooleanOperator>>();
		for (Pair<HintExpression, BooleanOperator> pair : anomaly.getHintSet()) {
			newHintSet.add(new Pair<HintExpression, BooleanOperator>(new HintExpressionForLiveCheck(pair
					.getFirstElement()), pair.getSecondElement()));
		}

		super.setHintSet(newHintSet);
		super.setId(anomaly.getId());
		super.setContextSet(anomaly.getContextSet());
		super.setParent(anomaly.getParent());
	}

	/**
	 * @param device
	 * @param featureIds
	 * @return list which contains a pair consisting of featureId and the appropriate
	 */
	@Override
	protected synchronized List<Feature> getFeatureValues(String device, List<String> featureIds) {
		mLogger.trace("trying to get feature values, contextSet = " + super.contextSet);
		return FeatureBaseForLiveCheck.getInstance().getFeaturesByContext(device, featureIds, super.contextSet);
	}

	@Override
	public boolean evaluate(String device) {

		mLogger.info("----------------Evaluating anomaly "
				+ this.id + " for device " + device + "---------------------");

		/**
		 * Only create triggers in testing mode
		 */
		if (Processor.getInstance().isTesting()) {
			super.checkSlidingCtx(device);
		}

		long sTime = System.currentTimeMillis();

		for (Pair<HintExpression, BooleanOperator> p : super.hintSet) {
			Hint hint = p.getFirstElement().getHintValuePair().getFirstElement();
			List<String> featureIdSet = hint.getFeatureIds();
			List<Feature> fVals = getFeatureValues(device, featureIdSet);

			// logger.warn("#### " + fVals.size());

			if (fVals.isEmpty()) {
				mLogger.warn("--------------------Anomaly " + this.id +
						" no features found for Hint " + hint.getId() + " --------------------");
			}

			// set effective featureset in hint
			hint.setFeatureSet(fVals);
		}

		// evaluate all hints
		boolean result = super.evaluateHintSet(device);
		long eTime = System.currentTimeMillis();
		mLogger.info("--------------------Anomaly "
				+ this.id + " eval finished with " + result + "--------------------");
		mRlogger.reportResultsToLogger(device, id, ANOMALY, result);
		super.printTimedResult(getClass(), result, eTime - sTime);
		return result;

	}

}
