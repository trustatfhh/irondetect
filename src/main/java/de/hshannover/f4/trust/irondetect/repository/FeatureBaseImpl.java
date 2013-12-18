/*
 * #%L
 * =====================================================
 * 
 *   |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *     | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *     | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *     |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                              \____/
 * 
 *  =====================================================
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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.repository;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ContextParamType;
import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.irondetect.model.Context;
import de.hshannover.f4.trust.irondetect.util.Helper;
import de.hshannover.f4.trust.irondetect.util.Pair;
import de.hshannover.f4.trust.irondetect.util.event.Event;
import de.hshannover.f4.trust.irondetect.util.event.EventReceiver;
import de.hshannover.f4.trust.irondetect.util.event.FeatureBaseUpdateEvent;
import de.hshannover.f4.trust.irondetect.util.event.TrainingData;
import de.hshannover.f4.trust.irondetect.util.event.TrainingDataLoadedEvent;

/**
 * @author bahellma
 * 
 */
public class FeatureBaseImpl implements FeatureBase {

	private static Logger logger = Logger.getLogger(FeatureBaseImpl.class);

	private Map<String, List<FeatureHistory>> featuresForDevice;

	private List<EventReceiver> eventReceiver;

	private static FeatureBaseImpl instance;
	
	private FeatureBaseImpl() {
		this.featuresForDevice = new HashMap<String, List<FeatureHistory>>();
		this.eventReceiver = new ArrayList<EventReceiver>();
	}
	
	public static synchronized FeatureBaseImpl getInstance() {
		if (instance == null) {
			instance = new FeatureBaseImpl();
		}
		
		return instance;
	}

	
	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.irondetect.repository.FeatureBaseInterface#addNewFeatures(java.util.ArrayList)
	 */
	@Override
	public synchronized void addNewFeatures(
			List<Pair<String, Pair<Feature, Boolean>>> features, boolean isTraining) {
		if (features != null && features.size() > 0) {
			logger.trace("Incoming features: " + features.size());
			long startTime = System.currentTimeMillis();
	
			String device = null;
			Boolean wasDeleted = null;
			Calendar currentFeatureDateTime;
	
			Feature currentFeature = null;
			Pair<Feature, Boolean> currentPair = null;
			FeatureHistory fh = null;
			List<FeatureHistory> featureHistoryForCurrentDevice = null;
	
			Map<String, Integer> numberOfDeletedFeatures = new HashMap<String, Integer>();
			Map<String, Integer> numberOfNewFeatures = new HashMap<String, Integer>();
			Map<String, Integer> numberOfUpdatedFeatures = new HashMap<String, Integer>();
			
			Integer currentNumberOfDeletedFeatures = 0;
			Integer currentNumberOfNewFeatures = 0;
			Integer currentNumberOfUpdatedFeatures = 0;
	
			boolean success = false;
			
			Map<String, Set<String>> changesDuringFeatureBaseUpdate = new HashMap<String, Set<String>>();
			Set<String> currentChangesForDevice = null;
			
			Map<String, TrainingData> trainingData = new HashMap<String, TrainingData>();
			TrainingData currentTrainingData = null;
		
			for (Pair<String, Pair<Feature, Boolean>> devicePair : features) {
				device = devicePair.getFirstElement();
				currentPair = devicePair.getSecondElement();
				currentFeature = (Feature) currentPair.getFirstElement();
				wasDeleted = currentPair.getSecondElement();
				currentFeatureDateTime = Helper.getXsdStringAsCalendar(currentFeature.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME)).getValue());
	
				logger.trace("Working on <Feature, Boolean>-pairs for device: "
						+ device);
	
				success = false;
	
				if ((featureHistoryForCurrentDevice = this.featuresForDevice
						.get(device)) == null) {
					featureHistoryForCurrentDevice = new Vector<FeatureHistory>();
					this.featuresForDevice.put(device,
							featureHistoryForCurrentDevice);
					logger.trace("List of feature histories for current device was generated.");
				}
	
				if ((currentChangesForDevice = changesDuringFeatureBaseUpdate.get(device)) == null) {
					currentChangesForDevice = new HashSet<String>();
					changesDuringFeatureBaseUpdate.put(device, currentChangesForDevice);
				}
				
				if ((currentTrainingData = trainingData.get(device)) == null) {
					currentTrainingData = new TrainingData();
					trainingData.put(device, currentTrainingData);
				}
				
				if ((currentNumberOfDeletedFeatures = numberOfDeletedFeatures.get(device)) == null) {
					currentNumberOfDeletedFeatures = 0;
					numberOfDeletedFeatures.put(device, currentNumberOfDeletedFeatures);
				}
				
				if ((currentNumberOfNewFeatures = numberOfNewFeatures.get(device)) == null) {
					currentNumberOfNewFeatures = 0;
					numberOfNewFeatures.put(device, currentNumberOfNewFeatures);
				}
				
				if ((currentNumberOfUpdatedFeatures = numberOfUpdatedFeatures.get(device)) == null) {
					currentNumberOfUpdatedFeatures = 0;
					numberOfUpdatedFeatures.put(device, currentNumberOfUpdatedFeatures);
				}
	
				/**
				 * Check if time of current Feature is before/after the saved value for the specific training data for the Feature's device.
				 */
				currentTrainingData.setStartTime(currentFeatureDateTime);
				currentTrainingData.setEndTime(currentFeatureDateTime);
				
				/**
				 * Handle deleted features.
				 */
				if (wasDeleted == true) {
					fh = getFeatureHistoryByQualifiedFeatureId(
							featureHistoryForCurrentDevice, currentFeature.getQualifiedId());
					if (fh != null) {
						fh.addFeature(currentPair);
						currentChangesForDevice.add(currentFeature.getQualifiedIdWithoutInstance());
						currentNumberOfDeletedFeatures++;
					} else {
						logger.error("Feature '" + currentFeature.getQualifiedId()
								+ "' for device '" + device
								+ "' could not be added.");
					}
	
				} else {
					/**
					 * Handle new features
					 */
					if (!containsQualifiedFeatureId(
							featureHistoryForCurrentDevice, currentFeature)) {
						fh = new FeatureHistory();
						fh.addFeature(currentPair);
						success = featureHistoryForCurrentDevice.add(fh);
						if (success) {
							currentChangesForDevice.add(currentFeature.getQualifiedIdWithoutInstance());
							currentNumberOfNewFeatures++;
						} else {
							logger.error("Feature '" + currentFeature.getQualifiedId()
									+ "' for device '" + device
									+ "' could not be added.");
						}
	
						/**
						 * Handle updates of features.
						 */
					} else {
						success = false;
						fh = getFeatureHistoryByQualifiedFeatureId(
								featureHistoryForCurrentDevice, currentFeature.getQualifiedId());
						if (fh != null) {
							fh.addFeature(currentPair);
							currentChangesForDevice.add(currentFeature.getQualifiedIdWithoutInstance());
							currentNumberOfUpdatedFeatures++;
						} else {
							logger.error("Feature '" + currentFeature.getQualifiedId()
									+ "' for device '" + device
									+ "' could not be updated.");
						}
					}
				}
				
				numberOfDeletedFeatures.put(device, currentNumberOfDeletedFeatures);
				numberOfNewFeatures.put(device, currentNumberOfNewFeatures);
				numberOfUpdatedFeatures.put(device, currentNumberOfUpdatedFeatures);
			}
			
			/**
			 * Remove empty device entries.
			 */
			for (String d : changesDuringFeatureBaseUpdate.keySet()) {
				if (changesDuringFeatureBaseUpdate.get(d).size() == 0) {				
					changesDuringFeatureBaseUpdate.remove(device);
					trainingData.remove(device);
				}
			}
			
			Set<String> changedFeaturesForDevice;
			TrainingData t;
			for (String d : changesDuringFeatureBaseUpdate.keySet()) {
				logger.info("Features for device '" + d + "' (deleted/new/updated): " + numberOfDeletedFeatures.get(d) + "/" + numberOfNewFeatures.get(d) + "/" + numberOfUpdatedFeatures.get(d));
				
				if (isTraining) {
					changedFeaturesForDevice = changesDuringFeatureBaseUpdate.get(d);
					t = trainingData.get(d);
					t.setFeatureIds(changedFeaturesForDevice);
					logger.info("Time of first feature for device '" + d + "': " + Helper.getCalendarAsXsdDateTime(t.getStartTime()));
					logger.info("Time of last feature for device '" + d + "': " + Helper.getCalendarAsXsdDateTime(t.getEndTime()));
				}
			}
			
			Event e;
			if (isTraining) {
				e = new TrainingDataLoadedEvent();
				((TrainingDataLoadedEvent) e).setPayload(trainingData);
			} else {
				/**
				 * Sends a Map containing Lists of FeatureIds that changed during the FeatureBaseUpdate for each device.
				 */
				e = new FeatureBaseUpdateEvent();
				((FeatureBaseUpdateEvent) e).setPayload(changesDuringFeatureBaseUpdate);
			}

			/**
			 * Inform all registered EventReceiver.
			 */
			for (EventReceiver er : this.eventReceiver) {
				er.submitNewEvent(e);
			}
			
			long time = System.currentTimeMillis() - startTime;
			logger.info("Adding " + features.size() + " items took " + time + " milliseconds.");
		} else {
			logger.trace("Incoming feature list was empty; not adding anything to FeatureBase.");
		}
	}

	/**
	 * @param device
	 * @return
	 */
	public List<FeatureHistory> getAllFeatureHistoriesForDevice(String device) {
		return this.featuresForDevice.get(device);
	}

	/**
	 * @param device
	 * @return
	 */
	public List<Pair<Feature, Boolean>> getAllCurrentFeaturesForDevice(
			String device) {
		List<FeatureHistory> featureHistories = this.featuresForDevice
				.get(device);
		Pair<Feature, Boolean> pair;
		List<Pair<Feature, Boolean>> result = new ArrayList<Pair<Feature, Boolean>>();
		for (FeatureHistory fh : featureHistories) {
			pair = fh.getLatestFeature();
			result.add(pair);
		}

		return result;
	}

	/**
	 * @param feature
	 * @param device
	 * @return
	 */
	public Pair<Feature, Boolean> getCurrentSpecificFeatureForDevice(
			Feature feature, String device) {
		return getCurrentSpecificFeatureForDevice(feature.getId(), device);
	}

	/**
	 * @param featureId
	 * @param device
	 * @return
	 */
	public Pair<Feature, Boolean> getCurrentSpecificFeatureForDevice(
			String featureId, String device) {
		return getFeatureHistoryByQualifiedFeatureId(
				this.featuresForDevice.get(device), featureId).getLatestFeature();
	}

	/**
	 * @return
	 */
	public List<String> getAllDevices() {
		return new ArrayList<String>(featuresForDevice.keySet());
	}

	/**
	 * @param device
	 * @return
	 */
	public List<Feature> getAllCurrentNotDeletedFeaturesForDevice(String device) {
		List<FeatureHistory> featureHistories = this.featuresForDevice
				.get(device);
		Pair<Feature, Boolean> pair;
		List<Feature> result = new ArrayList<Feature>();
		for (FeatureHistory fh : featureHistories) {
			pair = fh.getLatestFeature();
			if (pair.getSecondElement() == false) {
				result.add(pair.getFirstElement());
			}
		}

		return result;
	}
	
	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.irondetect.repository.FeatureBaseInterface#getFeaturesByContext(de.hshannover.f4.trust.irondetect.model.ContextParameter)
	 */
	@Override
	public synchronized List<Feature> getFeaturesByContext(String device, List<String> ids, List<Context> contextSet) {
		List<Feature> result = new Vector<Feature>();
		
		List<FeatureHistory> featureHistoriesForDevice = this.featuresForDevice.get(device);
		String lowercaseFeatureId;
		for (String featureId : ids) {
			lowercaseFeatureId = featureId.toLowerCase();
			for (FeatureHistory fh : featureHistoriesForDevice) {
				if (fh.getQualifiedFeatureHistoryIdWithoutInstance().contains(lowercaseFeatureId)) {
					if (contextSet != null && contextSet.size() > 0) {
						for (Context context : contextSet) {
							result.addAll(getFeaturesForContext(fh, context));
						}
					} else {
						result.addAll(getFeaturesForContext(fh));
					}
				}
			}
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.irondetect.repository.FeatureBase#addEventReceiver(de.hshannover.f4.trust.irondetect.util.EventReceiver)
	 */
	@Override
	public void addEventReceiver(EventReceiver er) {
		if (!this.eventReceiver.contains(er)) {
			this.eventReceiver.add(er);
		}
	}

	@Override
	public void resetFeatureBase() {
		this.featuresForDevice = new HashMap<String, List<FeatureHistory>>();
	}
	
	/**
	 * @param featureHistories
	 * @param feature
	 * @return
	 */
	private boolean containsQualifiedFeatureId(
			List<FeatureHistory> featureHistories, Feature feature) {
		for (FeatureHistory fh : featureHistories) {
			if (fh.hasQualifiedFeatureId(feature)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * @param featureId
	 * @param featureHistories
	 * @return
	 */
	public FeatureHistory getFeatureHistoryByQualifiedFeatureId(
			List<FeatureHistory> featureHistories, String featureId) {
		String id;

		for (FeatureHistory fh : featureHistories) {
			id = fh.getQualifiedFeatureHistoryId();
			if (id != null && id.equals(featureId)) {
				return fh;
			}
		}

		return null;
	}
	
	private List<Feature> getFeaturesForContext(FeatureHistory history) {
		List<Feature> result = new ArrayList<Feature>();

		logger.trace("history: " + history.features.size() + " for featureID: " + history.getQualifiedFeatureHistoryId());
		
		Feature f = null;
		boolean deleted = false;
		for (Pair<Feature, Boolean> p : history.getAllFeatures()) {
			f = p.getFirstElement();
			deleted = p.getSecondElement();

			// if the LATEST element was deleted, return NOTHING
			if (deleted) {
				return result;
			} else {
				result.add(f);
			}
		}
		
		logger.trace("result-size: " + result.size());

		return result;
	}
	
	private List<Feature> getFeaturesForContext(FeatureHistory history, Context context) {
		List<Feature> result = new ArrayList<Feature>();

		logger.trace("history: " + history.features.size() + " for featureID: " + history.getQualifiedFeatureHistoryId());
		
		Feature f = null;
		boolean deleted = false;
		for (Pair<Feature, Boolean> p : history.getAllFeatures()) {
			f = p.getFirstElement();
			deleted = p.getSecondElement();

			// if the element was deleted, return the current list
			if (deleted) {
				return result;
			} else {
				if (context.match(f)) {					
					result.add(f);
				}
			}
		}
		
		logger.trace("result-size: " + result.size());

		return result;
	}
}
