package com.azuka.mainactivity.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.azuka.mainactivity.R
import com.azuka.mainactivity.data.MobileData
import com.azuka.mainactivity.fragments.DiaglogFragment
import java.util.*


class DataAdapter(
    var context: Context,
    private val mobileDataList: kotlin.collections.List<MobileData>
) :
    RecyclerView.Adapter<DataAdapter.MobileDataHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MobileDataHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.car_view_list, parent, false)
        return MobileDataHolder(view)
    }

    override fun onBindViewHolder(holder: MobileDataHolder, position: Int) {
        val mobileData = mobileDataList[position]
        var totalVolume = 0.0
        var preVolume = 0.0
        var hasDecrease = false
        val quarter = StringBuilder()
        val volume = StringBuilder()
        val isDecreased = StringBuilder()
        val volumeByQuarterMap: HashMap<Int, Double> = mobileData.volumeByQuarterMap
        for (i in 1..4) {
            //add up all quarter and display as year
            //append values into string builder to be displayed when user click on the image
            if (volumeByQuarterMap[i] != null) {
                //has value for that quarter
                totalVolume += volumeByQuarterMap[i]!!
                quarter.append(i).append(",")
                volume.append(java.lang.String.format("%f", volumeByQuarterMap[i])).append(",")

                //Compare each quarter to see if there is a decrease
                if (preVolume < volumeByQuarterMap[i]!!) {
                    preVolume = volumeByQuarterMap[i]!!
                    isDecreased.append("0,")
                } else {
                    hasDecrease = true
                    isDecreased.append("1,")
                }
            } else {
                //has no value for that quarter
                totalVolume += 0
            }
        }
        holder.tvYear.text =
            String.format(context.getString(R.string.year), mobileData.year.toString())
        holder.tvVolume.text = String.format("%f", totalVolume)
        if (hasDecrease) {
            //there is a decrease in quarter, show clickable image.
            holder.ivDecrease.visibility = View.VISIBLE
            holder.ivDecrease.isEnabled = true
        } else {
            //there is not any decrease in quarter, hide clickable image.
            holder.ivDecrease.visibility = View.GONE
            holder.ivDecrease.isEnabled = false
        }
        holder.ivDecrease.setOnClickListener { //show dialog
            val dialogFragment: DiaglogFragment = DiaglogFragment.newInstance(
                mobileData.year.toString(),
                quarter.toString(), volume.toString(), isDecreased.toString()
            )
            dialogFragment.isCancelable = false
            dialogFragment.showsDialog
        }
    }

    override fun getItemCount(): Int {
        return mobileDataList.size
    }

    inner class MobileDataHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        lateinit var itemView: View
        var tvYear: TextView = itemView.findViewById<View>(R.id.tvYear) as TextView
        var tvVolume: TextView = itemView.findViewById<View>(R.id.tvVolume) as TextView
        var ivDecrease: ImageView = itemView.findViewById<View>(R.id.ivDecrease) as ImageView

    }
}