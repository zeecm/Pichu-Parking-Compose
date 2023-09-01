package com.pichugroup.pichuparking.api

import com.google.gson.Gson
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

internal class PichuParkingAPIClient {
    private val logger = KotlinLogging.logger {}
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
        private const val PARKING_LOTS_RESOURCE: String = "parking-lots"
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

    suspend fun getParkingLots(bikesOnly: Boolean = true): List<PichuParkingData> {
        var finalData: List<PichuParkingData> = listOf()
        val parkingLotEndpoint: String = API_INVOKE_URL + PARKING_LOTS_RESOURCE
        try {
            val parkingLotResponse: HttpResponse = makeAPICall(
                endpoint = parkingLotEndpoint,
                headers = requestHeader,
            )

            val pichuResponse: PichuParkingAPIResponse =
                deserializePichuParkingResponse(parkingLotResponse.body())
            finalData = pichuResponse.data
        } catch (e: UnresolvedAddressException) {
            logger.error(e) { "Unresolved address error: $e" }
        } catch (e: ClientRequestException) {
            val statusCode = e.response.status.value
            logger.error(e) { "Client request error with status code $statusCode: $e" }
        } catch (e: ServerResponseException) {
            val statusCode = e.response.status.value
            logger.error(e) { "Server response error with status code $statusCode: $e" }
        } catch (e: Exception) {
            logger.error(e) { "General error: $e" }
        }
        if (bikesOnly) {
            finalData = finalData.filter { it.vehicleCategory == "Y"}
        }
        return finalData
    }


    private fun deserializePichuParkingResponse(jsonText: String): PichuParkingAPIResponse {
        val gson = Gson()
        return gson.fromJson(jsonText, PichuParkingAPIResponse::class.java)
    }
}