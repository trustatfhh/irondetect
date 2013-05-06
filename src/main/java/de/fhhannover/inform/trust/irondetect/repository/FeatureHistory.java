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

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.ContextParamType;
import de.fhhannover.inform.trust.ContextParameter;
import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.util.Helper;
import de.fhhannover.inform.trust.irondetect.util.Pair;

/**
 * @author bahellma
 *
 */
public class FeatureHistory {

	private static Logger logger = Logger.getLogger(FeatureHistory.class);
	
	LinkedList<Pair<Feature,Boolean>> features;
	
	/**
	 * 
	 */
	public FeatureHistory() {
		this.features = new LinkedList<Pair<Feature,Boolean>>();
	}
	
	/**
	 * @param f
	 */
	public void addFeature(Pair<Feature, Boolean> p) {
		ContextParameter newDateTime = p.getFirstElement().getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
		
		if (this.features.size() == 0) {
			this.features.addFirst(p);
			logger.info("Feature '" + p.getFirstElement().getQualifiedId() + "' was inserted into emtpy FeatureHistory" + " with 'deleted == " + p.getSecondElement() + "'");
		} else {
			logger.info("Trying to insert feature '" + p.getFirstElement().getQualifiedId() + "' into existing FeatureHistory (holds " + this.features.size() + " item(s) at the moment).");
			int idx = 0;
			Feature currentFeature = null;
			
			for (int i = 0; i < this.features.size(); i++) {
				currentFeature = this.features.get(i).getFirstElement();
				ContextParameter currentDateTime = currentFeature.getContextParameterByType(new ContextParamType(ContextParamType.DATETIME));
				
				Calendar newTime = Helper.getXsdStringAsCalendar(newDateTime.getValue());
				Calendar currentTime = Helper.getXsdStringAsCalendar(currentDateTime.getValue());
				
				if (newTime.after(currentTime)) {
					idx = i;
					break;
				} else if (newTime.compareTo(currentTime) == 0) {
					logger.info("Feature was inserted into FeatureHistory with SAME timestamp as an exisiting Feature.");
					idx = i;
					break;
				} else {
					idx = i + 1;
				}
			}
			
			logger.trace("Feature '" + p.getFirstElement().getQualifiedId() + "' was inserted at index = " + idx + " with 'deleted == " + p.getSecondElement() + "'");
			this.features.add(idx, p);
		}
	}
	
	/**
	 * @return
	 */
	public Pair<Feature, Boolean> getLatestFeature() {
		return this.features.getFirst();
	}
	
	/**
	 * @return
	 */
	public List<Pair<Feature, Boolean>> getAllFeatures() {
		return this.features;
	}
	
	/**
	 * @return
	 */
	public String getQualifiedFeatureHistoryId() {
		return this.features.getFirst() == null ? null : this.features.getFirst().getFirstElement().getQualifiedId();
	}
	
	/**
	 * @return
	 */
	public String getQualifiedFeatureHistoryIdWithoutInstance() {
		return this.features.getFirst() == null ? null : this.features.getFirst().getFirstElement().getQualifiedIdWithoutInstance();
	}
	
	/**
	 * @param feature
	 * @return
	 */
	public boolean hasQualifiedFeatureId(Feature feature) {
		return this.hasQualifiedFeatureId(feature.getQualifiedId());
	}
	
	/**
	 * @param qualifiedFeatureId
	 * @return
	 */
	public boolean hasQualifiedFeatureId(String qualifiedFeatureId) {
		return this.getQualifiedFeatureHistoryId().equals(qualifiedFeatureId);
	}

	/**
	 * @param f
	 * @param isDeleted
	 */
	public void addFeature(Feature f, boolean isDeleted) {
		this.addFeature(new Pair<Feature, Boolean>(f, isDeleted));
	}

	/**
	 * @return
	 */
	public int getSize() {
		return this.features.size();
	}
	
	/**
	 * @return
	 */
	public int getNumberOfDeletedFeatures() {
		int result = 0;
		for (Pair<Feature, Boolean> p : this.features) {
			if (p.getSecondElement() == true) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public int getNumberOfNotDeletedFeatures() {
		int result = 0;
		for (Pair<Feature, Boolean> p : this.features) {
			if (p.getSecondElement() == false) {
				result++;
			}
		}
		return result;
	}

}
