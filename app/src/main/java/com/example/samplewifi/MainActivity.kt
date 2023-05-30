package com.example.samplewifi

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.TrafficStats
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.security.NetworkSecurityPolicy
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.samplewifi.utils.ConnectionClassStateChangeListener
import com.example.samplewifi.utils.NetworkStatsHelper
import com.example.samplewifi.utils.PackageManagerHelper
import com.example.samplewifi.utils.TrafficStatsHelper
import com.facebook.network.connectionclass.ConnectionClassManager
import com.facebook.network.connectionclass.ConnectionQuality
import com.facebook.network.connectionclass.DeviceBandwidthSampler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            if (isGranted.values.all { true }) {
                /* checkAndRequestUsageStatsPermission()
                 checkOneDay()
                 check1DayBelow29()*/
                // checkAndRequestUsageStatsPermission()
                //getAppDataUsage(packageName)
                val networkStatsManager =
                    getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
                val uid = PackageManagerHelper.getPackageUid(this, packageName)
                val networkStatsHelper = NetworkStatsHelper(networkStatsManager, uid)
                val traffic = TrafficStatsHelper()
                Log.e(
                    TAG,
                    "getAllRxBytesMobile ${ TrafficStatsHelper.humanReadableByteCountSI(networkStatsHelper.getAllRxBytesMobile(this))} | ${TrafficStatsHelper.getAllRxBytesMobile()}",
                )
                Log.e(
                    TAG,
                    "getAllTxBytesMobile ${networkStatsHelper.getAllTxBytesMobile(this)} | ${TrafficStatsHelper.getAllTxBytesMobile()}",
                )
                Log.e(
                    TAG,
                    "allRxBytesWifi ${TrafficStatsHelper.humanReadableByteCountSI(networkStatsHelper.allRxBytesWifi)} | ${TrafficStatsHelper.getAllRxBytesWifi()}",
                )
                Log.e(
                    TAG,
                    "allTxBytesWifi ${TrafficStatsHelper.humanReadableByteCountSI(networkStatsHelper.allTxBytesWifi)} | ${TrafficStatsHelper.getAllTxBytesWifi()}",
                )
                Log.e(
                    TAG,
                    "getPackageRxBytesMobile ${networkStatsHelper.getPackageRxBytesMobile(this)} | ${
                        TrafficStatsHelper.getPackageRxBytes(uid)
                    }",
                )
                Log.e(
                    TAG,
                    "getPackageTxBytesMobile ${networkStatsHelper.getPackageTxBytesMobile(this)} | ${
                        TrafficStatsHelper.getPackageTxBytes(uid)
                    }",
                )
                Log.e(TAG, "packageRxBytesWifi ${networkStatsHelper.packageRxBytesWifi}")
                Log.e(TAG, "packageTxBytesWifi ${networkStatsHelper.packageTxBytesWifi}")
                Log.e(
                    TAG,
                    "TrafficStatsHelper getAllTxBytes | ${TrafficStatsHelper.getAllTxBytes()}",
                )
                Log.e(
                    TAG,
                    "TrafficStatsHelper getAllRxBytes | ${TrafficStatsHelper.getAllRxBytes()}",
                )


                val calendar = Calendar.getInstance()
                calendar.set(2023, Calendar.MAY, 30)
                Log.e(TAG, ":DAY ${calendar.get(Calendar.DAY_OF_MONTH)} ")
                GlobalScope.launch {
                    repeat(400) {
                        delay(1000)
                        val a = getNetworkUsageForDay(calendar)
                        Log.e(TAG, "getNetworkUsageForDay: ${a}")
                        withContext(Dispatchers.Main) {
                            textView.text = "${TrafficStatsHelper.humanReadableByteCountSI(a)}"
                        }
                    }
                    textView.text = "Done!}"
                }

            } else {
                // Explain to the user that the feature is unavailable because the
                // feature requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }

    private val textView: TextView by lazy { findViewById(R.id.textView) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         checkAndRequestUsageStatsPermission()
        val networkStatsManager =
            getSystemService(Context.NETWORK_STATS_SERVICE) as android.app.usage.NetworkStatsManager
       /* networkStatsManager.registerUsageCallback(ConnectivityManager.TYPE_WIFI,null,100,object :NetworkStatsManager.UsageCallback(){
            override fun onThresholdReached(p0: Int, p1: String?) {
                Log.e(TAG, "onThresholdReached: ${p0} - ${p1}", )
            }
        })*/
    }

    fun calculateNetworkUsageInOneHour() {
        GlobalScope.launch {
            val startTxBytes = TrafficStats.getTotalTxBytes()
            val startRxBytes = TrafficStats.getTotalRxBytes()
            val startTime = System.currentTimeMillis()

            // Wait for 1 hour
            repeat(300) {
                delay(1000)

                val endTxBytes = TrafficStats.getTotalTxBytes()
                val endRxBytes = TrafficStats.getTotalRxBytes()
                val endTime = System.currentTimeMillis()

                val txBytesPerHour = (endTxBytes - startTxBytes)
                val rxBytesPerHour = (endRxBytes - startRxBytes)
                withContext(Dispatchers.Main) {
                    textView.text = "Tran $txBytesPerHour | receive $rxBytesPerHour"
                }
                Log.d(TAG, "Transmitted bytes per hour: $txBytesPerHour")
                Log.d(TAG, "Received bytes per hour: $rxBytesPerHour")
            }
        }
    }

    private fun checkAndRequestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        } else {
            // Tiếp tục thực hiện lấy thông tin lưu lượng mạng
            //fetchNetworkStats()
           // AppNetworkUsageHelper.getAppNetworkUsage(applicationContext, "")
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.PACKAGE_USAGE_STATS,
                    android.Manifest.permission.PACKAGE_USAGE_STATS,
                    android.Manifest.permission.READ_PHONE_STATE,
                )
            )
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }


    fun getAppDataUsage(packageName: String) {
        val uid = android.os.Process.myUid()


        val totalRxBytes = TrafficStats.getUidRxBytes(uid)
        val totalTxBytes = TrafficStats.getUidTxBytes(uid)
        val getTotalTxBytes = TrafficStats.getTotalTxBytes()
        val getTotalRxBytes = TrafficStats.getTotalRxBytes()
        val getMobileTxBytes = TrafficStats.getMobileTxBytes()


        if (totalRxBytes.toInt() == TrafficStats.UNSUPPORTED || totalTxBytes.toInt() == TrafficStats.UNSUPPORTED) {
            Log.e(TAG, "TrafficStats is unsupported on this device")
            return
        }

        Log.d(TAG, "Total received bytes: $totalRxBytes")
        Log.d(TAG, "Total transmitted bytes: $totalTxBytes")
        Log.d(TAG, "Total getTotalTxBytes: $getTotalTxBytes")
        Log.d(TAG, "Total getTotalRxBytes: $getTotalRxBytes")
        Log.d(TAG, "Total getMobileTxBytes: $getMobileTxBytes")
    }


    private fun fetchNetworkStats() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork

        val networkStatsManager =
            getSystemService(Context.NETWORK_STATS_SERVICE) as android.app.usage.NetworkStatsManager
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (network != null) {
            val subscriberId: String? = telephonyManager.subscriberId

            val networkStats = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                subscriberId,
                0,
                System.currentTimeMillis()
            )


            val rxBytes = networkStats.rxBytes
            val txBytes = networkStats.txBytes

            Log.e(TAG, "fetchNetworkSt ${rxBytes}: ")
            Log.e(TAG, "fetchNetworkSt ${txBytes}: ")
        }
    }

    fun checkOneDay() {
        val context = applicationContext
        val networkStatsManager =
            getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkType = ConnectivityManager.TYPE_MOBILE_HIPRI // Loại mạng di động
        val subscriberId = telephonyManager.subscriberId // ID thuê bao
        val startTime =
            System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Thời điểm bắt đầu (1 ngày trước)
        val endTime = System.currentTimeMillis() // Thời điểm kết thúc (hiện tại)

        val bucket = networkStatsManager.querySummaryForDevice(
            networkType,
            subscriberId,
            startTime,
            endTime
        )

        val rxBytes = bucket.rxBytes // Lưu lượng dữ liệu đã nhận (received)
        val txBytes = bucket.txBytes // Lưu lượng dữ liệu đã gửi (transmitted)

        Log.d("NetworkStats", "Received bytes: $rxBytes")
        Log.d("NetworkStats", "Transmitted bytes: $txBytes")
    }

    fun check1DayBelow29() {
        val startTime =
            System.currentTimeMillis() - (24 * 60 * 60 * 1000) // Thời điểm bắt đầu (1 ngày trước)
        val endTime = System.currentTimeMillis() // Thời điểm kết thúc (hiện tại)

        val rxBytes: Long
        val txBytes: Long

        rxBytes = TrafficStats.getMobileRxBytes() // Lưu lượng dữ liệu đã nhận (received)
        txBytes = TrafficStats.getMobileTxBytes() // Lưu lượng dữ liệu đã gửi (transmitted)

        Log.d("NetworkStats", "Received bytes: $rxBytes")
        Log.d("NetworkStats", "Transmitted bytes: $txBytes")
    }

    fun shareP2P() {
        val wifiP2pManager = getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val channel = wifiP2pManager.initialize(this, mainLooper, null)


// Tìm kiếm thiết bị P2P khác trong phạm vi
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Tìm kiếm thành công
                // Chờ các thiết bị P2P khác kết nối và yêu cầu chia sẻ mạng
            }

            override fun onFailure(reason: Int) {
                // Tìm kiếm thất bại
            }
        })

// Callback khi có thiết bị P2P khác yêu cầu chia sẻ mạng
        val wifiP2pReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION) {
                    // Kiểm tra xem thiết bị khác đã kết nối hay không
                    val networkInfo =
                        intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
                }
            }
        }
    }

    private val ssid = "MyWiFiNetwork" // Tên SSID của điểm truy cập
    private val password = "MyPassword"
    private fun createPersonalHotspot() {
        val wifiManager =
            applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        // Kiểm tra nếu phiên bản Android hiện tại có hỗ trợ tạo điểm truy cập cá nhân
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val wifiConfiguration = WifiConfiguration().apply {
                allowedAuthAlgorithms.clear()
                allowedGroupCiphers.clear()
                allowedKeyManagement.clear()
                allowedPairwiseCiphers.clear()
                allowedProtocols.clear()
                SSID = ssid
                preSharedKey = password
                allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
            }
            wifiManager.isWifiEnabled = false // Tắt Wi-Fi trước khi tạo điểm truy cập cá nhân
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.NEARBY_WIFI_DEVICES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "no permision: ")
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            wifiManager.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback() {
                override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation) {
                    super.onStarted(reservation)
                    // Đã bắt đầu tạo điểm truy cập cá nhân
                    // Lưu trữ thông tin reservation nếu cần sử dụng sau này
                    Log.e(TAG, "onStarted: ")
                }

                override fun onStopped() {
                    super.onStopped()
                    // Đã dừng tạo điểm truy cập cá nhân
                    Log.e(TAG, "onStopped: ")
                }

                override fun onFailed(reason: Int) {
                    super.onFailed(reason)
                    // Tạo điểm truy cập cá nhân thất bại
                    Log.e(TAG, "onFailed: ")
                }
            }, null)
        } else {
            // Phiên bản Android hiện tại không hỗ trợ tạo điểm truy cập cá nhân
        }
    }


    private fun setHotspotEnabled(enabled: Boolean) {
        try {
            val wifiManager =
                applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val method = wifiManager.javaClass.getMethod(
                "setWifiApEnabled",
                WifiConfiguration::class.java,
                Boolean::class.javaPrimitiveType
            )
            val wifiConfiguration = WifiConfiguration()
            wifiConfiguration.SSID = "YourSSID" // Tên mạng Wi-Fi cá nhân
            wifiConfiguration.preSharedKey = "12345678" // Mật khẩu mạng Wi-Fi cá nhân
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
            method.invoke(wifiManager, wifiConfiguration, enabled)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setHotspotEnabled1(enabled: Boolean) {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val wifiConfiguration = WifiConfiguration()
                wifiConfiguration.SSID = "YourSSID" // Tên mạng Wi-Fi cá nhân
                wifiConfiguration.preSharedKey = "YourPassword" // Mật khẩu mạng Wi-Fi cá nhân
                wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

                val method = wifiManager.javaClass.getMethod(
                    "startSoftAp",
                    WifiConfiguration::class.java,
                    Boolean::class.javaPrimitiveType
                )
                method.invoke(wifiManager, wifiConfiguration, true)
            } else {
                // Xử lí cho phiên bản Android trước 8.0
                // (Cần sử dụng reflection để truy cập vào các phương thức không công khai)
            }
        } else {
            wifiManager.isWifiEnabled = false
        }
    }


    fun openHotspot() {
        val intent = Intent()
        intent.action = Settings.ACTION_WIFI_ADD_NETWORKS

        /* val intent = Intent(Intent.ACTION_MAIN)
         intent.component = ComponentName("com.android.settings", "com.android.settings.Settings\$TetherSettingsActivity")
         startActivity(intent)*/
        startActivity(intent)


    }

    private fun getNetworkUsageForDay(calendar: Calendar): Long {
        val startTime = getStartOfDay(calendar)
        val endTime = getEndOfDay(calendar)

        val networkType =
            ConnectivityManager.TYPE_WIFI // Hoặc sử dụng ConnectivityManager.TYPE_WIFI cho Wi-Fi
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val subscriberId = try {
            telephonyManager.subscriberId ?: null
        } catch (e: Exception) {
            null
        }
        // Thay bằng subscriberId của thiết bị

        try {
            val networkStatsManager =
                getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
            val networkStats = networkStatsManager.querySummary(
                networkType,
                subscriberId,
                startTime,
                endTime
            )


            var totalUsage = 0L

            while (networkStats.hasNextBucket()) {
                val bucket = NetworkStats.Bucket()
                networkStats.getNextBucket(bucket)
                /*Log.e(TAG, "App name             : ${getApplicationNameByUid(bucket.uid)}")
                Log.e(
                    TAG,
                    "getNetworkUsageForDay: ${TrafficStatsHelper.humanReadableByteCountSI(bucket.rxBytes)} - ${
                        TrafficStatsHelper.humanReadableByteCountSI(bucket.txBytes)
                    }"
                )*/
                totalUsage += bucket.rxBytes + bucket.txBytes
            }

            networkStats.close()

            return totalUsage

        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to query network usage", e)
        }

        return 0L
    }


    private fun getApplicationNameByUid(uid: Int): String {
        val packageManager = packageManager

        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (applicationInfo in packages) {
            if (applicationInfo.uid == uid) {
                return applicationInfo.loadLabel(packageManager).toString()
            }
        }

        return ""
    }

    private fun getStartOfDay(calendar: Calendar): Long {
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        return startOfDay.timeInMillis
    }

    private fun getEndOfDay(calendar: Calendar): Long {
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = calendar.timeInMillis
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        return endOfDay.timeInMillis
    }


}

//https://developer.android.com/guide/topics/connectivity/wifi-save-network-passpoint-config
//https://developer.android.com/reference/android/net/wifi/WifiManager#addOrUpdatePasspointConfiguration(android.net.wifi.hotspot2.PasspointConfiguration)
//https://developer.android.com/reference/android/net/wifi/WifiNetworkSuggestion.Builder#setPasspointConfig(android.net.wifi.hotspot2.PasspointConfiguration)
//https://developer.android.com/guide/topics/connectivity/wifi-suggest
// https://cs.android.com/android/platform/superproject/+/master:packages/modules/Connectivity/Tethering/common/TetheringLib/src/android/net/TetheringManager.java?hl=vi