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



import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.ifmap.ActionToIfmapMapper;
import de.hshannover.f4.trust.irondetect.livechecker.repository.FeatureBaseForLiveCheck;
import de.hshannover.f4.trust.irondetect.model.Action;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.Policy;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author Marcel Reichenbach
 *
 */
public class ActionForLiveCheck extends Action {

	private Logger mLogger = Logger.getLogger(ActionForLiveCheck.class);

	public ActionForLiveCheck(Action action) {

	}

	@Override
	public void perform(String device){
		assert super.keyValuePairs != null : "action shouldn't be null!";

		mLogger.info("PERFORMING ACTION (" + this.toString() + ")");

		ArrayList<Pair<String, String>> mappedKeyValuePairs = new ArrayList<Pair<String, String>>();

		for(Pair<String, String> p : this.keyValuePairs) {
			String key = p.getFirstElement();
			String value = p.getSecondElement();
			if(value.startsWith(Policy.GET_KEY)) {
				String valueKey = value.substring(1);
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(valueKey);
				List<Feature> features = FeatureBaseForLiveCheck.getInstance().getFeaturesByContext(device, tmp, null);
				for(Feature f : features) {
					Pair<String, String> remapped = new Pair<String, String>(key, f.getValue());
					mappedKeyValuePairs.add(remapped);
				}
			} else {
				mappedKeyValuePairs.add(p);
			}
		}

		ActionToIfmapMapper.getInstance().addNewAction(device, mappedKeyValuePairs);
	}
}