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
/**
 * 
 */
package de.hshannover.f4.trust.irondetect.model;



import java.util.Set;


/**
 * @author jvieweg
 * @author ibente
 * 
 */
public class Feature {
	
	private TrustLog trustLog;

	private String value;
	private FeatureType type;
	private Set<ContextParameter> contextParameters;
	private Category category;
	private String id;

	public Feature(String id, String value,
			FeatureType type, Category category,
			Set<ContextParameter> contextParams) {
		this.setId(id);
		this.value = value;
		this.type = type;
		this.category = category;
		this.contextParameters = contextParams;
	}


	public Feature() {
		this("", "", null, null, null);
	}
	
	
	public Feature(Feature source) {
		//TODO echte tiefe Kopie!
		this(source.getId(), source.value, source.type, source.category, source.contextParameters);
	}
	
	/**
	 * @return the value
	 */

	/**
	 * @return the type
	 */
	public FeatureType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(FeatureType type) {
		this.type = type;
	}

	/**
	 * @return the category
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * @param category
	 *            the category to set
	 */
	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * @return the contextParameters
	 */
	public Set<ContextParameter> getContextParameters() {
		return contextParameters;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder ctxParamStr = new StringBuilder("contextParameters=");
		if(contextParameters != null) {
			for (ContextParameter c : contextParameters) {
				ctxParamStr.append(c.toString());
			}
		}
		return "Feature [ ID=" + this.id + (this.trustLog != null ? ", TL = " + this.trustLog.getTrustLevel() : "") + ", type=" + type + ", value=" + value + ", "
				+ ctxParamStr.toString() + ", category=" + category + "]";
	}
	
	/**
	 * @return
	 */
	public String getQualifiedId() {
		return this.category.getId() + "." + this.getId();
	}

	/**
	 * @return
	 */
	public String getQualifiedIdWithoutInstance() {
		StringBuilder result = new StringBuilder();
		String[] tmp = this.category.getId().split("\\.");
		int firstColon;
		String instanceString;
		for (int i = 0; i < tmp.length; i++) {
			firstColon = tmp[i].indexOf(":");
			if (firstColon != -1) {				
				instanceString = tmp[i].substring(firstColon);
				result.append(tmp[i].replaceAll(instanceString, ""));
			} else {
				result.append(tmp[i]);
			}
			result.append(".");
		}
		result.append(this.getId());
		return result.toString();
	}
	
	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.irondetect.model.prototype.ModelElement#getId()
	 */
	public String getId() {
		return this.id;
	}
	
	/* (non-Javadoc)
	 * @see de.hshannover.f4.trust.irondetect.model.prototype.ModelElement#setId(java.lang.String)
	 */
	public void setId(String id) {
		this.id = this.normalize(id);
	}
	
	/**
	 * @param id
	 * @return
	 */
	private String normalize(String id) {
		StringBuilder result = new StringBuilder();
		
		String[] tmp = id.split("\\.");	// remove categories, if present
		
		// replace underscores and convert everything left to lower case
		if (tmp != null &&  tmp.length >= 1) {
			result.append(tmp[tmp.length - 1].replace("_", "").toLowerCase());
		} else {
			result.append(id.replace("_", "").toLowerCase());
		}

		return result.toString();
	}


	/**
	 * @param parameters
	 * @param type
	 * @return
	 */
	public ContextParameter getContextParameterByType(ContextParamType type) {
		ContextParameter result = null;

		for (ContextParameter param : this.contextParameters) {
			if (param.getType().getTypeId() == type.getTypeId()) {
				return param;
			}
		}

		return result;
	}


	/**
	 * @return the trustLog
	 */
	public TrustLog getTrustLog() {
		return trustLog;
	}


	/**
	 * @param trustLog the trustLog to set
	 */
	public void setTrustLog(TrustLog trustLog) {
		this.trustLog = trustLog;
	}

}
