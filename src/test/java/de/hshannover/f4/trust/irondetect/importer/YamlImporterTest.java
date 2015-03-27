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
package de.hshannover.f4.trust.irondetect.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.hshannover.f4.trust.ironcommon.yaml.YamlWriter;
import de.hshannover.f4.trust.irondetect.model.Category;
import de.hshannover.f4.trust.irondetect.model.ContextParamType;
import de.hshannover.f4.trust.irondetect.model.ContextParameter;
import de.hshannover.f4.trust.irondetect.model.Feature;
import de.hshannover.f4.trust.irondetect.model.FeatureType;
import de.hshannover.f4.trust.irondetect.util.Configuration;
import de.hshannover.f4.trust.irondetect.util.Helper;

public class YamlImporterTest {

	private Logger logger = Logger.getLogger(YamlImporterTest.class);

	private final int COUNT_FEATURES = 5;
	private final int COUNT_DEVICES = 3;

	private final String FILENAME_PREFIX = "device";
	private final String FILENAME_POSTFIX = "_000000_20150327.yaml";

	@Before
	public void setUp() {
		try {
			createTestFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		YamlImporter importer = new YamlImporter();
		importer.loadTrainingDatabases(Configuration.yamlTrainingData());
	}

	private List<Feature> createFeatures() {
		List<Feature> result = new ArrayList<Feature>();

		Feature f;
		for (int i = 0; i < COUNT_FEATURES; i++) {
			logger.trace("Creating feature " + i);
			Set<ContextParameter> contextParams = new HashSet<ContextParameter>();
			contextParams.add(new ContextParameter(new ContextParamType(
					ContextParamType.DATETIME), Helper
					.getCalendarAsXsdDateTime(new GregorianCalendar())));
			f = new Feature("id" + i, Integer.toString(i), new FeatureType(
					FeatureType.QUALIFIED), new Category("category" + i),
					contextParams);
			result.add(f);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private void createTestFiles() throws IOException {
		logger.info(YamlImporterTest.class.getSimpleName() + " has started");

		String filename;
		String directory = Configuration.yamlTrainingData();

		new File(Configuration.yamlTrainingData()).mkdir();

		for (int i = 0; i < COUNT_DEVICES; i++) {
			filename = directory + "/" + FILENAME_PREFIX + i + FILENAME_POSTFIX;
			logger.trace("Deleting existing file '" + filename + "'");
			new File(filename).delete();

			Map<String, Object> data = new HashMap<>();

			try {
				logger.info("Creating features for device " + i);
				List<Feature> features = createFeatures();

				logger.info("Storing features to YAML file");
				int featureNumber = 1;
				for (Feature f : features) {
					addFeatureToMap(f, featureNumber, data);
					featureNumber++;
				}
			} finally {
				logger.info("Writing to yaml file '" + filename + "'");
				YamlWriter.persist(filename, data);
			}
		}
	}

	private void addFeatureToMap(Feature f, int featureNumber,
			Map<String, Object> data) {
		Map<String, Object> featureData = new HashMap<>();
		featureData.put(YamlImporter.KEY_FEATURE_ID, f.getId());
		featureData.put(YamlImporter.KEY_FEATURE_VALUE, f.getValue());
		featureData.put(YamlImporter.KEY_FEATURE_TYPE, f.getType().getTypeId());
		featureData.put(YamlImporter.KEY_FEATURE_CATEGORY, f.getCategory()
				.getId());

		Map<String, Object> contextParameterData = new HashMap<>();
		int ctxParamNumber = 1;
		for (ContextParameter contextParameter : f.getContextParameters()) {
			Map<String, Object> contextParameterEntryData = new HashMap<>();
			contextParameterEntryData.put(
					YamlImporter.KEY_CONTEXTPARAMETER_TYPE, contextParameter
							.getType().getTypeId());
			contextParameterEntryData.put(
					YamlImporter.KEY_CONTEXTPARAMETER_VALUE,
					contextParameter.getValue());
			contextParameterData.put("param" + ctxParamNumber,
					contextParameterEntryData);
			ctxParamNumber++;
		}
		featureData.put(YamlImporter.KEY_CONTEXT_PARAMETERS,
				contextParameterData);

		data.put("feature" + featureNumber, featureData);
	}

}
