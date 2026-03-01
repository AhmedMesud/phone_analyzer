package com.example.phone_analyzer

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.os.Build
import android.app.ActivityManager
import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build.VERSION
import android.os.StatFs
import android.os.Environment
import java.io.File
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.io.BufferedReader
import java.io.InputStreamReader
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.bluetooth.BluetoothAdapter
import android.telephony.TelephonyManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.Display
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.nfc.NfcAdapter
import android.os.Build.VERSION_CODES
import android.content.pm.PackageManager
import android.content.pm.ConfigurationInfo

class MainActivity: FlutterActivity() {
    private val CHANNEL = "com.example.phone_analyzer/device"
    private val PREFS_NAME = "app_prefs"
    private val KEY_LANGUAGE = "language_code"
    private val KEY_FIRST_LAUNCH = "first_launch"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            when (call.method) {
                "getDeviceModel" -> {
                    val deviceModel = Build.MODEL ?: "Bilinmiyor"
                    result.success(deviceModel)
                }
                "getDeviceCodeName" -> {
                    val deviceCodeName = Build.DEVICE ?: "Bilinmiyor"
                    result.success(deviceCodeName)
                }
                "getHardware" -> {
                    val hardware = Build.HARDWARE ?: "Bilinmiyor"
                    result.success(hardware)
                }
                "getBoard" -> {
                    val board = Build.BOARD ?: "Bilinmiyor"
                    result.success(board)
                }
                "getTotalRAM" -> {
                    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val memoryInfo = ActivityManager.MemoryInfo()
                    activityManager.getMemoryInfo(memoryInfo)
                    
                    val totalRAM = memoryInfo.totalMem
                    val availableRAM = memoryInfo.availMem
                    val usedRAM = totalRAM - availableRAM
                    
                    val ramInfo = HashMap<String, String>()
                    ramInfo["total"] = formatBytes(totalRAM)
                    ramInfo["available"] = formatBytes(availableRAM)
                    ramInfo["used"] = formatBytes(usedRAM)
                    ramInfo["usagePercent"] = "${((usedRAM * 100) / totalRAM).toInt()}%"
                    
                    result.success(ramInfo)
                }
                "getLanguage" -> {
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val language = prefs.getString(KEY_LANGUAGE, "en")
                    val firstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
                    val response = HashMap<String, Any>()
                    response["language"] = language ?: "en"
                    response["firstLaunch"] = firstLaunch
                    result.success(response)
                }
                "saveLanguage" -> {
                    val languageCode = call.argument<String>("languageCode") ?: "en"
                    val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putString(KEY_LANGUAGE, languageCode).putBoolean(KEY_FIRST_LAUNCH, false).apply()
                    result.success(true)
                }
                "getCameraInfo" -> {
                    val cameraInfo = getCameraDetails()
                    result.success(cameraInfo)
                }
                "getDeviceIdentity" -> {
                    val identity = getDeviceIdentity()
                    result.success(identity)
                }
                "getStorageInfo" -> {
                    val storage = getStorageInfo()
                    result.success(storage)
                }
                "getBatteryInfo" -> {
                    val battery = getBatteryInfo()
                    result.success(battery)
                }
                "getCpuInfo" -> {
                    val cpu = getCpuInfo()
                    result.success(cpu)
                }
                "getSensorInfo" -> {
                    val sensors = getSensorInfo()
                    result.success(sensors)
                }
                "getNetworkInfo" -> {
                    val network = getNetworkInfo()
                    result.success(network)
                }
                "getSoftwareInfo" -> {
                    val software = getSoftwareInfo()
                    result.success(software)
                }
                "getDisplayInfo" -> {
                    val display = getDisplayInfo()
                    result.success(display)
                }
                "getOtherFeatures" -> {
                    val features = getOtherFeatures()
                    result.success(features)
                }
                "getGpuInfo" -> {
                    val gpuInfo = getGpuInfo()
                    result.success(gpuInfo)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun getCameraDetails(): Map<String, Any> {
        val cameraInfo = HashMap<String, Any>()
        
        if (VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraIds = cameraManager.cameraIdList
            
            cameraInfo["cameraCount"] = cameraIds.size
            
            var backCameraResolution = "Bilinmiyor"
            var frontCameraResolution = "Bilinmiyor"
            var hasAutofocus = false
            var hardwareLevel = "Bilinmiyor"
            
            for (cameraId in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)
                
                // Çözünürlük
                val streamConfigMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                val sizes = streamConfigMap?.getOutputSizes(android.graphics.ImageFormat.JPEG)
                
                if (sizes != null && sizes.isNotEmpty()) {
                    val maxSize = sizes.maxByOrNull { it.width * it.height }
                    if (maxSize != null) {
                        val totalPixels = maxSize.width * maxSize.height
                        val megapixels = totalPixels / 1_000_000.0
                        // Örn: 12.0 MP veya 48.0 MP
                        val resolution = "%.1f MP".format(megapixels)
                        
                        when (lensFacing) {
                            CameraCharacteristics.LENS_FACING_BACK -> backCameraResolution = resolution
                            CameraCharacteristics.LENS_FACING_FRONT -> frontCameraResolution = resolution
                        }
                    }
                }
                
                // Otofokus desteği (arka kamera için kontrol et)
                if (lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                    hasAutofocus = afModes != null && afModes.size > 1
                    
                    // Donanım seviyesi
                    val hwLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    hardwareLevel = when (hwLevel) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full (Profesyonel)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited (Sınırlı)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy (Eski)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level 3 (Gelişmiş)"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL -> "External (Harici)"
                        else -> "Bilinmiyor"
                    }
                }
            }
            
            cameraInfo["backCameraResolution"] = backCameraResolution
            cameraInfo["frontCameraResolution"] = frontCameraResolution
            cameraInfo["hasAutofocus"] = hasAutofocus
            cameraInfo["hardwareLevel"] = hardwareLevel
        } else {
            cameraInfo["cameraCount"] = 0
            cameraInfo["backCameraResolution"] = "API 21+ Gerekli"
            cameraInfo["frontCameraResolution"] = "API 21+ Gerekli"
            cameraInfo["hasAutofocus"] = false
            cameraInfo["hardwareLevel"] = "Bilinmiyor"
        }
        
        return cameraInfo
    }

    private fun getDeviceIdentity(): Map<String, String> {
        val identity = HashMap<String, String>()
        
        // Üretici
        identity["manufacturer"] = Build.MANUFACTURER?.replaceFirstChar { it.uppercase() } ?: "Bilinmiyor"
        
        // Marka
        identity["brand"] = Build.BRAND?.replaceFirstChar { it.uppercase() } ?: "Bilinmiyor"
        
        // Ürün kodu
        identity["product"] = Build.PRODUCT ?: "Bilinmiyor"
        
        // Seri numarası (Android 8.0+ sınırlı)
        identity["serial"] = try {
            if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                Build.SERIAL ?: "Bilinmiyor"
            }
        } catch (e: Exception) {
            "Erişim Yok"
        }
        
        // Bootloader versiyonu
        identity["bootloader"] = Build.BOOTLOADER ?: "Bilinmiyor"
        
        // Radyo/Baseband versiyonu
        identity["radioVersion"] = Build.getRadioVersion() ?: "Bilinmiyor"
        
        return identity
    }

    private fun getStorageInfo(): Map<String, Any> {
        val storage = HashMap<String, Any>()
        
        // Dahili depolama
        val internalPath = Environment.getDataDirectory().path
        val internalStat = StatFs(internalPath)
        
        val totalInternal = internalStat.totalBytes
        val availableInternal = internalStat.availableBytes
        val usedInternal = totalInternal - availableInternal
        
        storage["totalStorage"] = formatStorageSize(totalInternal)
        storage["availableStorage"] = formatStorageSize(availableInternal)
        storage["usedStorage"] = formatStorageSize(usedInternal)
        
        // SD Kart kontrolü
        val sdCardPath = Environment.getExternalStorageDirectory().path
        if (sdCardPath != internalPath) {
            try {
                val sdStat = StatFs(sdCardPath)
                val totalSD = sdStat.totalBytes
                storage["sdCardTotal"] = formatStorageSize(totalSD)
                storage["hasSDCard"] = true
            } catch (e: Exception) {
                storage["hasSDCard"] = false
                storage["sdCardTotal"] = "Yok"
            }
        } else {
            storage["hasSDCard"] = false
            storage["sdCardTotal"] = "Yok"
        }
        
        return storage
    }

    private fun formatStorageSize(bytes: Long): String {
        val gb = bytes / (1024 * 1024 * 1024)
        return if (gb >= 1) {
            "${gb} GB"
        } else {
            val mb = bytes / (1024 * 1024)
            "${mb} MB"
        }
    }

    private fun formatBytes(bytes: Long): String {
        val gb = bytes / (1024 * 1024 * 1024)
        return if (gb >= 1) {
            "${gb} GB"
        } else {
            val mb = bytes / (1024 * 1024)
            "${mb} MB"
        }
    }

    private fun getBatteryInfo(): Map<String, Any> {
        val batteryInfo = HashMap<String, Any>()
        
        val batteryIntent = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        
        if (batteryIntent != null) {
            // Sıcaklık (onuncu derece cinsinden, örn: 285 = 28.5°C)
            val temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            batteryInfo["temperature"] = "${temperature / 10.0}°C"
            
            // Voltaj (mV cinsinden, örn: 4201 = 4.2V)
            val voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
            batteryInfo["voltage"] = "${voltage / 1000.0}V"
            
            // Teknoloji (Li-ion, Li-Po)
            val technology = batteryIntent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Bilinmiyor"
            batteryInfo["technology"] = technology
            
            // Şarj durumu
            val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val chargingStatus = when (status) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Şarj Oluyor"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Deşarj Oluyor"
                BatteryManager.BATTERY_STATUS_FULL -> "Dolu"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Şarj Olmuyor"
                else -> "Bilinmiyor"
            }
            batteryInfo["chargingStatus"] = chargingStatus
            
            // Pil yüzdesi
            val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct = if (level >= 0 && scale > 0) {
                (level * 100 / scale)
            } else {
                -1
            }
            batteryInfo["level"] = "$batteryPct%"
            
            // Şarj tipi (USB, AC, Kablosuz)
            val chargePlug = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val chargeType = when (chargePlug) {
                BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                BatteryManager.BATTERY_PLUGGED_AC -> "AC (Priz)"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Kablosuz"
                else -> if (status == BatteryManager.BATTERY_STATUS_CHARGING) "Şarj Oluyor" else "Prizde Değil"
            }
            batteryInfo["chargeType"] = chargeType
            batteryInfo["isWirelessCharging"] = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS
        } else {
            batteryInfo["temperature"] = "Bilinmiyor"
            batteryInfo["voltage"] = "Bilinmiyor"
            batteryInfo["technology"] = "Bilinmiyor"
            batteryInfo["chargingStatus"] = "Bilinmiyor"
            batteryInfo["level"] = "Bilinmiyor"
            batteryInfo["chargeType"] = "Bilinmiyor"
            batteryInfo["isWirelessCharging"] = false
        }
        
        return batteryInfo
    }

    private fun getCpuInfo(): Map<String, String> {
        val cpuInfo = HashMap<String, String>()
        
        try {
            // /proc/cpuinfo dosyasını oku
            val process = Runtime.getRuntime().exec("cat /proc/cpuinfo")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            var processorCount = 0
            var cpuImplementer = ""
            var cpuArchitecture = ""
            var cpuVariant = ""
            var cpuPart = ""
            var cpuRevision = ""
            var hardware = ""
            var serial = ""
            
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    when {
                        it.startsWith("processor") -> processorCount++
                        it.contains("CPU implementer") -> cpuImplementer = it.substringAfter(":").trim()
                        it.contains("CPU architecture") -> cpuArchitecture = it.substringAfter(":").trim()
                        it.contains("CPU variant") -> cpuVariant = it.substringAfter(":").trim()
                        it.contains("CPU part") -> cpuPart = it.substringAfter(":").trim()
                        it.contains("CPU revision") -> cpuRevision = it.substringAfter(":").trim()
                        it.contains("Hardware") -> hardware = it.substringAfter(":").trim()
                        it.contains("Serial") -> serial = it.substringAfter(":").trim()
                        else -> { /* Diğer satırları yoksay */ }
                    }
                }
            }
            reader.close()
            
            // Çekirdek sayısı
            cpuInfo["coreCount"] = if (processorCount > 0) "$processorCount" else Runtime.getRuntime().availableProcessors().toString()
            
            // CPU Mimarisi
            val arch = when (cpuArchitecture) {
                "8" -> "ARM64 (ARMv8)"
                "7" -> "ARMv7"
                "6" -> "ARMv6"
                else -> System.getProperty("os.arch") ?: "Bilinmiyor"
            }
            cpuInfo["architecture"] = arch
            
            // CPU İşlemci bilgisi (Örn: Cortex-A53, Cortex-A76)
            val partName = when (cpuPart) {
                "0xd03" -> "Cortex-A53"
                "0xd04" -> "Cortex-A35"
                "0xd05" -> "Cortex-A55"
                "0xd06" -> "Cortex-A65"
                "0xd07" -> "Cortex-A57"
                "0xd08" -> "Cortex-A72"
                "0xd09" -> "Cortex-A73"
                "0xd0a" -> "Cortex-A75"
                "0xd0b" -> "Cortex-A76"
                "0xd0c" -> "Cortex-A77"
                "0xd0d" -> "Cortex-A78"
                "0xd0e" -> "Cortex-A510"
                "0xd0f" -> "Cortex-A710"
                "0xd44" -> "Cortex-X1"
                "0xd4a" -> "Cortex-X2"
                "0xd4b" -> "Cortex-X3"
                "0xd4d" -> "Cortex-A715"
                "0xd4e" -> "Cortex-X4"
                else -> cpuPart
            }
            cpuInfo["cpuPart"] = if (partName.isNotEmpty()) partName else "Bilinmiyor"
            
            // GPU bilgisi (hardware alanından)
            cpuInfo["gpuInfo"] = if (hardware.isNotEmpty() && hardware != "Unknown") hardware else Build.HARDWARE ?: "Bilinmiyor"
            
            // Frekans bilgisi (Her çekirdek için max frekans)
            val maxFreqs = mutableListOf<String>()
            for (i in 0 until Runtime.getRuntime().availableProcessors()) {
                try {
                    val freqFile = File("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq")
                    if (freqFile.exists()) {
                        val freq = freqFile.readText().trim().toLong()
                        val freqGHz = freq / 1000000.0
                        if (freqGHz > 0 && !maxFreqs.contains(String.format("%.1f GHz", freqGHz))) {
                            maxFreqs.add(String.format("%.1f GHz", freqGHz))
                        }
                    }
                } catch (e: Exception) {
                    // Hata durumunda yoksay
                }
            }
            cpuInfo["maxFrequencies"] = if (maxFreqs.isNotEmpty()) maxFreqs.joinToString(", ") else "Bilinmiyor"
            
        } catch (e: Exception) {
            cpuInfo["coreCount"] = Runtime.getRuntime().availableProcessors().toString()
            cpuInfo["architecture"] = System.getProperty("os.arch") ?: "Bilinmiyor"
            cpuInfo["cpuPart"] = "Bilinmiyor"
            cpuInfo["gpuInfo"] = Build.HARDWARE ?: "Bilinmiyor"
            cpuInfo["maxFrequencies"] = "Bilinmiyor"
        }
        
        return cpuInfo
    }

    private fun getSensorInfo(): Map<String, Any> {
        val sensorInfo = HashMap<String, Any>()
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // Parmak izi sensörü (Android 6.0+)
        val hasFingerprint = if (VERSION.SDK_INT >= VERSION_CODES.M) {
            val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as? android.hardware.fingerprint.FingerprintManager
            fingerprintManager?.isHardwareDetected ?: false
        } else {
            false
        }
        sensorInfo["hasFingerprint"] = hasFingerprint
        sensorInfo["fingerprintLocation"] = if (hasFingerprint) "Arka / Ekran / Yan" else "Yok"
        
        // Yüz tanıma - FaceManager API'si sınırlı kullanılabilirlikte
        // Şimdilik parmak izi sensörü varsa ve Android 10+ ise tahminde bulun
        val hasFaceRecognition = if (VERSION.SDK_INT >= VERSION_CODES.Q && hasFingerprint) {
            // Gerçek yüz tanıma kontrolü yerine tahminde bulun
            // Samsung, Google Pixel gibi cihazlarda genelde yüz tanıma da vardır
            val model = Build.MODEL?.lowercase() ?: ""
            val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
            model.contains("pixel") || 
            model.contains("samsung") && (model.contains("s21") || model.contains("s22") || model.contains("s23") || model.contains("s24")) ||
            manufacturer.contains("apple")
        } else {
            false
        }
        sensorInfo["hasFaceRecognition"] = hasFaceRecognition
        
        // Yakınlık sensörü
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        sensorInfo["hasProximity"] = proximitySensor != null
        
        // Ortam ışığı sensörü
        val lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        sensorInfo["hasLightSensor"] = lightSensor != null
        
        // Barometre (basınç sensörü)
        val pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        sensorInfo["hasBarometer"] = pressureSensor != null
        
        // Termometre (sıcaklık sensörleri)
        val ambientTempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        val tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_TEMPERATURE)
        sensorInfo["hasThermometer"] = ambientTempSensor != null || tempSensor != null
        
        // İvmeölçer, Jiroskop, Pusula
        sensorInfo["hasAccelerometer"] = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
        sensorInfo["hasGyroscope"] = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
        sensorInfo["hasCompass"] = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
        
        return sensorInfo
    }

    private fun getNetworkInfo(): Map<String, String> {
        val networkInfo = HashMap<String, String>()
        
        // Wi-Fi MAC Adresi (Android 6.0+ sınırlı)
        val wifiMac = try {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.connectionInfo?.macAddress ?: "02:00:00:00:00:00 (Sınırlı)"
            } else {
                val wifiManager = getSystemService(Context.WIFI_SERVICE) as? WifiManager
                wifiManager?.connectionInfo?.macAddress ?: "Bilinmiyor"
            }
        } catch (e: Exception) {
            "Erişim Yok"
        }
        networkInfo["wifiMac"] = wifiMac
        
        // Bluetooth MAC Adresi
        val btMac = try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            bluetoothAdapter?.address ?: "Kapalı"
        } catch (e: Exception) {
            "Erişim Yok"
        }
        networkInfo["bluetoothMac"] = btMac
        
        // IMEI (TelephonyManager ile - izin gerektirir)
        val imei = try {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                // IMEI_READ izni gerekir
                telephonyManager?.imei ?: "İzin Gerekli"
            } else {
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.deviceId ?: "İzin Gerekli"
            }
        } catch (e: Exception) {
            "Erişim Yok (İzin Gerekli)"
        }
        networkInfo["imei"] = imei
        
        // IP Adresi
        val ipAddress = try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val ip = wifiManager?.connectionInfo?.ipAddress ?: 0
            String.format("%d.%d.%d.%d", 
                ip and 0xff, 
                (ip shr 8) and 0xff, 
                (ip shr 16) and 0xff, 
                (ip shr 24) and 0xff)
        } catch (e: Exception) {
            "Bilinmiyor"
        }
        networkInfo["ipAddress"] = ipAddress
        
        // Ağ Tipi
        val networkType = try {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            
            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "Wi-Fi"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                    val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                    when (telephonyManager?.networkType) {
                        TelephonyManager.NETWORK_TYPE_NR -> "5G"
                        TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
                        TelephonyManager.NETWORK_TYPE_HSPAP, 
                        TelephonyManager.NETWORK_TYPE_HSPA,
                        TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
                        else -> "Mobil Veri"
                    }
                }
                else -> "Bağlı Değil"
            }
        } catch (e: Exception) {
            "Bilinmiyor"
        }
        networkInfo["networkType"] = networkType
        
        // Wi-Fi Detayları
        val wifiInfo = try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wifiManager?.connectionInfo
        } catch (e: Exception) {
            null
        }
        
        // Bağlantı Hızı (Link Speed)
        val linkSpeed = wifiInfo?.linkSpeed?.toString() ?: "Bilinmiyor"
        networkInfo["wifiLinkSpeed"] = if (linkSpeed != "Bilinmiyor") "$linkSpeed Mbps" else linkSpeed
        
        // Sinyal Gücü (RSSI)
        val rssi = wifiInfo?.rssi?.toString() ?: "Bilinmiyor"
        networkInfo["wifiSignalStrength"] = rssi
        
        // Sinyal seviyesi açıklaması
        val signalLevel = wifiInfo?.rssi?.let {
            when {
                it >= -50 -> "Mükemmel"
                it >= -60 -> "Çok İyi"
                it >= -70 -> "İyi"
                it >= -80 -> "Orta"
                else -> "Zayıf"
            }
        } ?: "Bilinmiyor"
        networkInfo["wifiSignalQuality"] = signalLevel
        
        // Wi-Fi Frekansı (2.4 GHz / 5 GHz)
        val frequency = wifiInfo?.frequency?.toString() ?: "Bilinmiyor"
        networkInfo["wifiFrequency"] = if (frequency != "Bilinmiyor") "$frequency MHz" else frequency
        
        // Wi-Fi Standardı (802.11)
        val wifiStandard = wifiInfo?.let {
            // LinkSpeed ve frequency'den tahmin
            val speed = it.linkSpeed
            when {
                speed >= 1000 -> "Wi-Fi 6 (802.11ax)"
                speed >= 600 -> "Wi-Fi 5 (802.11ac)"
                speed >= 300 -> "Wi-Fi 4 (802.11n)"
                else -> "802.11g/a"
            }
        } ?: "Bilinmiyor"
        networkInfo["wifiStandard"] = wifiStandard
        
        return networkInfo
    }

    private fun getSoftwareInfo(): Map<String, String> {
        val softwareInfo = HashMap<String, String>()
        
        // Android Güvenlik Yaması Tarihi
        val securityPatch = if (VERSION.SDK_INT >= VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH ?: "Bilinmiyor"
        } else {
            "API 23+ Gerekli"
        }
        softwareInfo["securityPatch"] = securityPatch
        
        // Google Play Services Sürümü
        val gmsVersion = try {
            val packageInfo = packageManager.getPackageInfo("com.google.android.gms", 0)
            packageInfo?.versionName ?: "Yüklü Değil"
        } catch (e: Exception) {
            "Yüklü Değil"
        }
        softwareInfo["gmsVersion"] = gmsVersion
        
        // Kernel Versiyonu
        softwareInfo["kernelVersion"] = System.getProperty("os.version") ?: "Bilinmiyor"
        
        // Root/Custom ROM Kontrolü
        val isRooted = when {
            Build.TAGS?.contains("test-keys") == true -> "Evet (Test Keys)"
            File("/system/app/Superuser.apk").exists() -> "Evet (Superuser)"
            File("/system/xbin/su").exists() -> "Evet (SU Binary)"
            File("/system/bin/su").exists() -> "Evet (SU Binary)"
            File("/data/local/xbin/su").exists() -> "Evet (SU Binary)"
            File("/data/local/bin/su").exists() -> "Evet (SU Binary)"
            File("/sbin/su").exists() -> "Evet (SU Binary)"
            File("/su/bin/su").exists() -> "Evet (SU Binary)"
            else -> "Hayır (Stok ROM)"
        }
        softwareInfo["isRooted"] = isRooted
        
        // Android sürümü
        softwareInfo["androidVersion"] = Build.VERSION.RELEASE ?: "Bilinmiyor"
        
        return softwareInfo
    }

    private fun getDisplayInfo(): Map<String, Any> {
        val displayInfo = HashMap<String, Any>()
        
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY)
        
        if (display != null) {
            // Ekran Yenileme Hızı (Hz)
            val refreshRate = display.refreshRate
            displayInfo["refreshRate"] = "${refreshRate.toInt()} Hz"
            
            // HDR Desteği
            val hdrCapabilities = if (VERSION.SDK_INT >= VERSION_CODES.O) {
                display.hdrCapabilities
            } else {
                null
            }
            
            val hdrSupported = hdrCapabilities?.supportedHdrTypes?.isNotEmpty() ?: false
            val hdrTypes = hdrCapabilities?.supportedHdrTypes?.map {
                when (it) {
                    Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION -> "Dolby Vision"
                    Display.HdrCapabilities.HDR_TYPE_HDR10 -> "HDR10"
                    Display.HdrCapabilities.HDR_TYPE_HLG -> "HLG"
                    Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS -> "HDR10+"
                    else -> "Bilinmiyor"
                }
            }?.joinToString(", ") ?: "Desteklenmiyor"
            
            displayInfo["hdrSupport"] = if (hdrSupported) hdrTypes else "Desteklenmiyor"
            
            // Always-on Display desteği (Aktivite tarafında kontrol edilir)
            displayInfo["alwaysOnDisplay"] = if (VERSION.SDK_INT >= VERSION_CODES.O) "Kontrol Ediliyor" else "Desteklenmiyor"
            
        } else {
            displayInfo["refreshRate"] = "Bilinmiyor"
            displayInfo["hdrSupport"] = "Bilinmiyor"
            displayInfo["alwaysOnDisplay"] = "Bilinmiyor"
        }
        
        // Ekran teknolojisi (Build bilgisinden tahmin)
        val panelType = when {
            Build.BOARD?.contains("amoled", ignoreCase = true) == true -> "AMOLED"
            Build.BOARD?.contains("oled", ignoreCase = true) == true -> "OLED"
            Build.HARDWARE?.contains("qcom", ignoreCase = true) == true -> "LCD/AMOLED (Qualcomm)"
            Build.MANUFACTURER?.contains("samsung", ignoreCase = true) == true -> "AMOLED (Samsung)"
            else -> "LCD (Tahmin)"
        }
        displayInfo["panelType"] = panelType
        
        return displayInfo
    }

    private fun getOtherFeatures(): Map<String, Any> {
        val features = HashMap<String, Any>()
        val packageManager = packageManager
        
        // Stereo Hoparlör kontrolü
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val isStereo = audioManager?.let {
            // Basit kontrol - gerçek stereo testi için ses çıkış analizi gerekir
            it.getDevices(AudioManager.GET_DEVICES_OUTPUTS).size > 1
        } ?: false
        features["hasStereoSpeakers"] = isStereo
        
        // NFC Desteği
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        features["hasNFC"] = nfcAdapter != null
        features["nfcEnabled"] = nfcAdapter?.isEnabled ?: false
        
        // IR Blaster (Kızılötesi)
        val hasIR = packageManager.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)
        features["hasIRBlaster"] = hasIR
        
        // Su/Darbe Dayanıklılık - Build modelden tahmin
        val ipRating = when {
            Build.MODEL?.contains("samsung", ignoreCase = true) == true && 
            (Build.MODEL?.contains("s21", ignoreCase = true) == true || 
             Build.MODEL?.contains("s22", ignoreCase = true) == true ||
             Build.MODEL?.contains("s23", ignoreCase = true) == true ||
             Build.MODEL?.contains("s24", ignoreCase = true) == true) -> "IP68"
            
            Build.MODEL?.contains("pixel", ignoreCase = true) == true && 
            (Build.MODEL?.contains("6", ignoreCase = true) == true || 
             Build.MODEL?.contains("7", ignoreCase = true) == true ||
             Build.MODEL?.contains("8", ignoreCase = true) == true) -> "IP68"
            
            Build.MANUFACTURER?.contains("sony", ignoreCase = true) == true -> "IP65/IP68"
            
            else -> "Bilinmiyor (Üretici Dokümantasyonu)"
        }
        features["ipRating"] = ipRating
        
        // Fast Charging desteği (tahmin)
        features["fastCharging"] = "Cihaz Bağımlı (Üretici Dokümantasyonu)"
        
        return features
    }

    private fun getGpuInfo(): Map<String, String> {
        val gpuInfo = HashMap<String, String>()
        
        // OpenGL ES sürümünü al
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configInfo = activityManager.deviceConfigurationInfo
        
        // OpenGL ES sürümü
        val glEsVersion = configInfo?.glEsVersion ?: "Bilinmiyor"
        gpuInfo["openGlEsVersion"] = glEsVersion
        
        // GPU Modeli - Build.HARDWARE'dan tahmin
        val gpuModel = when {
            Build.HARDWARE.contains("qcom", ignoreCase = true) || 
            Build.HARDWARE.contains("sm", ignoreCase = true) ||
            Build.HARDWARE.contains("sdm", ignoreCase = true) -> "Adreno (Qualcomm)"
            
            Build.HARDWARE.contains("exynos", ignoreCase = true) ||
            Build.HARDWARE.contains("universal", ignoreCase = true) -> "Mali (Samsung Exynos)"
            
            Build.HARDWARE.contains("mt", ignoreCase = true) ||
            Build.HARDWARE.contains("mediatek", ignoreCase = true) -> "Mali (MediaTek)"
            
            Build.HARDWARE.contains("kirin", ignoreCase = true) ||
            Build.HARDWARE.contains("hi", ignoreCase = true) -> "Mali (HiSilicon)"
            
            Build.HARDWARE.contains("apple", ignoreCase = true) -> "Apple GPU"
            
            Build.HARDWARE.contains("mali", ignoreCase = true) -> "Mali (ARM)"
            
            Build.HARDWARE.contains("powervr", ignoreCase = true) ||
            Build.HARDWARE.contains("sgx", ignoreCase = true) -> "PowerVR"
            
            else -> ""
        }
        gpuInfo["gpuModel"] = gpuModel
        
        // Build.HARDWARE değeri (ham)
        gpuInfo["hardware"] = Build.HARDWARE ?: "Bilinmiyor"
        
        return gpuInfo
    }
}
