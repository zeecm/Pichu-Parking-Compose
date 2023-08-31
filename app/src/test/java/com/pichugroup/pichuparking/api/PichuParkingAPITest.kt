package com.pichugroup.pichuparking.api

import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class PichuParkingAPITest {
    private val parkingAPIClient = PichuParkingAPIClient()
    @Test
    fun testGetParkingLots() {
        val parkingLotData = runBlocking {
            parkingAPIClient.getParkingLots()
        }
        assert(parkingLotData[0] is PichuParkingData)
    }
}