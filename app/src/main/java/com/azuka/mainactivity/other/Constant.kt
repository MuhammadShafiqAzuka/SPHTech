package com.azuka.mainactivity.other

import android.os.Bundle
import com.azuka.mainactivity.fragments.DiaglogFragment

class Constant {
    companion object {
        fun newInstance(
            year: String,
            quarter: String,
            volume: String,
            isDecreased: String
        ): DiaglogFragment {
            val DetailDialog = DiaglogFragment()
            val bundle = Bundle(1)
            bundle.putString("year", year)
            bundle.putString("quarter", quarter)
            bundle.putString("volume", volume)
            bundle.putString("isDecreased", isDecreased)
            DetailDialog.arguments = bundle
            return DetailDialog
        }
    }
}