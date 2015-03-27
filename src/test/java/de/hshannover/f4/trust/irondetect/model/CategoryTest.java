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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.hshannover.f4.trust.irondetect.model;



import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.hshannover.f4.trust.irondetect.model.Category;
import static org.junit.Assert.*;

/**
 *
 * @author ibente
 */
public class CategoryTest {
    
    /**
     * Test of setParent method, of class Category.
     */
    @Test
    public void testGetSetParent() {
        System.out.println("setParent");
        Category parent = new Category("parent");
        Category child = new Category("child");
        child.setParent(parent);
        assertEquals(parent, child.getParent());
    }

    /**
     * Test of getSubCategories method, of class Category.
     */
    @Test
    public void testGetSetSubCategories() {
        System.out.println("getSubCategories");
        
        Category parent = new Category("parent");
        List<Category> firstLevelChildren = new ArrayList<Category>();
        List<Category> secondLevelChildren = new ArrayList<Category>();
        for (int i = 0; i < 10; i++) {
            Category c = new Category("first level " + i);
            c.setParent(parent);
            parent.addSubCategory(c);
            firstLevelChildren.add(c);
        }
        for (int i = 0; i < 10; i++) {
            Category category = new Category("second level " + i);
            category.setParent(firstLevelChildren.get(i));
            firstLevelChildren.get(i).addSubCategory(category);
            secondLevelChildren.add(category);
        }
        
        assertEquals(parent.getSubCategories().size(), firstLevelChildren.size());
        for (int i = 0; i < 10; i++) {          
          assertEquals(parent.getSubCategories().get(i).getSubCategories().get(0), secondLevelChildren.get(i));
        }
    }

    /**
     * Test of toString method, of class Category.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        Category parent = new Category("parent");
        Category first = new Category("first");
        Category second = new Category("second");
        
        parent.addSubCategory(first);
        first.setParent(parent);
        parent.addSubCategory(second);
        second.setParent(parent);
        
        String expResult = "Category (id= parent, subcategories=Category (id= first, subcategories=)Category (id= second, subcategories=))";
        String result = parent.toString();
        assertEquals(expResult, result);
    }

}
