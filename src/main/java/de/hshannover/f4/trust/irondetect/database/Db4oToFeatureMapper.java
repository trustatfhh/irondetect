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
package de.hshannover.f4.trust.irondetect.database;



import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author bahellma
 * 
 */
public class Db4oToFeatureMapper {

	private static Logger logger = Logger.getLogger(Db4oToFeatureMapper.class);

	/**
	 * @return 
	 * @return
	 * 
	 */
	public static List<Pair<String,Pair<Feature,Boolean>>> loadTrainingDatabases() {
		logger.info("Trying to load training databases from: "
				+ Configuration.db4oDatabase());
		File directory = new File(Configuration.db4oDatabase());

		if (directory.isDirectory()) {
			String[] files = directory.list();
			List<String> db4oFiles = new ArrayList<String>();
			for (String file : files) {
				if (file.endsWith(".db4o")) {
					db4oFiles.add(file);
				}
			}

			logger.trace("Found " + db4oFiles.size() + " db4o files.");

			if (db4oFiles.size() > 0) {
				List<Pair<String, Pair<Feature, Boolean>>> features = new ArrayList<Pair<String, Pair<Feature, Boolean>>>();
				int count = 0;
				int countFeatures = 0;
				ObjectContainer db;
				String device;
				String date;
				String time;
				ObjectSet<Feature> featuresForDevice;

				for (String file : db4oFiles) {
					String[] split = file.split("_");
					if (split.length == 3) {
						device = split[0]; // filename:
						date = split[1];
						time = split[2];
					} else {
						device = file.replace(".db4o", "");
						time = "not set";
						date = "not set";
					}
					
					logger.info("Loading file '" + file + "'");
					db = Db4oEmbedded.openFile(directory + "/" + file);

					try {
						featuresForDevice = db.query(Feature.class);

						logger.info("Adding " + featuresForDevice.size()
								+ " Features from device '" + device
								+ "' to the FeatureBase.");
						
						if (!date.equalsIgnoreCase("not set")) {
							logger.info("file was saved at "
									+ time.replace(".db4o", "") + " on " + date
									+ ".");
						}
						
						for (Feature f : featuresForDevice) {
							if (hasNullOrEmptyValues(f) == false) {
								features.add(new Pair<String, Pair<Feature, Boolean>>(
										device,
										new Pair<Feature, Boolean>(f, false)));
								countFeatures++;
								logger.trace("Feature: " + f);								
							} else {
								logger.trace("Feature: " + f + " was NOT inserted into FeatureBase; ID/Category was NULL or emphty");
							}
						}
					} finally {
						db.close();
						count++;
					}
				}

				logger.info("Imported " + count
						+ " db40 files.");
				logger.info("Imported " + countFeatures + " features.");
				
				return features;
			} else {
				logger.error("The directory configured IS NOT a exisiting directory.");
			}
		}
		
		return null;
	}

	private static boolean hasNullOrEmptyValues(Feature f) {
		if (f == null) {
			return true;
		}
		
		if (f.getCategory() == null) { 
			return true;
		} else if (f.getCategory().getId() == null) {
			return true;
		} else if (f.getCategory().getId().isEmpty()) {
			return true;
		}
		
		if (f.getId() == null) {
			return true;
		} else if (f.getId().isEmpty()) {
			return true;
		}
		
		if (f.getQualifiedId() == null) {
			return true;
		} else if (f.getQualifiedId().isEmpty()) {
			return true;
		}
		
		return false;
	}
}
