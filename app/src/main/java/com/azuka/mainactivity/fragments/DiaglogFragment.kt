package com.azuka.mainactivity.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.azuka.mainactivity.R


class DiaglogFragment : DialogFragment() {
    var year: String? = null
    var quarter: String? = null
    var volume: String? = null
    var isDecreased: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        year = requireArguments().getString("year")
        quarter = requireArguments().getString("quarter")
        volume = requireArguments().getString("volume")
        isDecreased = requireArguments().getString("isDecreased")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val dialogInfo: View =
            requireActivity().layoutInflater.inflate(R.layout.dialog_details, null)
        val quarterList: MutableList<Array<String>> =
            mutableListOf(quarter!!.split(",".toRegex()).toTypedArray())
        val volumeList: MutableList<Array<String>> =
            mutableListOf(volume!!.split(",".toRegex()).toTypedArray())
        val isDecreaseList: MutableList<Array<String>> =
            mutableListOf(isDecreased!!.split(",".toRegex()).toTypedArray())
        var counter = 0
        for (i in 1..4) {
            //only display if there is record for that quarter.
            val llQuarter = dialogInfo.findViewById<LinearLayout>(
                requireActivity().resources.getIdentifier(
                    "llQ$i", "id", requireActivity().packageName
                )
            )
            if (quarterList.contains(i.toString())) {
                val tvQuarter = dialogInfo.findViewById<TextView>(
                    requireActivity().resources.getIdentifier(
                        "tvQ$i", "id", requireActivity().packageName
                    )
                )
                val tvVol = dialogInfo.findViewById<TextView>(
                    requireActivity().resources.getIdentifier(
                        "tvVolQ$i", "id", requireActivity().packageName
                    )
                )
                llQuarter.visibility = View.VISIBLE
                tvQuarter.text = String.format(
                    getString(R.string.quarter),
                    quarterList[counter]
                )
                tvVol.text = volumeList[counter].toString()
                if (isDecreaseList[counter].equals("1")) {
                    //indicate in red for decreased quarter
                    tvVol.setTextColor(resources.getColor(R.color.colorRed))
                } else {
                    //indicate in black for normal quarter
                    tvVol.setTextColor(resources.getColor(R.color.colorBlack))
                }
                counter++
            } else {
                //hide if there is no record for that quarter
                llQuarter.visibility = View.GONE
            }
        }
        builder.setView(dialogInfo)
        builder.setPositiveButton(
            R.string.ok
        ) { dialog, _ -> dialog.dismiss() }
        builder.setTitle(year)
        return builder.create()
    }

    companion object {
        fun newInstance(
            year: String?,
            quarter: String?,
            volume: String?,
            isDecreased: String?
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