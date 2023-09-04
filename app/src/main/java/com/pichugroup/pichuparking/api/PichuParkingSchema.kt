package com.pichugroup.pichuparking.api

import com.google.gson.annotations.SerializedName

enum class VehicleCategory(val description: String) {
    CAR(description = "Car"), MOTORCYCLE(description = "MotorCycle"), HEAVY_VEHICLE(description = "Heavy Vehicle");

    companion object {
        val all: Set<VehicleCategory> = values().toSet()
    }
}

sealed class PichuParkingAPIResponse<T : PichuParkingData> {
    abstract var timestamp: String
    abstract var data: Set<T>

    fun checkValid(): Boolean {
        if (this.timestamp.isNullOrBlank() ||
            this.data.isNullOrEmpty() ||
            !this.data.all { it.checkValid() }
        ) {
            throw IllegalArgumentException("invalid data for PichuParkingAPIResponse")
        }
        return true
    }
}

data class PichuParkingAPIParkingLotResponse(
    @SerializedName("timestamp") override var timestamp: String,
    @SerializedName("data") override var data: Set<PichuParkingLots>,
): PichuParkingAPIResponse<PichuParkingLots>()

data class PichuParkingAPIParkingRatesResponse(
    override var timestamp: String,
    override var data: Set<PichuParkingRates>,
): PichuParkingAPIResponse<PichuParkingRates>()

sealed class PichuParkingData {
    abstract val carparkID: String
    abstract val carparkName: String
    abstract val vehicleCategory: String
    abstract val latitude: Double
    abstract val longitude: Double

    companion object {
        val vehicleCategoryMap: Map<String, String> = mapOf(
            "C" to VehicleCategory.CAR.description,
            "H" to VehicleCategory.HEAVY_VEHICLE.description,
            "Y" to VehicleCategory.MOTORCYCLE.description,
        )
    }

    abstract fun checkValid(): Boolean
}

data class PichuParkingLots(
    @SerializedName("carparkID") override val carparkID: String,
    @SerializedName("carparkName") override val carparkName: String,
    @SerializedName("latitude") override val latitude: Double,
    @SerializedName("longitude") override val longitude: Double,
    @SerializedName("vehicleCategory") override val vehicleCategory: String,
    @SerializedName("availableLots") val availableLots: Int,
    @SerializedName("agency") val agency: String
) : PichuParkingData() {
    val translatedVehicleCategory: String
        get() = vehicleCategoryMap[vehicleCategory] ?: "Unknown"

    override fun checkValid(): Boolean {
        if (carparkID.isNullOrBlank() &&
            carparkName.isNullOrBlank() &&
            latitude == null &&
            longitude == null &&
            vehicleCategory.isNullOrBlank() &&
            availableLots == null) {
            throw IllegalArgumentException("invalid data for PichuParkingLots")
        }
        return true
    }
}

data class PichuParkingRates(
    @SerializedName("carparkID") override val carparkID: String,
    @SerializedName("carparkName") override val carparkName: String,
    @SerializedName("latitude") override val latitude: Double,
    @SerializedName("longitude") override val longitude: Double,
    @SerializedName("vehicleCategory") override val vehicleCategory: String,
    @SerializedName("parkingSystem") val parkingSystem: String,
    @SerializedName("capacity") val capacity: Int,
    @SerializedName("timeRange") val timeRange: String,
    @SerializedName("weekdayMin") val weekdayMin: String,
    @SerializedName("weekdayRate") val weekdayRate: String,
    @SerializedName("saturdayMin") val saturdayMin: String,
    @SerializedName("saturdayRate") val saturdayRate: String,
    @SerializedName("sundayPHMin") val sundayPHMin: String,
    @SerializedName("sundayPHRate") val sundayPHRate: String
) : PichuParkingData() {
    override fun checkValid(): Boolean{
        if (carparkID.isNullOrBlank() &&
            carparkName.isNullOrBlank() &&
            latitude == null &&
            longitude == null &&
            vehicleCategory.isNullOrBlank() &&
            parkingSystem.isNullOrBlank() &&
            capacity == null &&
            timeRange.isNullOrBlank() &&
            weekdayMin.isNullOrBlank() &&
            weekdayRate.isNullOrBlank() &&
            saturdayMin.isNullOrBlank() &&
            saturdayRate.isNullOrBlank() &&
            sundayPHMin.isNullOrBlank() &&
            sundayPHRate.isNullOrBlank()) {
            throw IllegalArgumentException("invalid data for PichuParkingRates")
        }
        return true
    }
}