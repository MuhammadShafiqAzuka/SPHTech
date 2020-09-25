package com.azuka.mainactivity.data

import java.util.*

class MobileData {
    //recording the year
    var year = 0

    //recording the volume for each quarter with 1, 2, 3, 4 as key and its volume as value
    var volumeByQuarterMap = HashMap<Int, Double>()
        set(volumeByQuarterMap) {
            this.volumeByQuarterMap.putAll(volumeByQuarterMap)
        }
}