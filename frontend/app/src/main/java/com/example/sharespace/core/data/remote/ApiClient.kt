package com.example.sharespace.core.data.remote

import com.example.sharespace.core.data.repository.dto.finance.ApiBill
import com.example.sharespace.core.data.repository.dto.finance.ApiPayment
import com.example.sharespace.core.data.repository.dto.finance.ApiTransaction
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.reflect.TypeToken
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private val transactionDeserializer = JsonDeserializer<ApiTransaction> { json, _, context ->
        val jsonObject = json.asJsonObject

        // Check if the JSON has a type field
        if (jsonObject.has("type")) {
            val type = jsonObject.get("type").asString

            when (type) {
                "bill" -> context.deserialize<ApiBill>(json, ApiBill::class.java)
                "payment" -> context.deserialize<ApiPayment>(json, ApiPayment::class.java)
                else -> throw IllegalArgumentException("Unknown transaction type: $type")
            }
        } else {
            throw IllegalArgumentException("JSON missing required 'type' field for ApiTransaction")
        }
    }

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        // Register the deserializer only for ApiTransaction class
        .registerTypeAdapter(ApiTransaction::class.java, transactionDeserializer)
        // Also register for List<ApiTransaction> to handle the collection properly
        .registerTypeAdapter(
            object : TypeToken<List<ApiTransaction>>() {}.type,
            JsonDeserializer { json, typeOfT, context ->
                val jsonArray = json.asJsonArray
                val transactions = mutableListOf<ApiTransaction>()

                for (element in jsonArray) {
                    transactions.add(context.deserialize(element, ApiTransaction::class.java))
                }

                transactions
            }
        )
        .create()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}