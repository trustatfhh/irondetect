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



import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import org.apache.log4j.Logger;

import de.hshannover.f4.trust.ContextParamType;
import de.hshannover.f4.trust.ContextParameter;
import de.hshannover.f4.trust.irondetect.util.ComparisonOperator;
import de.hshannover.f4.trust.irondetect.util.Helper;

/**
 * @author rosso
 *
 */
public class ContextParameterPol extends Evaluable {

    private ComparisonOperator compOp;
    private ContextParamType type;
    private String value;
    private static Logger logger = Logger.getLogger(ContextParameterPol.class);

    /**
     * @param id
     * @param type
     * @param value
     * @param compOp
     */
    public ContextParameterPol(String id, ContextParamType type, String value,
            ComparisonOperator compOp) {
        this.setId(id);
        this.compOp = compOp;
        this.setType(type);
        this.setValue(value);
    }

    /**
     * @param comOp
     */
    public void setComparisonOperator(ComparisonOperator comOp) {
        this.compOp = comOp;
    }

    /**
     * @return
     */
    public ComparisonOperator getComparisonOperator() {
        return this.compOp;
    }

    /**
     * @return the type
     */
    public ContextParamType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ContextParamType type) {
        this.type = type;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * If types do not match, false is returned.
     *
     * @param ctxParamsFeature
     * @return
     */
    public boolean check(Set<ContextParameter> ctxParamsFeature) {
        for (ContextParameter contextParameter : ctxParamsFeature) {
            // check type
            if (contextParameter.getType().getTypeId() == this.type.getTypeId()
                    || (contextParameter.getType().getTypeId() == ContextParamType.DATETIME
                    && this.type.getTypeId() == ContextParamType.SLIDING)) {
                // compare

                ComparisonOperator comparisonOperator = this.compOp;

                // FIXME: db4o <-> enums ... equals and == DON'T return true on
                // equally enums ... :-/
                // switch (this.type) {
                if (this.type.getTypeId() == ContextParamType.TRUSTLEVEL) {
                    // case TRUSTLEVEL:{
                    int desired = Integer.parseInt(this.value);
                    int actual = Integer.parseInt(contextParameter.getValue());
                    logger.debug("Actual TL " + actual + " should be "
                            + this.compOp + " than desired TL " + desired);
                    switch (comparisonOperator) {
                        case EQ:
                            return desired == actual;
                        case ST:
                            return desired > actual;
                        case GT:
                            return desired < actual;
                        case NE:
                            return desired != actual;
                        case SE:
                            return desired >= actual;
                        case GE:
                            return desired <= actual;
                        default:
                            return false;
                    }
                } else if (this.type.getTypeId() == ContextParamType.DATETIME) {
                    // case DATETIME:{
                    Calendar actualDateTime = Helper
                            .getXsdStringAsCalendar(contextParameter.getValue());

                    String[] timeString = this.value.split(":");
                    Calendar desiredDateTime = new GregorianCalendar(
                            actualDateTime.get(GregorianCalendar.YEAR),
                            actualDateTime.get(GregorianCalendar.MONTH),
                            actualDateTime.get(GregorianCalendar.DATE),
                            Integer.parseInt(timeString[0]),
                            Integer.parseInt(timeString[1]),
                            0); // TODO
                    // evil
                    // and
                    // dirty
                    // hack
                    desiredDateTime.setTimeZone(actualDateTime.getTimeZone());

                    logger.debug("Feature date/time: "
                            + Helper.getCalendarAsXsdDateTime(actualDateTime));
                    logger.debug("Desired date/time: "
                            + Helper.getCalendarAsXsdDateTime(desiredDateTime));

                    switch (comparisonOperator) {
                        case EQ:
                            return actualDateTime.compareTo(desiredDateTime) == 0;
                        case GE:
                            return actualDateTime.compareTo(desiredDateTime) >= 0;
                        case GT:
                            return actualDateTime.compareTo(desiredDateTime) > 0;
                        case NE:
                            return actualDateTime.compareTo(desiredDateTime) != 0;
                        case SE:
                            return actualDateTime.compareTo(desiredDateTime) <= 0;
                        case ST:
                            return actualDateTime.compareTo(desiredDateTime) < 0;
                        default:
                            return false;
                    }
                } else if (this.type.getTypeId() == ContextParamType.SLIDING) {
                    // case SLIDING:{
                    Calendar featureDateTime = Helper
                            .getXsdStringAsCalendar(contextParameter.getValue());
                    String[] timeString = this.value.split(":");
                    if (timeString.length != 3) {
                        logger.error("Wrong time format for sliding context!");
                        return false;
                    }
                    long delta = Long.parseLong(timeString[0]) * 3600 * 1000
                            + Long.parseLong(timeString[1]) * 60 * 1000
                            + Long.parseLong(timeString[2]) * 1000;
                    long current = System.currentTimeMillis();
                    long featureTime = featureDateTime.getTimeInMillis();

                    logger.debug("Feature date/time: "
                            + Helper.getCalendarAsXsdDateTime(featureDateTime));
                    logger.debug("Sliding intervall (secs): " + delta / 1000.f
                            + "current time: " + (new Date(current)).toString());

                    switch (comparisonOperator) {
                        case EQ:
                            return featureTime >= (current - delta);
                        case NE:
                            return featureTime < (current - delta);
                        default:
                            return false;
                    }
                } else {
                    // case LOCATION:
                    // case OTHERDEVICES:
                    // default:
                    return super.evaluateCompOpOnString(this.compOp,
                            contextParameter.getValue(), this.value);
                }
            }
        }
        return false;
    }

    @Override
    public boolean evaluate(String device) {
        return false;
    }
}
