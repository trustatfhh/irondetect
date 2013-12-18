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
 * This file is part of irondetect, version 0.0.5, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2010 - 2013 Trust@HsH
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



import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

import de.hshannover.f4.trust.Category;
import de.hshannover.f4.trust.ContextParamType;
import de.hshannover.f4.trust.ContextParameter;
import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.FeatureType;

/**
 * @author bahellma
 * 
 */
public class Db4oTest {

	private static Logger logger = Logger.getLogger(Db4oTest.class);

	private static final int COUNT_FEATURES = 5;

	private static final int COUNT_DEVICES = 3;

	private static final String FILENAME_PREFIX = "device";
	private static final String FILENAME_POSTFIX = "_000000_20120101.db4o";

	public static void createTestDb4oFiles() {
		logger.info(Db4oTest.class.getSimpleName() + " has started");

		ObjectContainer db;
		String filename;
		
		for (int i = 0; i < COUNT_DEVICES; i++) {
			filename = Configuration.db4oDatabase() + "/" + FILENAME_PREFIX + i + FILENAME_POSTFIX;
			logger.trace("Deleting existing file '" + filename + "'");
			new File(filename).delete();

			logger.info("Creating/opening database '" + filename + "'");
			db = Db4oEmbedded.openFile(filename);

			try {
				logger.info("Creating features for device " + i);
				List<Feature> features = createFeatures();

				logger.info("Storing features to db4o");
				for (Feature f : features) {
					db.store(f);
				}
			} finally {
				db.close();
				logger.info("Closing db4o-file '" + FILENAME_POSTFIX + "'");
			}
		}
	}

	/**
	 * @return
	 */
	private static List<Feature> createFeatures() {
		List<Feature> result = new ArrayList<Feature>();

		Feature f;
		for (int i = 0; i < COUNT_FEATURES; i++) {
			logger.trace("Creating feature " + i);
			Set<ContextParameter> contextParams = new HashSet<ContextParameter>();
			contextParams.add(new ContextParameter(new ContextParamType(ContextParamType.DATETIME),
					Helper.getCalendarAsXsdDateTime(new GregorianCalendar())));
			f = new Feature("id" + i, Integer.toString(i),
					new FeatureType(FeatureType.QUALIFIED),
					new Category("category" + i), contextParams);
			result.add(f);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

}
