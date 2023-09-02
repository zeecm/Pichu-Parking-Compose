package com.pichugroup.pichuparking.api

import com.google.gson.annotations.SerializedName

enum class VehicleCategory(val description: String) {
    CAR(description = "Car"), MOTORCYCLE(description = "MotorCycle"), HEAVY_VEHICLE(description = "Heavy Vehicle")
}

data class PichuParkingAPIResponse(
    @SerializedName("timestamp") var timestamp: String,
    @SerializedName("data") var data: List<PichuParkingData>,
) {
    fun CheckValid(): Boolean {
        if (this.timestamp.isNullOrBlank() || this.data.isNullOrEmpty() || !this.data.all { it.CheckValid() }) {
            throw IllegalArgumentException("invalid data for PichuParkingAPIResponse")
        }
        return true
    }
}

data class PichuParkingData(
    @SerializedName("CarparkID") var carparkID: String,
    @SerializedName("carparkName") var carparkName: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("vehicleCategory") var vehicleCategory: String,
    @SerializedName("availableLots") var availableLots: Int,
) {
    companion object {
        val vehicleCategoryMap: Map<String, String> = mapOf(
            "C" to VehicleCategory.CAR.description,
            "H" to VehicleCategory.HEAVY_VEHICLE.description,
            "Y" to VehicleCategory.MOTORCYCLE.description,
        )
    }

    val translatedVehicleCategory: String
        get() = vehicleCategoryMap[vehicleCategory] ?: "Unknown"

    fun CheckValid(): Boolean {
        if (carparkID.isNullOrBlank() || carparkName.isNullOrBlank() || latitude == null || longitude == null || vehicleCategory.isNullOrBlank() || availableLots == null) {
            throw IllegalArgumentException("invalid data for PichuParkingData")
        }
        return true
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