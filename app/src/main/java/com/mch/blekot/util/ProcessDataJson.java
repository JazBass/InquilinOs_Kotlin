package com.mch.blekot.util;

import java.util.HashMap;
import java.util.Iterator;
import com.mch.blekot.util.IData;
import org.json.JSONException;
import org.json.JSONObject;

public final class ProcessDataJson implements IData{
    /**
     * Indica si los campos han sido mapeados con el JSON
     * TRUE -> OK
     * FALSE -> KO
     */
    private boolean state;

    // Mapa con las claves y valores del JSON
    private HashMap<String, Object> dataMap;

    public ProcessDataJson() {
        // TODO Auto-generated constructor stub
        this.state = false;
        this.dataMap = new HashMap<>();
    }

    public boolean isState() {
        return state;
    }

    public void clearData() {
        this.dataMap.clear();
    }

    public Object getValue(String key) {
        return this.dataMap.containsKey(key) ? this.dataMap.get(key) : null;
    }

    @Override
    public void getData(JSONObject dataJson) {
        // TODO Auto-generated method stub
        try {
            Iterator<String> it = dataJson.keys();
            String key = "";

            while (it.hasNext()) {
                key = it.next().toString();
                this.dataMap.put(key, dataJson.get(key));
            }
            this.state = true;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            this.state = false;
        }
    }

    @Override
    public String toString() {
        return "ProcessDataJson " + this.dataMap.toString();
    }
}