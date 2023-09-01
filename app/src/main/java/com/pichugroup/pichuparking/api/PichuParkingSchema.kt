package com.pichugroup.pichuparking.api

import com.google.gson.annotations.SerializedName

data class PichuParkingAPIResponse(
    @SerializedName("timestamp") var timestamp: String,
    @SerializedName("data") var data: List<PichuParkingData>,
)

data class PichuParkingData(
    @SerializedName("carparkID") var carparkID: String,
    @SerializedName("carparkName") var carparkName: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("vehicleCategory") var vehicleCategory: String,
    @SerializedName("availableLots") var availableLots: Int,
) {
    companion object {
        val vehicleCategoryMap: Map<String, String> = mapOf(
            "C" to "Car",
            "H" to "Heavy Vehicle",
            "Y" to "Motorcycle",
        )
    }
    fun translateVehicleCategory(): String {
        return vehicleCategoryMap[vehicleCategory] ?: "Unknown"
    }
}

data class PichuParkingRates(
    @SerializedName("carparkID") var carparkID: String,
    @SerializedName("vehicleCategory") var vehicleCategory: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("parkingSystem") var parkingSystem: String,
    @SerializedName("timeRange") var timeRange: String,
    @SerializedName("weekdayMin") var weekdayMin: String,
    @SerializedName("weekdayRate") var weekdayRate: String,
    @SerializedName("saturdayMin") var saturdayMin: String,
    @SerializedName("saturdayRate") var saturdayRate: String,
    @SerializedName("sundayMin") var sundayMin: String,
    @SerializedName("sundayPHRate") var sundayPHRate: String,
)