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
 * This file is part of irondetect, version 0.0.7, 
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
package de.hshannover.f4.trust.irondetect.engine;



import de.hshannover.f4.trust.irondetect.model.ConditionElement;
import de.hshannover.f4.trust.irondetect.model.Rule;

/**
 * @author bahellma
 *
 */
public class Trigger {

	private String id;
	private String device;
	private Rule root;
	private long slidingIntervall;

	public Trigger(String device, Rule root, long slidingIntervall, String conElementId) {
		this.device = device;
		this.root = root;
		this.slidingIntervall = slidingIntervall;
		this.id = this.device + ":" + conElementId;
	}

	public long getSlidingIntervall() {
		return this.slidingIntervall;
	}

	public String getDevice() {
		return this.device;
	}
	
	public Rule getRootRule() {
		return this.root;
	}
	
	public String getId() {
		return this.id;
	}
	
	@Override
	public String toString() {
		return "TRIGGER [" + this.id + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Trigger) {
			Trigger other = (Trigger) obj;
			if (this.id.equals(other.id)) {
				return true;
			} else {
				return false;
			}
		} else {			
			return super.equals(obj);
		}
	}
}
