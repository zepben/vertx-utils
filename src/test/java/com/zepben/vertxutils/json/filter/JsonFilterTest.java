/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.vertxutils.json.filter;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@EverythingIsNonnullByDefault
public class JsonFilterTest {

    @Test
    public void loadKvnetTest() throws FilterException, IOException {
        String testDataFile = "load/load-complete.json";
        String pattern = "-results(series(energy(maximums(kwIn,kwOut,kwNet,pf),readings(values(kwIn,kwOut,kwNet,pf)))))";
        String expectedOutputFileName = "load/load-kvnet.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void networkNoConnectivityTest() throws FilterException, IOException {
        String testDataFile = "network/network-complete.json";
        String pattern = "-feeders(assets(connections))";
        String expectedOutputFileName = "network/network-no-connectivity.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void networkSimpleConnectivityTest() throws FilterException, IOException {
        String testDataFile = "network/network-complete.json";
        String pattern = "-feeders(assets(connections(numCores,normalDirections,currentDirections,currentPhases,normalPhases),siteId,loadId,feeder,lngLat,lngLats))";
        String expectedOutputFileName = "network/network-simple-connectivity.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void networkMinimalDetailsTest() throws FilterException, IOException {
        String testDataFile = "network/network-complete.json";
        String pattern = "-feeders(assets(type,siteId,loadId,voltage,connections,feeder,length))";
        String expectedOutputFileName = "network/network-minimal-details.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void includeFilterField3Field4Test() throws FilterException, IOException {
        String testDataFile = "abcdefg/abcdefg-complete.json";
        String pattern = "b(e(field3,field4))";
        String expectedOutputFileName = "abcdefg/abcdefg-field3-field4.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void includeFilterFieldXFieldYTest() throws FilterException, IOException {
        String testDataFile = "abcdefg/abcdefg-complete.json";
        String pattern = "c(g(fieldX,fieldY))";
        String expectedOutputFileName = "abcdefg/abcdefg-fieldX-fieldY.json";
        testFilterSpecification(testDataFile, pattern, expectedOutputFileName);
    }

    @Test
    public void testSubfilter() throws FilterException {
        String pattern = "-feeders(assets(connections(numCores,normalDirections,currentDirections,currentPhases,normalPhases),siteId,loadId,feeder,lngLat,lngLats))";
        FilterSpecification fs = new FilterSpecification(pattern);
        FilterSpecification subfs = fs.getSubfilter("feeders.assets.connections").orElseThrow(AssertionError::new);
        String subFilterPattern = subfs.toString();
        assertThat(subFilterPattern, equalTo("connections(currentDirections,currentPhases,normalDirections,normalPhases,numCores)"));

        assertThat(fs.getSubfilter("feeders.blah"), equalTo(Optional.empty()));
    }


    private void testFilterSpecification(String testDataFile,
                                         String pattern,
                                         String expectedOutputFileName) throws IOException, FilterException {

        // Load the test data.
        JsonObject testData = new JsonObject(IOUtils.toString(getClass().getResourceAsStream(testDataFile), UTF_8));

        // Create the specification
        FilterSpecification filterSpecification = new FilterSpecification(pattern);

        // Filter it
        JsonObjectFilter.applyFilter(testData, filterSpecification);

        // Load the expected outcome.
        JsonObject expected = new JsonObject(IOUtils.toString(getClass().getResourceAsStream(expectedOutputFileName), UTF_8));

        // Assert that they're the same
        assertThat(testData, equalTo(expected));
    }

}
