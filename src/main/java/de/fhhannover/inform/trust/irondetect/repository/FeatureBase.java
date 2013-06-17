package de.fhhannover.inform.trust.irondetect.repository;

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

import de.fhhannover.inform.trust.Category;
import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.model.Context;
import de.fhhannover.inform.trust.irondetect.util.Pair;
import de.fhhannover.inform.trust.irondetect.util.event.EventSender;

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
