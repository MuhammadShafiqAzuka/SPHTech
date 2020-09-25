package com.azuka.mainactivity.mainview

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.azuka.mainactivity.R
import com.azuka.mainactivity.adapter.DataAdapter
import com.azuka.mainactivity.data.DataHelper
import com.azuka.mainactivity.data.MobileData
import org.json.JSONException
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private var rvData: RecyclerView? = null
    private var tvCount: TextView? = null
    private var dataAdapter: DataAdapter? = null
    private val mobileDataList: ArrayList<MobileData> = ArrayList()
    private var year = 0
    private var file: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvData = findViewById<View>(R.id.rvData) as RecyclerView
        tvCount = findViewById<View>(R.id.tvCount) as TextView
        file = File(getExternalFilesDir(null), FILE_NAME)
        DataHelper.getJson(this@MainActivity,
            SERVER_DATA_URL,
            null,
            { response ->
                try {
                    if (response.getString("success") == "true") {
                        //success returns "true"
                        val result = response.getJSONObject("result")
                        val recordArray = result.getJSONArray("records")
                        if (recordArray.length() != 0) {
                            //There are records

                            //First record
                            year = recordArray.getJSONObject(0).getString("quarter").substring(0, 4)
                                .toInt()
                            deleteLocalFile()
                            //Save first record as the field name
                            saveToLocal("" + "Year" + "," + "Q1" + " , " + "Q2" + "," + "Q3" + "," + "Q4")
                            val volumeByQuarterMap: HashMap<Int, Double?> = HashMap()
                            for (i in 0 until recordArray.length()) {
                                //Loop all records
                                val recordObject = recordArray.getJSONObject(i)
                                val yearQuarter = recordObject.getString("quarter")
                                if (i != recordArray.length() - 1) {
                                    //Not the last record
                                    if (year == yearQuarter.substring(0, 4).toInt()) {
                                        //same year, use the same map
                                        volumeByQuarterMap[yearQuarter.substring(6).toInt()] =
                                            recordObject.getDouble("volume_of_mobile_data")
                                    } else {
                                        //different year, save record in current year
                                        saveRecord(year, volumeByQuarterMap)

                                        //create record for new year by clearing the map
                                        volumeByQuarterMap.clear()
                                        year = yearQuarter.substring(0, 4).toInt()
                                        volumeByQuarterMap[yearQuarter.substring(6).toInt()] =
                                            recordObject.getDouble("volume_of_mobile_data")
                                    }
                                } else {
                                    //Last record, save the last result.
                                    if (year == yearQuarter.substring(0, 4).toInt()) {
                                        //same year, use the same map
                                        volumeByQuarterMap[yearQuarter.substring(6).toInt()] =
                                            recordObject.getDouble("volume_of_mobile_data")
                                        saveRecord(year, volumeByQuarterMap)
                                    } else {
                                        //different year, save record in current year.
                                        saveRecord(year, volumeByQuarterMap)

                                        //create record for new year by clearing the map.
                                        volumeByQuarterMap.clear()
                                        year = yearQuarter.substring(0, 4).toInt()
                                        volumeByQuarterMap[yearQuarter.substring(6).toInt()] =
                                            recordObject.getDouble("volume_of_mobile_data")
                                        //save record as it is the last record.
                                        saveRecord(year, volumeByQuarterMap)
                                    }
                                }
                                //refresh adapter
                                dataAdapter!!.notifyDataSetChanged()
                            }
                            tvCount!!.text = String.format(
                                getString(R.string.result_count),
                                java.lang.String.valueOf(mobileDataList.size)
                            )
                        } else {
                            //no records. prompt to read local file if there is.
                            if (file!!.exists()) {
                                showWarningDialog(
                                    getString(R.string.no_record_title),
                                    getString(R.string.no_record_has_local), true
                                )
                            } else {
                                showWarningDialog(
                                    getString(R.string.no_record_title),
                                    getString(R.string.no_record_no_local), false
                                )
                            }
                        }
                    } else {
                        //success returns not "true" (Wrong/incomplete parameter in URL)
                        //prompt to read local file if there is.
                        if (file!!.exists()) {
                            showWarningDialog(
                                getString(R.string.no_record_title),
                                getString(R.string.no_record_has_local), true
                            )
                        } else {
                            showWarningDialog(
                                getString(R.string.no_record_title),
                                getString(R.string.no_record_no_local), false
                            )
                        }
                    }
                } catch (e: JSONException) {
                    //Prompt to read local file if there is.
                    if (file!!.exists()) {
                        showWarningDialog(
                            getString(R.string.read_url_failed_title),
                            getString(R.string.read_url_failed_has_local), true
                        )
                    } else {
                        showWarningDialog(
                            getString(R.string.read_url_failed_title),
                            getString(R.string.read_url_failed_no_local), false
                        )
                    }
                    e.printStackTrace()
                }
            },
            { //Wrong url or cleared cache or cleared data without internet
                //Prompt to read local file if there is.
                if (file!!.exists()) {
                    showWarningDialog(
                        getString(R.string.read_url_failed_title),
                        getString(R.string.read_local_has_local), true
                    )
                } else {
                    showWarningDialog(
                        getString(R.string.read_url_failed_title),
                        getString(R.string.read_local_no_local), false
                    )
                }
            })
        dataAdapter = DataAdapter(this@MainActivity, mobileDataList)
        rvData!!.layoutManager = LinearLayoutManager(this@MainActivity)
        rvData!!.adapter = dataAdapter
    }

    private fun saveRecord(year: Int, volumeByQuarterMap: HashMap<Int, Double?>) {
        //save record to the list.
        val mobileData = MobileData()
        mobileData.year = year
        mobileData.volumeByQuarterMap
        mobileDataList.add(mobileData)

        //append each record into 1 string to be saved as csv file
        val message: StringBuilder = StringBuilder("" + year)
        for (i in 1..4) {
            //retrieve value from each quarter
            if (volumeByQuarterMap[i] != null) {
                message.append(",").append(volumeByQuarterMap[i])
            } else {
                message.append("," + "-")
            }
        }
        saveToLocal(message.toString())
    }

    private fun saveToLocal(message: String) {
        //save to csv file for offline
        try {
            val buf = BufferedWriter(FileWriter(file, true))
            buf.append(message)
            buf.newLine()
            buf.flush()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun readFromLocal() {
        //read csv file from local
        try {
            Toast.makeText(this@MainActivity, getString(R.string.read_local), Toast.LENGTH_SHORT)
                .show()
            val bufferedReader =
                BufferedReader(FileReader(File(getExternalFilesDir(null), FILE_NAME)))
            bufferedReader.readLine()
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                //skip the 1st record (field names)
                val data: Array<String?> = line.split(",".toRegex()).toTypedArray()
                if (data[0] != null) {
                    val volumeByQuarterMap: HashMap<Int, Double> = HashMap()
                    for (i in 1..4) {
                        if (data[i] != "-") {
                            //dont have record for that quarter
                            volumeByQuarterMap[i] = data[i]!!.toDouble()
                        }
                    }
                    val mobileData = MobileData()
                    mobileData.year = data[0]!!.toInt()
                    mobileData.volumeByQuarterMap = volumeByQuarterMap
                    mobileDataList.add(mobileData)
                    Log.e(
                        "Read",
                        data[0].toString() + " -> " + data[1] + " -> " + data[2] + " -> " + data[3]
                                + " -> " + data[4]
                    )
                }
            }
            dataAdapter!!.notifyDataSetChanged()
            tvCount!!.text = String.format(
                getString(R.string.result_count_offline),
                java.lang.String.valueOf(mobileDataList.size)
            )
            bufferedReader.close()
        } catch (e: IOException) {
            // Unable to read csv file
            Toast.makeText(
                this@MainActivity,
                getString(R.string.read_local_no_local),
                Toast.LENGTH_LONG
            ).show()
            Log.e("Read File", "ERROR" + e.message)
        }
    }

    private fun deleteLocalFile() {
        //Delete existing csv file
        try {
            if (file!!.exists()) {
                file!!.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showWarningDialog(title: String, message: String, hasLocalFile: Boolean) {
        //prompt dialog for error handling
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
        if (hasLocalFile) {
            //there is csv file saved previously, allow user to choose yes or no
            builder.setPositiveButton(
                R.string.yes
            ) { _, _ -> readFromLocal() }
                .setNegativeButton(R.string.no,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        tvCount!!.text = getString(R.string.no_record_title)
                    })
        } else {
            //there is no csv file saved previously, user can only choose "ok".
            builder.setPositiveButton(
                R.string.ok
            ) { dialog, _ ->
                dialog.dismiss()
                tvCount!!.text = getString(R.string.no_record_title)
            }
        }
        builder.create().show()
    }

    companion object {
        private const val SERVER_DATA_URL =
            "https://data.gov.sg/api/action/datastore_search?resource_id=a807b7ab-6cad-4aa6-87d0-e283a7353a0f"

        //            private final static String SERVER_DATA_URL = "https://data.gov.sg/api/action/datastore_search?resource_id=a807b7ab-6cad-4aa6-87d0-e283a7353a0f&q=jones";
        private const val FILE_NAME = "sph_records.csv"
    }
}