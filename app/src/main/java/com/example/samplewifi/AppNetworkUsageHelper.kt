package com.example.samplewifi

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.RemoteException
import android.telephony.TelephonyManager
import android.util.Log
import java.time.Instant

object AppNetworkUsageHelper {
    private const val TAG = "AppNetworkUsageHelper"

    fun getAppNetworkUsage(context: Context, packageName: String) {
        val networkStatsManager =
            context.getSystemService(Context.NETWORK_STATS_SERVICE) as? NetworkStatsManager
        if (networkStatsManager == null) {
            Log.e(TAG, "Failed to get NetworkStatsManager")
            return
        }

       // val subscriberId = getSubscriberId(context)

        val endTime = System.currentTimeMillis()
        val startTime = 0L // Lấy dữ liệu trong 1 giờ trước

        try {
            val networkType = getNetworkType(context)

            val networkStats = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_WIFI,
                null,
                startTime,
                endTime
            )


            var totalRxBytes = networkStats.rxBytes
            var totalTxBytes = networkStats.txPackets

           /* while (networkStats.hasNextBucket()) {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)

                totalRxBytes += bucket.rxBytes
                totalTxBytes += bucket.txBytes
            }

            networkStats.close()*/

            Log.d(TAG, "Total received bytes: $totalRxBytes")
            Log.d(TAG, "Total transmitted bytes: $totalTxBytes")

        } catch (e: RemoteException) {
            Log.e(TAG, "Failed to query network usage", e)
        }
    }

    private fun getSubscriberId(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return telephonyManager?.subscriberId ?: ""
    }

    private fun getNetworkType(context: Context): Int {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        return connectivityManager?.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)?.run {
                when {
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        ConnectivityManager.TYPE_ETHERNET
                    }
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        ConnectivityManager.TYPE_WIFI
                    }
                    else -> {
                        -1
                    }
                }
            }
        } ?: -1
    }
}
