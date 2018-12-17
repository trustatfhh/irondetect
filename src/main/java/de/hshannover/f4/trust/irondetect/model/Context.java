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
 * Copyright (C) 2010 - 2018 Trust@HsH
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



import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.irondetect.util.BooleanOperator;
import de.hshannover.f4.trust.irondetect.util.Pair;

/**
 * @author jvieweg
 *
 */
public class Context {

    /**
     * BooleanOperator of first Element in List is null. Consecutive elements contain the BooleanOperators that link them to their predecessor. //TODO Is this a
     * good solution? Alternatives?
     */
    private ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSet;
    private String id;
    private boolean isSliding;
    private long slidingIntervall;
    private Logger logger = Logger.getLogger(Context.class);

    public Context(String id) {
        this.id = id;
        this.isSliding = false;
        this.slidingIntervall = 0;
    }

    public Context() {
        this("");
    }

    /**
     * @return the ctxParamSet
     */
    public ArrayList<Pair<ContextParameterPol, BooleanOperator>> getCtxParamSet() {
        return ctxParamSet;
    }

    /**
     * @param ctxParamSet the ctxParamSet to set
     */
    public void setCtxParamSet(ArrayList<Pair<ContextParameterPol, BooleanOperator>> ctxParamSet) {
        this.ctxParamSet = ctxParamSet;
        for (Pair<ContextParameterPol, BooleanOperator> pair : ctxParamSet) {
            if (pair.getFirstElement().getType().getTypeId() == ContextParamType.SLIDING) {
                String[] timeString = pair.getFirstElement().getValue().split(":");
                if (timeString.length != 3) {
                    logger.error("Wrong time format for sliding context!");
                    this.isSliding = false;
                } else {
                    this.isSliding = true;
                    this.slidingIntervall = Long.parseLong(timeString[0]) * 3600 * 1000 + Long.parseLong(timeString[1]) * 60 * 1000 + Long.parseLong(timeString[2]) * 1000;
                }
            }
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the isSliding
     */
    public boolean isSliding() {
        return isSliding;
    }

    /**
     * @return the slidingIntervall
     */
    public long getSlidingIntervall() {
        return slidingIntervall;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder ctxParStr = new StringBuilder("contextParamSet=");
        for (Pair<ContextParameterPol, BooleanOperator> p : ctxParamSet) {
            ctxParStr.append(p.toString());
        }
        return "Context [" + ctxParStr.toString() + ", " + super.toString() + "]";
    }

    /**
     *
     * Checks if the given feature matches the given context.
     *
     * FIXME: Only first bool op matters
     *
     * @param f the feature
     * @param ctx the context
     * @return true if the feature matches the context, else false (default)
     */
    public boolean match(Feature f) {
        // get the context parameters for the feature
        Set<ContextParameter> ctxParamsFeature = f.getContextParameters();

        // if there is only one there is no boolean operator
        if (this.ctxParamSet.size() == 0) {
            return false;
        }

        logger.debug("Check for context match for Feature '" + f.getQualifiedId() + "', context: " + this);
        boolean result = false;

        // if there is only one there is no boolean operator
        if (this.ctxParamSet.size() == 1) {
            result = this.ctxParamSet.get(0).getFirstElement().check(ctxParamsFeature);
            logger.trace("check was " + result);
            return result;
        }

        BooleanOperator op = this.ctxParamSet.get(1).getSecondElement();

        switch (op) {
            case AND:
                for (int i = 0; i < this.ctxParamSet.size(); i++) {
                    result = this.ctxParamSet.get(i).getFirstElement().check(ctxParamsFeature);
                    if (!result) {
                        logger.trace("check was " + result);
                        return result;
                    }
                }
                break;
            case OR:
                for (int i = 0; i < this.ctxParamSet.size(); i++) {
                    result = this.ctxParamSet.get(i).getFirstElement().check(ctxParamsFeature);
                    if (result) {
                        logger.trace("check was " + result);
                        return result;
                    }
                }
                break;
            default:
                logger.error("Only AND/OR supported at this time!");
                break;
        }

        // return true if all are true
        return true;
    }
}
