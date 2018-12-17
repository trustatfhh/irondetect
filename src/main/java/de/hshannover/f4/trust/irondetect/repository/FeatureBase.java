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
package de.hshannover.f4.trust.irondetect.repository;



import java.util.List;

import de.hshannover.f4.trust.irondetect.model.Category;
import de.hshannover.f4.trust.irondetect.model.Context;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.event.EventSender;

/**
 * Interface for the FeatureBase.
 * 
 * @author bahellma
 * 
 */
public interface FeatureBase extends EventSender {

	/**
	 * Insert new {@link Feature}s and {@link Category}s into this
	 * {@link FeatureBase}. Checks if {@link Feature}s/{@link Category}s were
	 * present (and updates them) or not (insert them).
	 * 
	 * @param features
	 *            a {@link List} of {@link Pair}s, containing a {@link String}
	 *            for the device, and another {@link Pair} with the
	 *            {@link Feature} and a {@link Boolean} that marks deletion
	 *            (true == was deleted).
	 * @param isTraining
	 * 			  true, if the new Features are training data 
	 */
	public void addNewFeatures(
			List<Pair<String, Pair<Feature, Boolean>>> features, boolean isTraining);

	/**
	 * This method returns a {@link List} of all {@link Feature}s that were
	 * stored for a given device and match to a given {@link List} of
	 * {@link Feature} IDs (unqualified!) and a given {@link Context}.
	 * 
	 * Important note: IF a context is given, all {@link Feature}s that were not
	 * deleted are returned IF NO context is given, only the LATEST
	 * {@link Feature} is returned, if it is NOT marked as deleted. In that
	 * case, an EMPTY List is returned.
	 * 
	 * @param device
	 *            the device that will be searched for {@link Feature}s
	 * @param ids
	 *            the {@link Feature} IDs (unqualified!) to search for on the
	 *            given device
	 * @param contextSet
	 *            the {@link Context} all {@link Feature}s have to match
	 * @return a {@link List} of all {@link Feature}s that match to the given
	 *         device, the {@link Feature} IDs and the {@link Context}.
	 */
	public List<Feature> getFeaturesByContext(String device, List<String> ids,
			List<Context> contextSet);

	/**
	 * Deletes all information in the {@link FeatureBase}.
	 */
	public void resetFeatureBase();
}
