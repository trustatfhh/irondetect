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
 * This file is part of irondetect, version 0.0.9, 
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
package de.hshannover.f4.trust.irondetect.util;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.irondetect.gui.ResultObject;

/**
 * An {@link FeatureCountPairMatcher}.
 * 
 * @author Marcel Reichenbach
 */
public class FeatureCountPairMatcher extends PairMatcher<ResultObject, Document> {

	private String mErrorDescription;

	private ArgumentMatcher<Document> policyActionMatcher;

	public FeatureCountPairMatcher(int revFeatureCount) {
		policyActionMatcher = new PolicyActionMatcher(revFeatureCount);
	}

	/**
	 * The method matches() needs a one lastFeatureCount because this method calls multiple.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(Object o) {
		if (!super.matches(o)) {
			return false;
		}

		Pair<ResultObject, Document> pair = (Pair<ResultObject, Document>) o;
		Document document = pair.getSecondElement();

		if (!policyActionMatcher.matches(document)) {
			return false;
		}
		
		return true;
	}


	@SuppressWarnings("unused")
	private void setErrorDescription(String description) {
		mErrorDescription = description;
	}

	@Override
	public void describeTo(Description description) {
		super.describeTo(description);

		policyActionMatcher.describeTo(description);

		if (mErrorDescription != null) {
			description.appendText(" -> ERROR = " + mErrorDescription);
		}
	}
}
