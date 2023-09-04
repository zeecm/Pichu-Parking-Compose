package com.pichugroup.pichuparking.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pichugroup.pichuparking.BuildConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import java.nio.channels.UnresolvedAddressException

private val logger = KotlinLogging.logger {}



internal class PichuParkingAPIClient {
    private var requestHeader: MutableMap<String, String> = mutableMapOf(
        "x-api-key" to BuildConfig.PICHU_PARKING_API_KEY
    )

    private val httpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
        }
    }

    companion object {
        private const val API_INVOKE_URL: String =
            "https://q7p4ehtedd.execute-api.ap-southeast-1.amazonaws.com/prod/"
        enum class ParkingData(val endpoint: String) {
            LOTS("parking-lots"), RATES("parking-rates")
        }
    }

    private suspend fun makeAPICall(
        endpoint: String,
        headers: Map<String, String>? = mapOf(),
        params: Map<String, String>? = mapOf(),
    ): HttpResponse {
        val response: HttpResponse = httpClient.get(endpoint) {
            headers {
                headers?.forEach { (headerName, headerValue) ->
                    append(headerName, headerValue)
                }
            }
            url {
                params?.forEach { (paramName, paramValue) ->
                    parameters.append(paramName, paramValue)
                }
            }
        }
        if (response.status.value != 200) {
            val responseBody: String = runBlocking {
                response.body()
            }
            logger.error { "failed to get ok response from api $responseBody" }
        }
        return response
    }

    suspend fun getParkingLots(
        vehicleCategories: Set<VehicleCategory> = setOf(
            VehicleCategory.CAR, VehicleCategory.MOTORCYCLE
        )
    ): List<PichuParkingLots>? {
        return try {
            val parkingLotResponse = fetchParkingData(ParkingData.LOTS)
            parseAndFilterParkingData(parkingLotResponse, vehicleCategories)
        } catch (e: Exception) {
            handleException(e)
            null
        }
    }

    suspend fun getParkingRates(): List<PichuParkingRates>? {
        return try {
            val parkingRateResponse = fetchParkingData(ParkingData.RATES)
            val pichuResponse: PichuParkingAPIParkingRatesResponse = deserializePichuParkingResponse(parkingRateResponse.body())
            return pichuResponse.data.toList()
        } catch (e: Exception) {
            handleException(e)
            null
        }
    }

    private suspend fun fetchParkingData(parkingData: ParkingData): HttpResponse {
        val parkingLotEndpoint = API_INVOKE_URL + parkingData.endpoint
        return makeAPICall(endpoint = parkingLotEndpoint, headers = requestHeader)
    }

    private suspend fun parseAndFilterParkingData(
        parkingLotResponse: HttpResponse, vehicleCategories: Set<VehicleCategory>
    ): List<PichuParkingLots> {
        val pichuResponse: PichuParkingAPIParkingLotResponse =
            deserializePichuParkingResponse(parkingLotResponse.body())
        val lotData: Set<PichuParkingLots> = pichuResponse.data
        return lotData.filter { data ->
            vehicleCategories.any { category ->
                category.description == data.translatedVehicleCategory
            }
        }
    }

    private fun handleException(e: Exception) {
        when (e) {
            is UnresolvedAddressException -> logger.error(e) { "Unresolved address error: $e" }
            is ClientRequestException -> {
                val statusCode = e.response.status.value
                logger.error(e) { "Client request error with status code $statusCode: $e" }
            }

            is ServerResponseException -> {
                val statusCode = e.response.status.value
                logger.error(e) { "Server response error with status code $statusCode: $e" }
            }

            else -> logger.error(e) { "General error: $e" }
        }
    }


    private inline fun <reified T : PichuParkingAPIResponse<SubT>, reified SubT : PichuParkingData> deserializePichuParkingResponse(
        jsonText: String
    ): T {
        val gson = Gson()
        val typeToken = object : TypeToken<T>() {}.type
        val pichuResponse = gson.fromJson<T>(jsonText, typeToken)
        pichuResponse.checkValid()
        return pichuResponse
    }

}