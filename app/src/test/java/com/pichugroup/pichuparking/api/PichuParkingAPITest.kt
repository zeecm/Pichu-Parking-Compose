package com.pichugroup.pichuparking.api

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

internal class PichuParkingAPITest {
    private lateinit var parkingAPIClient: PichuParkingAPIClient
    @Before
    fun setup() {
        parkingAPIClient = PichuParkingAPIClient()
    }
    @Test
    fun testGetParkingLotsIterable() {
        val parkingData: List<PichuParkingData>?
        runBlocking {
            parkingData = parkingAPIClient.getParkingLots()
        }
        if (parkingData != null) {
            assert(parkingData.all { it is PichuParkingData })
        }
    }
}