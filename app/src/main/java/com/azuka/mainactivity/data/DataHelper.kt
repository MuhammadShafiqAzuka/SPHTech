package com.azuka.mainactivity.data

import android.content.Context
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


object DataHelper {
    private const val TIMEOUT_REQUEST = 10000 // In millis
    private var queue: RequestQueue? = null

    //Request for a GET JSON
    fun getJson(
        context: Context?,
        url: String?,
        jsonObject: JSONObject?,
        responseListener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener?
    ) {
        val request =
            JsonObjectRequest(Request.Method.GET, url, jsonObject, responseListener, errorListener)
        request.retryPolicy =
            DefaultRetryPolicy(TIMEOUT_REQUEST, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        if (queue == null) {
            queue = Volley.newRequestQueue(context)
        }
        queue!!.add(request)
    }
}