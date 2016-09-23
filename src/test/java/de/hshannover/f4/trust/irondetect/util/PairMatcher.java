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
package de.hshannover.f4.trust.irondetect.util;

import org.hamcrest.Description;
import org.mockito.ArgumentMatcher;

/**
 * An {@link PairMatcher}.
 * 
 * @author Marcel Reichenbach
 */
public class PairMatcher<A, B> extends ArgumentMatcher<Pair<A, B>> {

	private String mErrorDescription;

	public PairMatcher() {
	}

	@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
	@Override
	public boolean matches(Object o) {
		if (o == null) {
			setErrorDescription("It was not a Pair-Object. It is 'null'.\n");
			return false;
		}

		if (o instanceof Pair) {
			Pair pair = (Pair) o;

			try {
				
				Pair<A, B> pairWithType = (Pair<A, B>) o;
			
			} catch (ClassCastException e){
				setErrorDescription("But the Pair-Object types was: firstElement Class = '"
						+ pair.getFirstElement().getClass().getSimpleName() + "' and secondElement Class = '"
						+ pair.getSecondElement().getClass().getSimpleName() + "'.\n");
				return false;
			}
			return true;
		} else {

			setErrorDescription("But it was not a Pair-Object. It is a '" + o.getClass().getSimpleName()
					+ "'-Object.\n");
			return false;
		}
	}

	private void setErrorDescription(String description) {
		mErrorDescription = description;
	}

	@Override
	public void describeTo(Description description) {
		super.describeTo(description);
		if (mErrorDescription != null) {
			description.appendText(" -> ERROR = " + mErrorDescription);
		} else {
			description.appendText(" -> result = OK\n");
		}
	}
}
