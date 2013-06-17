package de.fhhannover.inform.trust.irondetect.util;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import de.fhhannover.inform.trust.ifmapj.exception.IfmapErrorResult;
import de.fhhannover.inform.trust.ifmapj.messages.PollResult;
import de.fhhannover.inform.trust.ifmapj.messages.ResultItem;
import de.fhhannover.inform.trust.ifmapj.messages.SearchResult;

/**
 * Helper class.
 *
 * @author ibente
 *
 */
public class Helper {

    private static final Logger logger = Logger.getLogger(Helper.class);

    /**
     * Check if the given op String equals 'update'.
     *
     * @param op
     * @return
     */
    public static boolean isUpdate(String op) {
        return ("update".equals(op));
    }

    /**
     * Check if the given op String equals 'delete'.
     *
     * @param op
     * @return
     */
    public static boolean isDelete(String op) {
        return ("delete".equals(op));
    }

    /**
     * Check if the given op String equals 'update' or 'delete'.
     *
     * @param op
     * @return
     */
    public static boolean isUpdateorDelete(String op) {
        return Helper.isUpdate(op) || Helper.isDelete(op);
    }

    /**
     * Prepare access to truststore by creating an InputStream. This supports<br/> both truststores that reside within the packaged jar as well as those<br/>
     * that reside on the local filesystem separately from the jar.<br/>
     *
     * @param path - path to the keystore
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream prepareTruststoreIs(String path)
            throws FileNotFoundException {
        InputStream is;
        // try jar
        is = Helper.class.getResourceAsStream(path);
        if (is == null) {
            logger.warn("Truststore " + path + " not "
                    + "found in jar. Will try filesystem now...");
            // try filesystem
            is = new FileInputStream(new File(path));
            logger.trace("Truststore was found at " + path);
        }

        return is;
    }

    /**
     * Prepare access to a file by creating an InputStream. This supports<br/> both files that reside within the packaged jar as well as those<br/> that reside
     * on the local filesystem separately from the jar.<br/> The file system is checked first.
     *
     * @param path - path to the file
     * @return
     * @throws FileNotFoundException
     */
    public static InputStream getInputStreamForFile(String path) throws FileNotFoundException {
        InputStream is;
        // try filesystem
        try {
            logger.trace("looking for file on disk ...");
            is = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            // try jar
            logger.trace("looking for file in jar ...");
            is = Helper.class.getResourceAsStream(path);
            if (is == null) {
                throw new FileNotFoundException("file was not found on disk or in jar: " + path);
            }
        }
        return is;
    }

    /**
     * Format the Date as xsd:DateTime.
     *
     * @param d - the date the is to be formatted
     * @return the xsd:DateTime String, e.g. 2003-05-31T13:20:05+0500
     */
    public static String getTimeAsXsdDateTime(Date d) {
        // this uses ugly hacks since we do not want to rely on JAXB
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("Z");
        String one = sdf1.format(d.getTime());
        String offset = sdf2.format(d.getTime());
        String offsetA = offset.substring(0, 3);
        String offsetB = offset.substring(3);
        return one + offsetA + ":" + offsetB;
    }

    /**
     * Transforms a given xsd:DateTime {@link String} (e.g. 2003-05-31T13:20:05-05:00) to a Java {@link Calendar} object. Uses DatetypeConverter to parse the
     * {@link String} object.
     *
     * @param xsdDateTime - the xsd:DateTime that is to be transformed.
     * @return a {@link Calendar} object representing the given xsd:DateTime
     */
    public static Calendar getXsdStringAsCalendar(String xsdDateTime) {
        assert xsdDateTime != null && !xsdDateTime.isEmpty();
        try {
            if (xsdDateTime.contains("+")) {
                int idxTz = xsdDateTime.lastIndexOf("+");
                int idxLastColon = xsdDateTime.lastIndexOf(":");
                if(idxLastColon < idxTz) {
                    String p1 = xsdDateTime.substring(0, idxTz+3);
                    String p2 = xsdDateTime.substring(idxTz+3, xsdDateTime.length());
                    xsdDateTime = p1 + ":" + p2;
                }
            } 
            if (xsdDateTime.contains(":")) {	// if the String contains an ':' literal, we try to interpret it as a xsdDateTime-string
                return DatatypeConverter.parseDateTime(xsdDateTime);
            } else {	// try to parse a time in milliseconds to a Calendar object
                Calendar tmp = new GregorianCalendar();
                tmp.setTimeInMillis(Long.parseLong(xsdDateTime));
                return tmp;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Illegal data/time format found (incoming String was: "
                    + xsdDateTime + "); setting to current date/time.");
            return new GregorianCalendar();
        }
    }

    /**
     * Transforms a given Java Calendar object to a xsd:DateTime String. Uses DatetypeConverter to "print" the {@link Calendar} object as a xsd:DateTime
     * {@link String}.
     *
     * @param c - the {@link Calendar} object including date and time.
     * @return a {@link String} in xsd:DateTime format, e.g. 2003-05-31T13:20:05-05:00
     */
    public static String getCalendarAsXsdDateTime(Calendar c) {
        return DatatypeConverter.printDateTime(c);
    }

    /**
     * 'Easy' Java way to transform a {@link Document} to a {@link String}
     *
     * @param doc
     * @return
     * @throws TransformerException
     */
    public static String documentToString(Document doc)
            throws TransformerException {
        // oh well ...
        StreamResult result = new StreamResult(new StringWriter());
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.transform(new DOMSource(doc), result);
        return result.getWriter().toString();
    }

    /**
     * Print {@link PollResult}
     */
    public static void printPollResult(PollResult pollResult) {

        // error, search, update, delete, notify
        Collection<IfmapErrorResult> errorRes = pollResult.getErrorResults();
        Collection<SearchResult> allRes = pollResult.getResults();
        Collection<SearchResult> searchRes = new Vector<SearchResult>();
        Collection<SearchResult> updateRes = new Vector<SearchResult>();
        Collection<SearchResult> deleteRes = new Vector<SearchResult>();
        Collection<SearchResult> notifyRes = new Vector<SearchResult>();

        for (SearchResult res : allRes) {
            switch (res.getType()) {
                case searchResult:
                    searchRes.add(res);
                    break;
                case updateResult:
                    updateRes.add(res);
                    break;
                case deleteResult:
                    deleteRes.add(res);
                    break;
                case notifyResult:
                    notifyRes.add(res);
                    break;
                default:
                    break;
            }
        }

        if (errorRes.size() > 0) {
            logger.debug("== ERROR RESULTS ==");
            for (IfmapErrorResult error : errorRes) {
                logger.debug(error);
            }
            System.exit(1);
        }

        if (searchRes.size() > 0) {
            logger.debug("== SEARCH RESULTS ==");
            for (SearchResult searchResult : searchRes) {
                parseSearchResult(searchResult);
            }
        }

        if (updateRes.size() > 0) {
            logger.debug("== UPDATE RESULTS ==");
            for (SearchResult searchResult : updateRes) {
                parseSearchResult(searchResult);
            }
        }

        if (deleteRes.size() > 0) {
            logger.debug("== DELETE RESULTS ==");
            for (SearchResult searchResult : deleteRes) {
                parseSearchResult(searchResult);
            }
        }

        if (notifyRes.size() > 0) {
            logger.debug("== NOTIFY RESULTS ==");
            for (SearchResult searchResult : notifyRes) {
                parseSearchResult(searchResult);
            }
        }
    }

    /**
     * Parse {@link SearchResult} object and print it to console
     */
    private static void parseSearchResult(SearchResult sr) {
        for (ResultItem resultItem : sr.getResultItems()) {
            logger.debug("****************************************************************************");
            logger.debug(resultItem);
            Collection<Document> meta = resultItem.getMetadata();
            for (Document document : meta) {
                try {
                    logger.debug(documentToString(document));
                } catch (TransformerException e) {
                    e.printStackTrace();
                }
            }
            logger.debug("****************************************************************************");
        }
    }
}
