/**
 *
 */
package de.fhhannover.inform.trust.irondetect.model;

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

import java.util.List;

import org.apache.log4j.Logger;

import de.fhhannover.inform.trust.Feature;
import de.fhhannover.inform.trust.irondetect.engine.TriggerManager;
import de.fhhannover.inform.trust.irondetect.repository.FeatureBaseImpl;

/**
 * Adds context to the whole thing.
 *
 * @author jvieweg
 *
 */
public abstract class ConditionElement extends Evaluable {

    private List<Context> contextSet;
    private Logger logger = Logger.getLogger(this.getClass());
    private Rule parent;

    /**
     * @return the contextSet
     */
    public List<Context> getContextSet() {
        return contextSet;
    }

    /**
     * @param contextSet the contextSet to set
     */
    public void setContextSet(List<Context> contextSet) {
        this.contextSet = contextSet;
    }

    protected void checkSlidingCtx(String device) {
        for (Context ctx : this.contextSet) {
            if (ctx.isSliding()) {
                logger.debug("found sliding context- creating trigger for device " + device + " with intervall " + ctx.getSlidingIntervall());
                TriggerManager.createNewTrigger(device, this.parent, ctx.getSlidingIntervall(), this.id);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder ctxSetStr = new StringBuilder("contextSet=");
        for (Context c : contextSet) {
            ctxSetStr.append(c.toString());
        }
        return "ConditionElement [" + ctxSetStr.toString() + ", "
                + super.toString() + "]";
    }

    /**
     * @param device
     * @param featureIds
     * @return list which contains a pair consisting of featureId and the appropriate
     */
    protected synchronized List<Feature> getFeatureValues(String device, List<String> featureIds) {
        logger.trace("trying to get feature values, contextSet = " + contextSet);
        return FeatureBaseImpl.getInstance().getFeaturesByContext(device, featureIds, contextSet);
    }

    /**
     * @param parent the parent rule to set
     */
    public void setParent(Rule parent) {
        this.parent = parent;
    }
}
