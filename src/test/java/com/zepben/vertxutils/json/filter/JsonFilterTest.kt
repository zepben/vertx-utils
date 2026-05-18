/*
 * Copyright 2026 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.vertxutils.json.filter

import com.zepben.testutils.junit.SystemLogExtension
import com.zepben.vertxutils.json.filter.JsonObjectFilter.Companion.applyFilter
import io.vertx.core.json.JsonObject
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

class JsonFilterTest {

    companion object {

        @JvmField
        @RegisterExtension
        val systemOut: SystemLogExtension = SystemLogExtension.SYSTEM_OUT.captureLog().muteOnSuccess()

    }

    @Test
    fun loadKvnetTest() {
        testFilterSpecification(
            testDataFile = "load/load-complete.json",
            pattern = "-results(series(energy(maximums(kwIn,kwOut,kwNet,pf),readings(values(kwIn,kwOut,kwNet,pf)))))",
            expectedOutputFileName = "load/load-kvnet.json",
        )
    }

    @Test
    fun networkNoConnectivityTest() {
        testFilterSpecification(
            testDataFile = "network/network-complete.json",
            pattern = "-feeders(assets(connections))",
            expectedOutputFileName = "network/network-no-connectivity.json",
        )
    }

    @Test
    fun networkSimpleConnectivityTest() {
        testFilterSpecification(
            testDataFile = "network/network-complete.json",
            pattern = "-feeders(assets(connections(numCores,normalDirections,currentDirections,currentPhases,normalPhases),siteId,loadId,feeder,lngLat,lngLats))",
            expectedOutputFileName = "network/network-simple-connectivity.json",
        )
    }

    @Test
    fun networkMinimalDetailsTest() {
        testFilterSpecification(
            testDataFile = "network/network-complete.json",
            pattern = "-feeders(assets(type,siteId,loadId,voltage,connections,feeder,length))",
            expectedOutputFileName = "network/network-minimal-details.json",
        )
    }

    @Test
    fun includeFilterField3Field4Test() {
        testFilterSpecification(
            testDataFile = "abcdefg/abcdefg-complete.json",
            pattern = "b(e(field3,field4))",
            expectedOutputFileName = "abcdefg/abcdefg-field3-field4.json",
        )
    }

    @Test
    fun includeFilterFieldXFieldYTest() {
        testFilterSpecification(
            testDataFile = "abcdefg/abcdefg-complete.json",
            pattern = "c(g(fieldX,fieldY))",
            expectedOutputFileName = "abcdefg/abcdefg-fieldX-fieldY.json",
        )
    }

    @Test
    fun testSubfilter() {
        val fs = FilterSpecification(
            "-feeders(assets(connections(numCores,normalDirections,currentDirections,currentPhases,normalPhases),siteId,loadId,feeder,lngLat,lngLats))",
        )

        assertThat(
            fs.getSubfilter("feeders.assets.connections").toString(),
            equalTo("connections(currentDirections,currentPhases,normalDirections,normalPhases,numCores)"),
        )

        assertThat(fs.getSubfilter("feeders.blah"), nullValue())
    }

    private fun testFilterSpecification(
        testDataFile: String,
        pattern: String,
        expectedOutputFileName: String,
    ) {
        // Load the test data.
        val testData = JsonObject(javaClass.getResource(testDataFile)?.readText())

        // Create the specification
        val filterSpecification = FilterSpecification(pattern)

        // Filter it
        applyFilter(testData, filterSpecification)

        // Load the expected outcome.
        val expected = JsonObject(javaClass.getResource(expectedOutputFileName)?.readText())

        // Assert that they're the same
        assertThat(testData, equalTo(expected))
    }

}
