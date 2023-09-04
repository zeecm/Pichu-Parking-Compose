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
        val parkingData: List<PichuParkingLots>?
        runBlocking {
            parkingData = parkingAPIClient.getParkingLots(VehicleCategory.all)
        }
        if (parkingData != null) {
            assert(parkingData.all { it is PichuParkingLots })
        }
    }

    @Test
    fun testGetParkingRatesIterable() {
        val parkingData: List<PichuParkingRates>?
        runBlocking {
            parkingData = parkingAPIClient.getParkingRates()
        }
        if (parkingData != null) {
            assert(parkingData.all { it is PichuParkingRates })
        }
    }
}