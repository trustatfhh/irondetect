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
package de.hshannover.f4.trust.irondetect.model;



import java.util.ArrayList;
import java.util.List;

public class TrustLog {

    private int trustLevel;
    private int trustTokenId;
    private List<Integer> processSenderSecurityPropertyRatings;
    private List<Integer> transmitSenderSecurityPropertyRatings;
    private List<Integer> processProviderSecurityPropertyRatings;
    private List<Integer> transmitProviderSecurityPropertyRatings;

    public TrustLog(int trustTokenId) {
        this.trustTokenId = trustTokenId;
        this.processSenderSecurityPropertyRatings = new ArrayList<Integer>();
    }

    /**
     * @return the trustTokenId
     */
    public int getTrustTokenId() {
        return trustTokenId;
    }

    /**
     * @return the trustLevel
     */
    public int getTrustLevel() {
        return trustLevel;
    }

    public List<Integer> getProcessSenderSecurityPropertyRatings() {
        return processSenderSecurityPropertyRatings;
    }

    public List<Integer> getTransmitProviderSecurityPropertyRatings() {
        return transmitProviderSecurityPropertyRatings;
    }

    public List<Integer> getProcessProviderSecurityPropertyRatings() {
        return processProviderSecurityPropertyRatings;
    }

    public List<Integer> getTransmitSenderSecurityPropertyRatings() {
        return transmitSenderSecurityPropertyRatings;
    }

    public void setProcessProviderSecurityPropertyRatings(List<Integer> processProviderSecurityPropertyRatings) {
        this.processProviderSecurityPropertyRatings = processProviderSecurityPropertyRatings;
    }

    public void setProcessSenderSecurityPropertyRatings(List<Integer> processSenderSecurityPropertyRatings) {
        this.processSenderSecurityPropertyRatings = processSenderSecurityPropertyRatings;
    }

    public void setTransmitProviderSecurityPropertyRatings(List<Integer> transmitProviderSecurityPropertyRatings) {
        this.transmitProviderSecurityPropertyRatings = transmitProviderSecurityPropertyRatings;
    }

    public void setTransmitSenderSecurityPropertyRatings(List<Integer> transmitSenderSecurityPropertyRatings) {
        this.transmitSenderSecurityPropertyRatings = transmitSenderSecurityPropertyRatings;
    }
    
    

    

    /**
     * @param trustLevel the trustLevel to set
     */
    public void setTrustLevel(int trustLevel) {
        this.trustLevel = trustLevel;
    }
}
