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
package de.hshannover.f4.trust.irondetect.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ironcommon.yaml.YamlReader;
import de.hshannover.f4.trust.irondetect.model.Category;
import de.hshannover.f4.trust.irondetect.model.ContextParamType;
import de.hshannover.f4.trust.irondetect.model.ContextParameter;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.FeatureType;
import de.hshannover.f4.trust.irondetect.util.Pair;

public class YamlImporter implements Importer {

	public static final String KEY_FEATURE_ID = "id";
	public static final String KEY_FEATURE_TYPE = "type";
	public static final String KEY_FEATURE_VALUE = "value";
	public static final String KEY_FEATURE_CATEGORY = "category";
	public static final String KEY_CONTEXT_PARAMETERS = "contextparameters";
	public static final String KEY_CONTEXTPARAMETER_VALUE = "value";
	public static final String KEY_CONTEXTPARAMETER_TYPE = "type";

	private Logger logger = Logger.getLogger(YamlImporter.class);

	@Override
	public List<Pair<String, Pair<Feature, Boolean>>> loadTrainingDatabases(
			String directoryPath) {
		logger.info("Trying to load training data from: "
				+ directoryPath);
		File directory = new File(directoryPath);

		if (directory.isDirectory()) {
			String[] files = directory.list();
			List<String> yamlFiles = new ArrayList<String>();
			for (String file : files) {
				if (file.endsWith(".yaml")) {
					yamlFiles.add(file);
				}
			}

			logger.info("Found " + yamlFiles.size() + " YAML files.");

			if (yamlFiles.size() > 0) {
				List<Pair<String, Pair<Feature, Boolean>>> features = new ArrayList<Pair<String, Pair<Feature, Boolean>>>();
				int count = 0;
				int countFeatures = 0;
				String device;
				String date;
				String time;

				for (String file : yamlFiles) {
					String[] split = file.split("_");
					if (split.length == 3) {
						device = split[0]; // filename:
						date = split[1];
						time = split[2];
					} else {
						device = file.replace(".yaml", "");
						time = "not set";
						date = "not set";
					}

					logger.info("Loading file '" + file + "'");

					Map<String, Object> map;
					try {
						map = YamlReader.loadMap(directory + "/" + file);
						// Properties properties = new Properties(directory +
						// "/"
						// + file);
						Set<String> keySetFeaturesForDevice;
						keySetFeaturesForDevice = map.keySet();
						logger.info("Adding " + keySetFeaturesForDevice.size()
								+ " Features from device '" + device
								+ "' to the FeatureBase.");

						if (!date.equalsIgnoreCase("not set")) {
							logger.info("file was saved at "
									+ time.replace(".yaml", "") + " on " + date
									+ ".");
						}

						Feature f;
						for (String featureKey : keySetFeaturesForDevice) {
							f = extractFeature(map, featureKey);

							if (hasNullOrEmptyValues(f) == false) {
								features.add(new Pair<String, Pair<Feature, Boolean>>(
										device, new Pair<Feature, Boolean>(f,
												false)));
								countFeatures++;
								logger.trace("Feature: " + f);
							} else {
								logger.trace("Feature: "
										+ f
										+ " was NOT inserted into FeatureBase; ID/Category was NULL or emphty");
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						count++;
					}
				}

				logger.info("Imported " + count + " YAML files.");
				logger.info("Imported " + countFeatures + " features.");

				return features;
			} else {
				logger.error("The directory configured IS NOT a exisiting directory.");
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private Feature extractFeature(Map<String, Object> map, String featureKey) {
		Map<String, Object> featureProperties = (Map<String, Object>) map
				.get(featureKey);

		String featureId = (String) featureProperties.get(KEY_FEATURE_ID);
		String featureValue = String.valueOf(featureProperties
				.get(KEY_FEATURE_VALUE));
		FeatureType featureType = new FeatureType(
				(int) featureProperties.get(KEY_FEATURE_TYPE));
		Category category = new Category(
				(String) featureProperties.get(KEY_FEATURE_CATEGORY));

		Set<ContextParameter> contextParameters = new HashSet<ContextParameter>();

		Map<String, Object> contextParametersProperties = (Map<String, Object>) featureProperties
				.get(KEY_CONTEXT_PARAMETERS);

		for (String key : contextParametersProperties.keySet()) {
			Map<String, Object> contextParam = (Map<String, Object>) contextParametersProperties
					.get(key);
			ContextParamType type = new ContextParamType(
					(int) contextParam.get(KEY_CONTEXTPARAMETER_TYPE));
			String value = (String) contextParam
					.get(KEY_CONTEXTPARAMETER_VALUE);
			ContextParameter c = new ContextParameter(type, value);
			contextParameters.add(c);
		}

		Feature result = new Feature(featureId, featureValue, featureType,
				category, contextParameters);
		logger.debug("Feature:" + result);
		return result;
	}

	private boolean hasNullOrEmptyValues(Feature f) {
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
