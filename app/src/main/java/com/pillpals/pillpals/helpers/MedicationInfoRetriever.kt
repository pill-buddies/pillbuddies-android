package com.pillpals.pillpals.helpers

import android.util.Log
import com.google.gson.Gson
import com.pillpals.pillpals.data.*
import com.shopify.promises.Promise
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit



import okhttp3.Call
import okhttp3.OkHttpClient
import java.util.*

object OkHttpUtils {
    fun cancelCallWithTag(client: OkHttpClient, tag: String) {
        for (call in client.dispatcher.queuedCalls()) {
            if (call.request().tag() == tag)
                call.cancel()
        }
        for (call in client.dispatcher.runningCalls()) {
            if (call.request().tag() == tag)
                call.cancel()
        }
    }
}

class MedicationInfoRetriever {
    companion object {
        /* Example of use
        MedicationInfoRetriever.activeIngredients(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun activeIngredients(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/activeingredient/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val activeIngredients = gson.fromJson(jsonString, Array<ActiveIngredient>::class.java).toList()

                            val ingredientNameList = activeIngredients.fold(listOf<String>()) { acc, it ->
                                acc.plus(it.ingredient_name)
                            }

                            val dosageValues = activeIngredients.fold("") { acc, it ->
                                if(acc.isNotEmpty()) acc + "/" + it.strength
                                else acc + it.strength
                            }

                            val dosageUnits = activeIngredients.fold(listOf<String>()) { acc, it ->
                                if(acc.contains(it.strength_unit)) acc
                                else acc.plus(it.strength_unit)
                            }.joinToString("/")

                            resolve(ingredientNameList)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.intakeRoutes(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun intakeRoutes(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/route/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val administrationRoutes = gson.fromJson(jsonString, Array<AdministrationRoute>::class.java).toList()

                            val administrationRouteNames = administrationRoutes.fold(listOf<String>()) { acc, it ->
                                acc.plus(it.route_of_administration_name)
                            }

                            resolve(administrationRouteNames)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.drugSchedules(73177).whenComplete { result: Promise.Result<List<String>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun drugSchedules(dpdId: Int): Promise<List<String>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://health-products.canada.ca/api/drug/schedule/?id=${dpdId}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val drugSchedules = gson.fromJson(jsonString, Array<DrugSchedule>::class.java).toList()

                            val drugScheduleNames = drugSchedules.fold(listOf<String>()) { acc, it ->
                                acc.plus(if(it.schedule_name == "OTC") "Over The Counter (OTC)" else it.schedule_name)
                            }

                            resolve(drugScheduleNames)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.interactions(listOf("207106", "152923", "656659")).whenComplete { result: Promise.Result<List<InteractionResult>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun interactions(rxcuis: List<String>): Promise<List<InteractionResult>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://rxnav.nlm.nih.gov/REST/interaction/list.json?rxcuis=${rxcuis.joinToString("+")}"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val interactionsResponse = gson.fromJson(jsonString, InteractionsResponse::class.java)

                            var res: List<InteractionResult> = listOf()

                            // Use first source I guess. Might specify to try for DrugBank later
                            val source = interactionsResponse.fullInteractionTypeGroup.firstOrNull()

                            source ?: return resolve(res)

                            source.fullInteractionType.forEach { fullInteractionType ->
                                fullInteractionType.interactionPair.forEach { interactionPair ->
                                    val interactionRxcuis = interactionPair.interactionConcept.fold(listOf<String>()) { acc, it ->
                                        acc.plus(it.minConceptItem.rxcui)
                                    }

                                    res = res.plus(InteractionResult(interactionRxcuis, interactionPair.description))
                                }
                            }

                            resolve(res.distinctBy { interactionResult -> interactionResult.interaction })
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.sideEffects("54092-381").whenComplete { result: Promise.Result<List<SideEffectResult>, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun sideEffects(ndcId: String): Promise<List<SideEffectResult>, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/event.json?search=patient.drug.openfda.product_ndc:\"${ndcId}\"&count=patient.reaction.reactionmeddrapt.exact"

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val adverseEffectResults = gson.fromJson(jsonString, OpenFDAAdverseEffectsAggregateResponse::class.java)

                            var res: List<SideEffectResult> = listOf()

                            val termObjects = adverseEffectResults.results

                            Log.i("test", termObjects.toString())

                            if(termObjects.isEmpty()) return resolve(res)

                            val totalCount = termObjects.fold(0) {acc, it -> acc + it.count}.toFloat()

                            res = termObjects.fold(listOf<SideEffectResult>()) {acc, it -> acc.plus(SideEffectResult(it.term, it.count, it.count.toFloat() / totalCount))}

                            resolve(res)
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.description("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun description(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            val label = labelResult.results.firstOrNull()

                            label ?: return resolve("")

                            val description = label.description.firstOrNull()

                            description ?: return resolve("")

                            resolve(description.removePrefix("11 DESCRIPTION "))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.warning("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun warning(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            val label = labelResult.results.firstOrNull()

                            label ?: return resolve("")

                            val warning = label.warnings_and_cautions.firstOrNull()

                            warning ?: return resolve("")

                            resolve(warning.removePrefix("5 WARNINGS AND PRECAUTIONS ").replace("(\\(5\\..\\))".toRegex(), "\n"))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.overdosage("54092-381").whenComplete { result: Promise.Result<String, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun overdosage(ndcId: String): Promise<String, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/label.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val labelResult = gson.fromJson(jsonString, OpenFDALabelResponse::class.java)

                            val label = labelResult.results.firstOrNull()

                            label ?: return resolve("")

                            val overdosage = label.overdosage.firstOrNull()

                            overdosage ?: return resolve("")

                            resolve(overdosage.removePrefix("10 OVERDOSAGE "))
                        }
                    }
                })
            }
        }

        /* Example of use
        MedicationInfoRetriever.recalls("54092-189").whenComplete { result: Promise.Result<RecallsResult, RuntimeException> ->
            when (result) {
                is Promise.Result.Success -> {
                    // Use result here
                    Log.i("Success", result.value.toString())
                }
                is Promise.Result.Error -> Log.i("Error", result.error.message!!)
            }
        }
         */
        fun recalls(ndcId: String): Promise<RecallsResult, RuntimeException> {
            return Promise {
                val client = OkHttpClient
                    .Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build()

                val url = "https://api.fda.gov/drug/enforcement.json?search=openfda.product_ndc:\"${ndcId}\""

                val request = Request.Builder().url(url).build()

                onCancel {
                    reject(RuntimeException("Canceled"))
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        reject(RuntimeException("Failed"))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (!response.isSuccessful) throw IOException("Unexpected code $response")

                            val jsonString = response.body!!.string()
                            val gson = Gson()
                            val recallsResult = gson.fromJson(jsonString, OpenFDARecallsResponse::class.java)

                            val recalls = recallsResult.results

                            if(recalls.isEmpty()) return resolve(RecallsResult(false, false, listOf()))

                            var mandated = false

                            val recallQuantitiesList = recalls.fold(listOf<String>()) {acc, it ->
                                if(it.voluntary_mandated.contains("Mandated")) mandated = true
                                acc.plus(it.product_quantity)
                            }

                            resolve(RecallsResult(recallQuantitiesList.any(), mandated, recallQuantitiesList))
                        }
                    }
                })
            }
        }
    }
}

data class InteractionResult(
    var rxcuis: List<String>,
    var interaction: String
)

data class SideEffectResult(
    var sideEffect: String,
    var rawCount: Int,
    var percent: Float
)

data class RecallsResult(
    val hasBeenRecalled: Boolean,
    val anyMandatoryRecalls: Boolean,
    val recallQuantities: List<String>
)

