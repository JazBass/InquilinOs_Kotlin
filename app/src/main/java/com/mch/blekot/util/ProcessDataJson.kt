package com.mch.blekot.util

import org.json.JSONObject
import org.json.JSONException
import java.util.HashMap

class ProcessDataJson : IData {
    /**
     * Indica si los campos han sido mapeados con el JSON
     * TRUE -> OK
     * FALSE -> KO
     */
    var isState = false
        private set

    // Mapa con las claves y valores del JSON
    private val dataMap: HashMap<String, Any> = HashMap()

    fun clearData() {
        dataMap.clear()
    }

    fun getValue(key: String): Any? {
        return if (dataMap.containsKey(key)) dataMap[key] else null
    }

    override fun getData(dataJson: JSONObject) {
        try {
            val it = dataJson.keys()
            var key = ""
            while (it.hasNext()) {
                key = it.next().toString()
                dataMap[key] = dataJson[key]
            }
            isState = true
        } catch (e: JSONException) {
            e.printStackTrace()
            isState = false
        }
    }

    override fun toString(): String {
        return "ProcessDataJson $dataMap"
    }
}