/*
 * #%L
 * =====================================================
 *    _____                _     ____  _   _       _   _
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
package de.hshannover.f4.trust;



import java.util.ArrayList;
import java.util.List;


/**
 * @author jvieweg
 *
 */
public class Category {
	

	//private int cardinality;
	private List<Category> subCategories;
	private String id;
	private Category parent;
	
	public Category(String id) {
		this.id = id;
		this.parent = null;
		//this.cardinality = cardinality;
		this.subCategories = new ArrayList<Category>();
	}
	
	public Category(Category source) {
		this.id = source.getId();
		this.subCategories = source.getSubCategories();
		setParent(source.getParent());
	}
	
	public void setParent(Category parent) {
		this.parent = parent;
	}
	
	public Category getParent() {
		return this.parent;
	}

	/**
	 * @return the subCategories
	 */
	public List<Category> getSubCategories() {
		return subCategories;
	}

	/**
	 * @param sub Subcategory to add
	 */
	public void addSubCategory(Category sub) {
		this.subCategories.add(sub);
	}



	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder subCatStr = new StringBuilder("");
		if (this.subCategories != null) {
			subCatStr.append("subcategories=");
			for (Category c : subCategories) {
				subCatStr.append(c.toString());
			}
		}
		return "Category (id= " + this.id + ", " + subCatStr.toString() + ")";
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;		
	}

}
