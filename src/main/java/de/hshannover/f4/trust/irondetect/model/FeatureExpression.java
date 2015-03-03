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
 * This file is part of irondetect, version 0.0.6, 
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
package de.hshannover.f4.trust.irondetect.model;



import org.apache.log4j.Logger;

import de.hshannover.f4.trust.Feature;
import de.hshannover.f4.trust.FeatureType;
import de.hshannover.f4.trust.irondetect.util.ComparisonOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;

public class FeatureExpression extends Evaluable {

    private Logger logger = Logger.getLogger(this.getClass());
    //fID , CompOp, Value
    private Pair<String, Pair<ComparisonOperator, String>> featureValPair;
    //feature which should be loaded from the featureBase
    private Feature feature = null;
    private boolean counter;
    private boolean valid;
    private boolean isEvaluated;
    private boolean isRevalidated;
    private boolean tagged;
    private int step = 0;
    private int scope = 0;

    public FeatureExpression(Feature actual, FeatureExpression expr, String id) {
        this.feature = actual;
        this.featureValPair = new Pair<String, Pair<ComparisonOperator, String>>(expr.getFeatureValuePair().getFirstElement(),
                new Pair<ComparisonOperator, String>(expr.getFeatureValuePair().getSecondElement().getFirstElement(),
                expr.getFeatureValuePair().getSecondElement().getSecondElement()));
        this.id = id;
        this.scope = expr.getScope();
        this.valid = true;
        this.counter = false;
        this.isEvaluated = false;
        this.isRevalidated = false;
        this.tagged = false;
    }

    /**
     * 
     * @param actual feature set by feature base (left side)
     * @param featureValPair policy based fID, comparison operator and desired feature value (right side)
     * @param id id of the feature expression itself
     */
    public FeatureExpression(Feature actual, Pair<String, Pair<ComparisonOperator, String>> featureValPair, String id, int scope) {
        this.feature = actual;
        this.featureValPair = featureValPair;
        this.id = id;
        this.valid = true;
        this.isEvaluated = false;
        this.counter = false;
        this.isRevalidated = false;
         this.tagged = false;
         this.scope = scope;
    }

    public FeatureExpression() {
        this.valid = true;
        this.counter = false;
        this.isEvaluated = false;
        this.isRevalidated = false;
         this.tagged = false;
    }

    /**
     * @param featureValPair
     */
    public void setFeatureValuePair(Pair<String, Pair<ComparisonOperator, String>> featureValPair) {
        String fId = featureValPair.getFirstElement();
        
        
        //check if there is a scope defined
        this.scope = 0;
        if(fId.contains(Policy.SCOPE_KEY)) {
            
            int idx = fId.lastIndexOf(Policy.SCOPE_KEY);
            if(idx < fId.length()-1) {
                this.scope = Integer.parseInt(fId.substring(idx+1));
            }
            fId = fId.substring(0, idx);
            logger.trace("Setting scope to " + this.scope + " for ID " + fId);
        }
        featureValPair.setFirstElement(fId);
        this.featureValPair = featureValPair;
    }

    /**
     * @return
     */
    public Pair<String, Pair<ComparisonOperator, String>> getFeatureValuePair() {
        return this.featureValPair;
    }

    public boolean evaluate(String device) {

        boolean hasTrust = false;

        if (feature == null) {
            return false;
        }

        if (feature.getTrustLog() != null) {
            hasTrust = true;
        }

        boolean result = false;

        // FIXME: db4o <-> enums ... equals and == DON'T return true on equally enums ... :-/
//		switch (feature.getType()) {
//		case QUANTITIVE:
//			result = super.evaluateCompOpOnNumber(featureValPair.getSecondElement().getFirstElement(), feature.getValue(), 
//					featureValPair.getSecondElement().getSecondElement());
//			break;
//		case QUALIFIED:
//			result = super.evaluateCompOpOnString(featureValPair.getSecondElement().getFirstElement(), feature.getValue(), 
//					featureValPair.getSecondElement().getSecondElement());
//			break;
//		default:
//			result = super.evaluateCompOpOnString(featureValPair.getSecondElement().getFirstElement(), feature.getValue(), 
//					featureValPair.getSecondElement().getSecondElement());
//			break;
//			
//		}
        if (feature.getType().getTypeId() == FeatureType.QUANTITIVE) {
            result = super.evaluateCompOpOnNumber(featureValPair.getSecondElement().getFirstElement(), feature.getValue(),
                    featureValPair.getSecondElement().getSecondElement());
        } else if (feature.getType().getTypeId() == FeatureType.QUALIFIED) {
            result = super.evaluateCompOpOnString(featureValPair.getSecondElement().getFirstElement(), feature.getValue(),
                    featureValPair.getSecondElement().getSecondElement());
        } else {
            result = super.evaluateCompOpOnString(featureValPair.getSecondElement().getFirstElement(), feature.getValue(),
                    featureValPair.getSecondElement().getSecondElement());
        }

        logger.debug("evaluating feature " + this.feature.getQualifiedId() + ", type is " + this.feature.getType() + ", value is " + this.feature.getValue() + " | against "
                + this.featureValPair.getSecondElement().getFirstElement() + " " + this.featureValPair.getSecondElement().getSecondElement() + " = " + result);
        if(hasTrust) {
            logger.trace("TL= " + feature.getTrustLog().getTrustLevel() + "; SPR Ratings: " );
            logger.trace("spr-process-sender");
            for(Integer i : feature.getTrustLog().getProcessSenderSecurityPropertyRatings()){
                logger.trace(i);
            }
            logger.trace("spr-transmit-sender");
            for(Integer i : feature.getTrustLog().getTransmitSenderSecurityPropertyRatings()){
                logger.trace(i);
            }
            logger.trace("spr-process-provider");
            for(Integer i : feature.getTrustLog().getProcessProviderSecurityPropertyRatings()){
                logger.trace(i);
            }
            logger.trace("spr-transmit-provider");
            for(Integer i : feature.getTrustLog().getTransmitProviderSecurityPropertyRatings()){
                logger.trace(i);
            }
        }
        return result;

    }

    public void setFeature(Feature value) {
        this.feature = value;
    }

    public Feature getFeature() {
        return this.feature;
    }

    public String getFeatureId() {
        return this.featureValPair.getFirstElement().toLowerCase();
    }

    public void invalidate() {
        this.valid = false;
    }
    
    public void validate() {
        this.valid = true;
    }

    /**
     * @return the valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return the counter
     */
    public boolean isCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter() {
        this.counter = true;
    }
    
    public boolean isEvaluated(){
        return this.isEvaluated;
    }
    
    public void setEvaluated() {
        this.isEvaluated = true;
    }
    
    public void setRevalidated() {
        this.valid = true;
        this.isRevalidated = true;
        this.tagged = false;
    }
    
    public boolean isRevalidated() {
        return this.isRevalidated;
    }
    
    public void tagValidation() {
        this.tagged = true;
    }
    
    public void unTagValidation() {
        this.tagged = false;
    }
    
    public boolean isTagged() {
        return this.tagged;
    }
    
    public void setStep(int s) {
        this.step = s;
    }
    
    public int getStep() {
        return this.step;
    }
    
    public int getScope() {
        return this.scope;
    }
}
