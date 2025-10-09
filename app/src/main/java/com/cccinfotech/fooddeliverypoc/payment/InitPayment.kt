package com.cccinfotech.fooddeliverypoc.payment

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.razorpay.Checkout
import org.json.JSONObject

object InitPayment {


    fun initPayment(activity: Activity, context: Context,amount:String) {
        Checkout.preload(context)
        val co = Checkout()
        co.setKeyID("rzp_test_eYmCcLI8Qqwe7N")

        try {
            val option = JSONObject()
            option.put("name", "Payment Get way")
            option.put("name", "software testing")
            option.put("theme.color", "#FF1515")
            option.put("currency", "INR")
            option.put("amount", amount)

            val retry = JSONObject()
            retry.put("enable", true)
            retry.put("max_count", 4)
            retry.put("retry", retry)

            val prefill = JSONObject()
            prefill.put("email", "shabiransari671@gmail.com")
            prefill.put("contact", "7388043661")
            option.put("prefill", prefill)
            co.open(activity, option)

        } catch (e: Exception) {
            Log.d("Code Here", "2")
            Toast.makeText(context, "Payment failed", Toast.LENGTH_SHORT).show()

        }
    }

}