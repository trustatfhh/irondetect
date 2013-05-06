package de.fhhannover.inform.trust.irondetect.procedure;

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

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.irondetect.model.ProcedureResult;

public class ProcedureResultMapper {
	
	private static Logger logger = Logger.getLogger(ProcedureResultMapper.class);

	public static ProcedureResult map(double actualValue, double expectedValue,
			DistanceType distanceType, Boundary boundaryType, double tolerance,
			double threshold) {
		double distance = 0;
		
		ProcedureResult retVal = new ProcedureResult(-1);
		

		switch (distanceType) {
		case absolute:
			distance = Math.abs(actualValue - expectedValue);
			break;
		case percent:
			distance = Math.abs(actualValue - expectedValue)
					/ expectedValue * 100;
			break;

		default:
			// do nothing
			break;
		}

		switch (boundaryType) {
		case low:
			// check tolerance
			if (actualValue < expectedValue && distance <= tolerance) {
				retVal = new ProcedureResult(-1);
				break;
			}
			// check threshold
			if (actualValue < expectedValue && distance <= threshold) {
				retVal = new ProcedureResult(0);
				break;
			}
			// lower with greater distance than threshold
			if (actualValue < expectedValue && distance > threshold) {
				retVal = new ProcedureResult(1);
				break;
			}
			break;
		case high:
			// check tolerance
			if (actualValue > expectedValue && distance <= tolerance) {
				retVal = new ProcedureResult(-1);
				break;
			}
			// check threshold
			if (actualValue > expectedValue && distance <= threshold) {
				retVal = new ProcedureResult(0);
				break;
			}
			// lower with greater distance than threshold
			if (actualValue > expectedValue && distance > threshold) {
				retVal = new ProcedureResult(1);
				break;
			}
			break;
		case both:
			// check tolerance
			if (distance <= tolerance) {
				retVal = new ProcedureResult(-1);
				break;
			}
			// check threshold
			if (distance <= threshold) {
				retVal = new ProcedureResult(0);
				break;
			}
			// lower with greater distance than threshold
			if (distance > threshold) {
				retVal = new ProcedureResult(1);
				break;
			}
			break;
		}

		logger.trace("mapping to ProcedureResult: actualValue[" + actualValue + "], expectedValue[" + expectedValue + "], distanceType[" + distanceType + "], boundaryType[" + boundaryType + "], tolerance[" + tolerance + "], threshold[" + threshold + "] ==> distance [" + distance + "] ==> " + retVal.getValue());
		return retVal;
	}

	public enum DistanceType {
		absolute, percent
	}

	public enum Boundary {
		low, high, both
	}
}
