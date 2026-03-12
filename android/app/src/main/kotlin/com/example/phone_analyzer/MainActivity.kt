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
import android.os.PowerManager

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
                    val hardware = getHardwareName(Build.HARDWARE)
                    result.success(hardware)
                }
                "getBoard" -> {
                    val board = getBoardName(Build.BOARD)
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
            try {
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                
                // Kamera ID listesini al
                val cameraIds = try {
                    cameraManager.cameraIdList
                } catch (e: Exception) {
                    // Kamera izni yoksa veya erişilemiyorsa boş dizi
                    emptyArray<String>()
                }
                
                cameraInfo["cameraCount"] = cameraIds.size
                
                var backCameraResolution = "Bilinmiyor"
                var frontCameraResolution = "Bilinmiyor"
                var hasAutofocus = false
                var hardwareLevel = "Bilinmiyor"
                var backCameraFound = false
                var frontCameraFound = false
                
                if (cameraIds.isEmpty()) {
                    cameraInfo["backCameraResolution"] = "Kamera Erişim Yok"
                    cameraInfo["frontCameraResolution"] = "Kamera Erişim Yok"
                    cameraInfo["hasAutofocus"] = false
                    cameraInfo["hardwareLevel"] = "Bilinmiyor"
                    return cameraInfo
                }
                
                for (cameraId in cameraIds) {
                    try {
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
                                val resolution = "%.1f MP".format(megapixels)
                                
                                when (lensFacing) {
                                    CameraCharacteristics.LENS_FACING_BACK -> {
                                        if (!backCameraFound) {
                                            backCameraResolution = resolution
                                            backCameraFound = true
                                            
                                            // Arka kamera için AF ve donanım seviyesi
                                            val afModes = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                                            hasAutofocus = afModes != null && afModes.any { 
                                                it != CameraCharacteristics.CONTROL_AF_MODE_OFF 
                                            }
                                            
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
                                    CameraCharacteristics.LENS_FACING_FRONT -> {
                                        if (!frontCameraFound) {
                                            frontCameraResolution = resolution
                                            frontCameraFound = true
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Tek kamera hatası, diğerlerine devam et
                    }
                }
                
                cameraInfo["backCameraResolution"] = backCameraResolution
                cameraInfo["frontCameraResolution"] = frontCameraResolution
                cameraInfo["hasAutofocus"] = hasAutofocus
                cameraInfo["hardwareLevel"] = hardwareLevel
            } catch (e: Exception) {
                cameraInfo["cameraCount"] = 0
                cameraInfo["backCameraResolution"] = "Kamera Erişim Hatası"
                cameraInfo["frontCameraResolution"] = "Kamera Erişim Hatası"
                cameraInfo["hasAutofocus"] = false
                cameraInfo["hardwareLevel"] = "Bilinmiyor"
            }
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

    // Anakart (Board) kodunu okunabilir isme çevir
    private fun getBoardName(boardCode: String?): String {
        if (boardCode == null) return "Bilinmiyor"
        
        // Samsung Board Kodları
        val boardMap = mapOf(
            // Samsung Exynos
            "s5e9925" to "Samsung Exynos 2200",
            "s5e8835" to "Samsung Exynos 1280",
            "s5e5515" to "Samsung Exynos 850",
            "universal2100" to "Samsung Exynos 2100",
            "universal2200" to "Samsung Exynos 2200",
            "universal2100_hdk" to "Samsung Exynos 2100 (HDR)",
            "kona" to "Qualcomm Snapdragon 865",
            "lahaina" to "Qualcomm Snapdragon 888",
            "taro" to "Qualcomm Snapdragon 8 Gen 1",
            "kalama" to "Qualcomm Snapdragon 8 Gen 2",
            "pineapple" to "Qualcomm Snapdragon 8 Gen 3",
            "sm6150" to "Qualcomm Snapdragon 675",
            "sm6125" to "Qualcomm Snapdragon 665",
            "sm8150" to "Qualcomm Snapdragon 855",
            "sm8250" to "Qualcomm Snapdragon 865",
            "sm8350" to "Qualcomm Snapdragon 888",
            "sm8450" to "Qualcomm Snapdragon 8 Gen 1",
            "sm8550" to "Qualcomm Snapdragon 8 Gen 2",
            "sm8650" to "Qualcomm Snapdragon 8 Gen 3",
            "bengal" to "Qualcomm Snapdragon 662",
            "atoll" to "Qualcomm Snapdragon 720G",
            "lito" to "Qualcomm Snapdragon 765G/768G",
            "mona" to "Qualcomm Snapdragon 778G",
            "yupik" to "Qualcomm Snapdragon 695",
            "holi" to "Qualcomm Snapdragon 690",
            "bengal_32" to "Qualcomm Snapdragon 662",
            "bengal_64" to "Qualcomm Snapdragon 662",
            "mt6785" to "MediaTek Helio G90T",
            "mt6789" to "MediaTek Helio G96",
            "mt6833" to "MediaTek Dimensity 700",
            "mt6853" to "MediaTek Dimensity 720/800U",
            "mt6873" to "MediaTek Dimensity 800",
            "mt6877" to "MediaTek Dimensity 900",
            "mt6885" to "MediaTek Dimensity 1000",
            "mt6889" to "MediaTek Dimensity 1100/1200",
            "mt6893" to "MediaTek Dimensity 1200",
            "mt6983" to "MediaTek Dimensity 9000",
            "mt6993" to "MediaTek Dimensity 9200",
            "gs101" to "Google Tensor",
            "gs201" to "Google Tensor G2",
            "gs301" to "Google Tensor G3",
            "t810" to "HiSilicon Kirin 950",
            "t830" to "HiSilicon Kirin 960",
            "t880" to "HiSilicon Kirin 970",
            "kirin980" to "HiSilicon Kirin 980",
            "kirin990" to "HiSilicon Kirin 990",
            "kirin9000" to "HiSilicon Kirin 9000"
        )
        
        return boardMap[boardCode.lowercase()] ?: boardCode.uppercase()
    }

    // İşlemci (Hardware) kodunu okunabilir isme çevir
    private fun getHardwareName(hardwareCode: String?): String {
        if (hardwareCode == null) return "Bilinmiyor"
        
        // Eğer kod heksadesimal formatındaysa (0x ile başlıyorsa)
        if (hardwareCode.startsWith("0x")) {
            return try {
                val hexValue = hardwareCode.removePrefix("0x").toInt(16)
                // ARM Cortex model numaralarını dene
                when (hexValue) {
                    0xd03 -> "ARM Cortex-A53"
                    0xd04 -> "ARM Cortex-A35"
                    0xd05 -> "ARM Cortex-A55"
                    0xd06 -> "ARM Cortex-A65"
                    0xd07 -> "ARM Cortex-A57"
                    0xd08 -> "ARM Cortex-A72"
                    0xd09 -> "ARM Cortex-A73"
                    0xd0a -> "ARM Cortex-A75"
                    0xd0b -> "ARM Cortex-A76"
                    0xd0c -> "ARM Cortex-A77"
                    0xd0d -> "ARM Cortex-A55 (rev2)"
                    0xd0e -> "ARM Cortex-A76AE"
                    0xd13 -> "ARM Cortex-R52"
                    0xd20 -> "ARM Cortex-M23"
                    0xd21 -> "ARM Cortex-M33"
                    0xd40 -> "ARM Neoverse N1"
                    0xd41 -> "ARM Cortex-A78"
                    0xd42 -> "ARM Cortex-A78AE"
                    0xd44 -> "ARM Cortex-X1"
                    0xd46 -> "ARM Cortex-A510"
                    0xd47 -> "ARM Cortex-A710"
                    0xd48 -> "ARM Cortex-X2"
                    0xd49 -> "ARM Cortex-X3"
                    0xd4d -> "ARM Cortex-A715"
                    0xd4e -> "ARM Cortex-X3 (rev2)"
                    else -> "ARM Cortex (0x${hardwareCode.removePrefix("0x").uppercase()})"
                }
            } catch (e: Exception) {
                hardwareCode.uppercase()
            }
        }
        
        // Diğer donanım kodları
        val hardwareMap = mapOf(
            "qcom" to "Qualcomm Snapdragon",
            "exynos" to "Samsung Exynos",
            "exynos5" to "Samsung Exynos 5",
            "exynos7" to "Samsung Exynos 7",
            "exynos9" to "Samsung Exynos 9",
            "kirin" to "HiSilicon Kirin",
            "mt6735" to "MediaTek MT6735",
            "mt6750" to "MediaTek MT6750",
            "mt6765" to "MediaTek Helio P35",
            "mt6771" to "MediaTek Helio P60",
            "mt6785" to "MediaTek Helio G90T",
            "mt6789" to "MediaTek Helio G96",
            "mt6833" to "MediaTek Dimensity 700",
            "mt6853" to "MediaTek Dimensity 720",
            "mt6873" to "MediaTek Dimensity 800",
            "mt6877" to "MediaTek Dimensity 900",
            "mt6885" to "MediaTek Dimensity 1000",
            "mt6889" to "MediaTek Dimensity 1100",
            "mt6893" to "MediaTek Dimensity 1200",
            "mt6983" to "MediaTek Dimensity 9000",
            "mt6985" to "MediaTek Dimensity 9200",
            "universal5420" to "Samsung Exynos 5420",
            "universal5430" to "Samsung Exynos 5430",
            "universal7420" to "Samsung Exynos 7420",
            "universal8890" to "Samsung Exynos 8890",
            "universal8895" to "Samsung Exynos 8895",
            "universal9810" to "Samsung Exynos 9810",
            "universal9820" to "Samsung Exynos 9820",
            "universal9825" to "Samsung Exynos 9825",
            "universal990" to "Samsung Exynos 990",
            "universal2100" to "Samsung Exynos 2100",
            "universal2200" to "Samsung Exynos 2200",
            "gs101" to "Google Tensor",
            "gs201" to "Google Tensor G2",
            "gs301" to "Google Tensor G3"
        )
        
        return hardwareMap[hardwareCode.lowercase()] ?: hardwareCode.uppercase()
    }

    // NetworkInterface'den MAC adresi al (Android 6.0+ için gerçek MAC)
    private fun getMacAddressFromNetworkInterface(interfaceName: String): String? {
        return try {
            val networkInterface = java.net.NetworkInterface.getByName(interfaceName)
            if (networkInterface != null) {
                val macBytes = networkInterface.hardwareAddress
                if (macBytes != null && macBytes.isNotEmpty()) {
                    val macBuilder = StringBuilder()
                    for (i in macBytes.indices) {
                        macBuilder.append(String.format("%02X", macBytes[i]))
                        if (i < macBytes.size - 1) {
                            macBuilder.append(":")
                        }
                    }
                    macBuilder.toString()
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Detaylı GPU Modeli belirleme
    private fun getDetailedGpuModel(): String {
        val board = Build.BOARD ?: ""
        val hardware = Build.HARDWARE ?: ""
        
        // Qualcomm Adreno GPU'lar
        when {
            // Snapdragon 8 Gen 3
            board.contains("pineapple", ignoreCase = true) ||
            hardware.contains("sm8650", ignoreCase = true) -> return "Adreno 750"
            
            // Snapdragon 8 Gen 2
            board.contains("kalama", ignoreCase = true) ||
            hardware.contains("sm8550", ignoreCase = true) -> return "Adreno 740"
            
            // Snapdragon 8 Gen 1
            board.contains("taro", ignoreCase = true) ||
            hardware.contains("sm8450", ignoreCase = true) -> return "Adreno 730"
            
            // Snapdragon 888/888+
            board.contains("lahaina", ignoreCase = true) ||
            hardware.contains("sm8350", ignoreCase = true) -> return "Adreno 660"
            
            // Snapdragon 865/865+/870
            board.contains("kona", ignoreCase = true) ||
            hardware.contains("sm8250", ignoreCase = true) ||
            hardware.contains("kona", ignoreCase = true) -> return "Adreno 650"
            
            // Snapdragon 855/855+
            hardware.contains("sm8150", ignoreCase = true) -> return "Adreno 640"
            
            // Snapdragon 778G
            hardware.contains("sm7325", ignoreCase = true) ||
            board.contains("cape", ignoreCase = true) -> return "Adreno 642L"
            
            // Snapdragon 780G
            hardware.contains("sm7350", ignoreCase = true) -> return "Adreno 642"
            
            // Snapdragon 768G
            board.contains("lito", ignoreCase = true) ||
            hardware.contains("sm7250", ignoreCase = true) -> return "Adreno 620"
            
            // Snapdragon 765G
            hardware.contains("sm7250", ignoreCase = true) ||
            board.contains("lito_v2", ignoreCase = true) -> return "Adreno 620"
            
            // Snapdragon 750G
            hardware.contains("sm7225", ignoreCase = true) -> return "Adreno 619"
            
            // Snapdragon 732G/730G/730
            hardware.contains("sm7150", ignoreCase = true) ||
            board.contains("atoll", ignoreCase = true) -> return "Adreno 618"
            
            // Snapdragon 720G
            hardware.contains("sm7125", ignoreCase = true) -> return "Adreno 618"
            
            // Snapdragon 695
            board.contains("yupik", ignoreCase = true) ||
            hardware.contains("sm6375", ignoreCase = true) -> return "Adreno 619"
            
            // Snapdragon 690
            hardware.contains("sm6350", ignoreCase = true) -> return "Adreno 619L"
            
            // Snapdragon 685
            hardware.contains("sm6225", ignoreCase = true) -> return "Adreno 610"
            
            // Snapdragon 680
            hardware.contains("sm6225", ignoreCase = true) ||
            board.contains("bengal", ignoreCase = true) -> return "Adreno 610"
            
            // Snapdragon 665/662/460
            hardware.contains("sm6125", ignoreCase = true) ||
            hardware.contains("sm6115", ignoreCase = true) ||
            hardware.contains("sm4250", ignoreCase = true) -> return "Adreno 610"
            
            // Snapdragon 480/480+
            hardware.contains("sm4350", ignoreCase = true) -> return "Adreno 619"
            
            // Snapdragon 4 Gen 1
            hardware.contains("sm4375", ignoreCase = true) -> return "Adreno 619"
            
            // Snapdragon 6 Gen 1
            hardware.contains("sm6450", ignoreCase = true) -> return "Adreno 710"
            
            // Snapdragon 7 Gen 1
            hardware.contains("sm7450", ignoreCase = true) -> return "Adreno 644"
            
            // Snapdragon 7+ Gen 2
            hardware.contains("sm7475", ignoreCase = true) -> return "Adreno 725"
        }
        
        // Samsung Exynos Mali GPU'lar
        when {
            // Exynos 2200 (AMD RDNA2)
            board.contains("s5e9925", ignoreCase = true) ||
            board.contains("universal2200", ignoreCase = true) -> return "AMD RDNA2 (Xclipse 920)"
            
            // Exynos 2100
            board.contains("s5e9920", ignoreCase = true) ||
            board.contains("universal2100", ignoreCase = true) -> return "Mali-G78 MP14"
            
            // Exynos 990
            board.contains("universal990", ignoreCase = true) -> return "Mali-G77 MP11"
            
            // Exynos 9825/9820
            board.contains("universal9825", ignoreCase = true) ||
            board.contains("universal9820", ignoreCase = true) -> return "Mali-G76 MP12"
            
            // Exynos 9810
            board.contains("universal9810", ignoreCase = true) -> return "Mali-G72 MP18"
            
            // Exynos 8895
            board.contains("universal8895", ignoreCase = true) -> return "Mali-G71 MP20"
            
            // Exynos 8890
            board.contains("universal8890", ignoreCase = true) -> return "Mali-T880 MP12"
            
            // Exynos 7420
            board.contains("universal7420", ignoreCase = true) -> return "Mali-T760 MP8"
            
            // Exynos 1280
            board.contains("s5e8825", ignoreCase = true) ||
            board.contains("universal1280", ignoreCase = true) -> return "Mali-G68"
            
            // Exynos 1330/1380
            board.contains("s5e8535", ignoreCase = true) ||
            board.contains("universal1330", ignoreCase = true) -> return "Mali-G68 MP2"
            
            // Exynos 850
            board.contains("s5e8822", ignoreCase = true) ||
            board.contains("universal850", ignoreCase = true) -> return "Mali-G52 MP1"
            
            // Exynos 9611/9610
            board.contains("universal9611", ignoreCase = true) ||
            board.contains("universal9610", ignoreCase = true) -> return "Mali-G72 MP3"
            
            // Exynos 7904/7885
            board.contains("universal7904", ignoreCase = true) ||
            board.contains("universal7885", ignoreCase = true) -> return "Mali-G71 MP2"
        }
        
        // MediaTek Mali GPU'lar
        when {
            // Dimensity 9200
            hardware.contains("mt6983", ignoreCase = true) -> return "Immortalis-G715 MC11"
            
            // Dimensity 9000/9000+
            hardware.contains("mt6983", ignoreCase = true) ||
            board.contains("mt6983", ignoreCase = true) -> return "Mali-G710 MC10"
            
            // Dimensity 8200/8100/8000
            hardware.contains("mt6895", ignoreCase = true) ||
            hardware.contains("mt6895z", ignoreCase = true) -> return "Mali-G610 MC6"
            
            // Dimensity 7200/7050
            hardware.contains("mt6886", ignoreCase = true) -> return "Mali-G610 MC4"
            
            // Dimensity 1300/1200/1100
            hardware.contains("mt6893", ignoreCase = true) ||
            hardware.contains("mt6891", ignoreCase = true) ||
            hardware.contains("mt6877", ignoreCase = true) -> return "Mali-G77 MC9"
            
            // Dimensity 1080/1000/920/900/810/800
            hardware.contains("mt6877", ignoreCase = true) ||
            hardware.contains("mt6877t", ignoreCase = true) ||
            hardware.contains("mt6875", ignoreCase = true) ||
            hardware.contains("mt6873", ignoreCase = true) -> return "Mali-G68 MC4"
            
            // Dimensity 720/700
            hardware.contains("mt6853", ignoreCase = true) ||
            hardware.contains("mt6833", ignoreCase = true) -> return "Mali-G57 MC3"
            
            // Helio G96/G95/G90T
            hardware.contains("mt6781", ignoreCase = true) ||
            hardware.contains("mt6785", ignoreCase = true) ||
            board.contains("mt6785", ignoreCase = true) -> return "Mali-G57 MC2"
            
            // Helio G88/G85/G80
            hardware.contains("mt6769", ignoreCase = true) ||
            hardware.contains("mt6768", ignoreCase = true) -> return "Mali-G52 MC2"
            
            // Helio G70/G65
            hardware.contains("mt6769", ignoreCase = true) ||
            hardware.contains("mt6765", ignoreCase = true) -> return "Mali-G52 MC2"
            
            // Helio P90/P70/P60
            hardware.contains("mt6779", ignoreCase = true) ||
            hardware.contains("mt6771", ignoreCase = true) -> return "PowerVR GM9446"
            
            // Helio G35/G25
            hardware.contains("mt6765", ignoreCase = true) ||
            hardware.contains("mt6762", ignoreCase = true) -> return "PowerVR GE8320"
        }
        
        // Google Tensor
        when {
            board.contains("gs301", ignoreCase = true) ||
            hardware.contains("gs301", ignoreCase = true) -> return "Mali-G715 MC7"
            
            board.contains("gs201", ignoreCase = true) ||
            hardware.contains("gs201", ignoreCase = true) -> return "Mali-G78 MP20"
            
            board.contains("gs101", ignoreCase = true) ||
            hardware.contains("gs101", ignoreCase = true) -> return "Mali-G78 MP20"
        }
        
        // HiSilicon Kirin
        when {
            board.contains("kirin9000", ignoreCase = true) ||
            hardware.contains("kirin9000", ignoreCase = true) -> return "Mali-G78 MP24"
            
            board.contains("kirin990", ignoreCase = true) ||
            hardware.contains("kirin990", ignoreCase = true) -> return "Mali-G76 MP16"
            
            board.contains("kirin980", ignoreCase = true) ||
            hardware.contains("kirin980", ignoreCase = true) -> return "Mali-G76 MP10"
            
            board.contains("kirin970", ignoreCase = true) ||
            hardware.contains("kirin970", ignoreCase = true) -> return "Mali-G72 MP12"
            
            board.contains("kirin960", ignoreCase = true) ||
            hardware.contains("kirin960", ignoreCase = true) -> return "Mali-G71 MP8"
            
            board.contains("kirin950", ignoreCase = true) ||
            hardware.contains("kirin950", ignoreCase = true) -> return "Mali-T880 MP4"
            
            board.contains("kirin710", ignoreCase = true) ||
            hardware.contains("kirin710", ignoreCase = true) -> return "Mali-G51 MP4"
        }
        
        // Genel kategoriler (eşleşme yoksa)
        return when {
            hardware.contains("qcom", ignoreCase = true) || 
            hardware.contains("sm", ignoreCase = true) ||
            hardware.contains("sdm", ignoreCase = true) -> "Adreno (Qualcomm)"
            
            hardware.contains("exynos", ignoreCase = true) ||
            board.contains("universal", ignoreCase = true) ||
            board.contains("s5e", ignoreCase = true) -> "Mali (Samsung)"
            
            hardware.contains("mt", ignoreCase = true) ||
            board.contains("mt", ignoreCase = true) -> "Mali (MediaTek)"
            
            hardware.contains("kirin", ignoreCase = true) -> "Mali (HiSilicon)"
            
            hardware.contains("gs", ignoreCase = true) ||
            board.contains("gs", ignoreCase = true) -> "Mali (Google Tensor)"
            
            hardware.contains("mali", ignoreCase = true) -> "Mali (ARM)"
            
            else -> Build.HARDWARE?.uppercase() ?: "Bilinmiyor"
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
        
        // Parmak izi sensörü - Android 9+ için BiometricManager, eski için FingerprintManager
        val hasFingerprint = when {
            VERSION.SDK_INT >= VERSION_CODES.Q -> {
                // Android 10+ - BiometricManager kullan
                try {
                    val biometricManager = getSystemService(Context.BIOMETRIC_SERVICE) as? android.hardware.biometrics.BiometricManager
                    val canAuthenticate = biometricManager?.canAuthenticate(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    canAuthenticate == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS
                } catch (e: Exception) {
                    // Fallback: PackageManager ile kontrol
                    packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
                }
            }
            VERSION.SDK_INT >= VERSION_CODES.M -> {
                // Android 6-9 - FingerprintManager kullan
                try {
                    val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as? android.hardware.fingerprint.FingerprintManager
                    fingerprintManager?.isHardwareDetected ?: false
                } catch (e: Exception) {
                    false
                }
            }
            else -> false
        }
        sensorInfo["hasFingerprint"] = hasFingerprint
        sensorInfo["fingerprintLocation"] = if (hasFingerprint) "Arka / Ekran / Yan" else "Yok"
        
        // Yüz tanıma - Android 10+ için BiometricManager
        val hasFaceRecognition = when {
            VERSION.SDK_INT >= VERSION_CODES.Q -> {
                try {
                    val biometricManager = getSystemService(Context.BIOMETRIC_SERVICE) as? android.hardware.biometrics.BiometricManager
                    // Yüz tanıma desteği var mı kontrol et
                    val canAuthenticate = biometricManager?.canAuthenticate(
                        android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_WEAK
                    )
                    canAuthenticate == android.hardware.biometrics.BiometricManager.BIOMETRIC_SUCCESS && hasFingerprint
                } catch (e: Exception) {
                    // Eski yöntem: cihaz modeline göre tahmin
                    val model = Build.MODEL?.lowercase() ?: ""
                    val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
                    model.contains("pixel") || 
                    (manufacturer.contains("samsung") && hasFingerprint) ||
                    (manufacturer.contains("xiaomi") && hasFingerprint) ||
                    (manufacturer.contains("oppo") && hasFingerprint) ||
                    (manufacturer.contains("vivo") && hasFingerprint)
                }
            }
            else -> {
                // Android 9 ve altı için tahmini kontrol
                val model = Build.MODEL?.lowercase() ?: ""
                val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
                model.contains("pixel") || 
                (manufacturer.contains("samsung") && hasFingerprint) ||
                manufacturer.contains("apple")
            }
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

        // Wi-Fi MAC Adresi - Android 6.0+ gerçek MAC'i gizler
        val wifiMac = try {
            // Android 6.0+ (API 23+) gerçek MAC adresini gizler
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                // NetworkInterface deneme
                val mac = getMacAddressFromNetworkInterface("wlan0")
                if (mac != null && mac != "02:00:00:00:00:00") {
                    mac
                } else {
                    "Gizli (Android 6.0+ Güvenlik)"
                }
            } else {
                getMacAddressFromNetworkInterface("wlan0") ?: "Bilinmiyor"
            }
        } catch (e: Exception) {
            "Gizli (Android 6.0+ Güvenlik)"
        }
        networkInfo["wifiMac"] = wifiMac

        // Bluetooth MAC Adresi ve Adı
        val btInfo = try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null) {
                // Bluetooth adı (diğer cihazların gördüğü isim)
                val btName = bluetoothAdapter.name ?: "Bilinmiyor"
                
                // MAC adresi - Android 6.0+ gizler
                val btMac = if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    "Gizli (Android 6.0+ Güvenlik)"
                } else {
                    bluetoothAdapter.address ?: "Bilinmiyor"
                }
                
                Pair(btMac, btName)
            } else {
                Pair("Bluetooth Kapalı", "Bluetooth Kapalı")
            }
        } catch (e: Exception) {
            Pair("Bilinmiyor", "Bilinmiyor")
        }
        networkInfo["bluetoothMac"] = btInfo.first
        networkInfo["bluetoothName"] = btInfo.second
        
        // IMEI (TelephonyManager ile - Android 10+ erişim yok)
        val imei = try {
            if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                // Android 10+ (API 29+) - IMEI erişimi kısıtlandı
                "Android 10+ Erişim Yok"
            } else if (VERSION.SDK_INT >= VERSION_CODES.O) {
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.imei ?: "İzin Gerekli"
            } else {
                val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.deviceId ?: "İzin Gerekli"
            }
        } catch (e: Exception) {
            "Erişim Yok"
        }
        networkInfo["imei"] = imei
        
        // IP Adresi - WifiManager öncelikli
        val ipAddress = try {
            // Önce WifiManager dene
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val wifiInfo = wifiManager?.connectionInfo
            val wifiIpInt = wifiInfo?.ipAddress ?: 0
            
            if (wifiIpInt != 0) {
                // WiFi bağlı, IP'yi çevir
                String.format("%d.%d.%d.%d",
                    wifiIpInt and 0xff,
                    (wifiIpInt shr 8) and 0xff,
                    (wifiIpInt shr 16) and 0xff,
                    (wifiIpInt shr 24) and 0xff)
            } else {
                // WiFi yoksa NetworkInterface dene
                var foundIp: String? = null
                val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
                for (intf in interfaces.asSequence()) {
                    val name = intf.name.lowercase()
                    // WiFi veya mobil veri arayüzleri
                    if (name.contains("wlan") || name.contains("rmnet") || name.contains("ccmni")) {
                        val addrs = intf.inetAddresses.asSequence()
                            .filter { !it.isLoopbackAddress && it is java.net.Inet4Address }
                            .map { it.hostAddress }
                            .toList()
                        if (addrs.isNotEmpty()) {
                            foundIp = addrs.first()
                            break
                        }
                    }
                }
                foundIp ?: "Bağlı Değil"
            }
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
            
            // Always-on Display desteği
            val alwaysOnDisplaySupported = if (VERSION.SDK_INT >= VERSION_CODES.O) {
                try {
                    val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                    // Reflection kullanarak isAlwaysOnDisplaySupported metodunu çağır
                    // (API 29+ sadece)
                    val method = powerManager?.javaClass?.getMethod("isAlwaysOnDisplaySupported")
                    method?.invoke(powerManager) as? Boolean ?: false
                } catch (e: Exception) {
                    // Fallback: cihaz modeline göre tahmin
                    val model = Build.MODEL?.lowercase() ?: ""
                    val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
                    when {
                        manufacturer.contains("samsung") -> true
                        manufacturer.contains("google") && model.contains("pixel") -> model.contains("pixel 2") || model.contains("pixel 3") || model.contains("pixel 4") || model.contains("pixel 5") || model.contains("pixel 6") || model.contains("pixel 7") || model.contains("pixel 8")
                        manufacturer.contains("oneplus") -> true
                        manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> true
                        manufacturer.contains("oppo") || manufacturer.contains("vivo") -> true
                        manufacturer.contains("huawei") || manufacturer.contains("honor") -> true
                        else -> false
                    }
                }
            } else {
                false
            }
            displayInfo["alwaysOnDisplay"] = if (alwaysOnDisplaySupported) "Destekleniyor" else "Desteklenmiyor"
            
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
        
        // Kablosuz şarj desteği - PackageManager ve cihaz modeli ile kontrol
        val hasWirelessCharging = when {
            // Samsung amiral gemisi modelleri
            Build.MANUFACTURER?.contains("samsung", ignoreCase = true) == true && (
                Build.MODEL?.contains("s21", ignoreCase = true) == true ||
                Build.MODEL?.contains("s22", ignoreCase = true) == true ||
                Build.MODEL?.contains("s23", ignoreCase = true) == true ||
                Build.MODEL?.contains("s24", ignoreCase = true) == true ||
                Build.MODEL?.contains("note", ignoreCase = true) == true ||
                Build.MODEL?.contains("fold", ignoreCase = true) == true ||
                Build.MODEL?.contains("flip", ignoreCase = true) == true ||
                Build.MODEL?.contains("s10", ignoreCase = true) == true ||
                Build.MODEL?.contains("s9", ignoreCase = true) == true ||
                Build.MODEL?.contains("s8", ignoreCase = true) == true
            ) -> true
            // Google Pixel modelleri (3 ve üstü)
            Build.MANUFACTURER?.contains("google", ignoreCase = true) == true && (
                Build.MODEL?.contains("pixel 3", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 4", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 5", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 6", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 7", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 8", ignoreCase = true) == true ||
                Build.MODEL?.contains("pixel 9", ignoreCase = true) == true
            ) -> true
            // Apple iPhone modelleri (8 ve üstü)
            Build.MANUFACTURER?.contains("apple", ignoreCase = true) == true -> true
            // OnePlus amiral gemisi modelleri
            Build.MANUFACTURER?.contains("oneplus", ignoreCase = true) == true && (
                Build.MODEL?.contains("8", ignoreCase = true) == true ||
                Build.MODEL?.contains("9", ignoreCase = true) == true ||
                Build.MODEL?.contains("10", ignoreCase = true) == true ||
                Build.MODEL?.contains("11", ignoreCase = true) == true ||
                Build.MODEL?.contains("12", ignoreCase = true) == true
            ) -> true
            // Xiaomi amiral gemisi modelleri
            Build.MANUFACTURER?.contains("xiaomi", ignoreCase = true) == true && (
                Build.MODEL?.contains("mi 10", ignoreCase = true) == true ||
                Build.MODEL?.contains("mi 11", ignoreCase = true) == true ||
                Build.MODEL?.contains("mi 12", ignoreCase = true) == true ||
                Build.MODEL?.contains("mi 13", ignoreCase = true) == true ||
                Build.MODEL?.contains("mi 14", ignoreCase = true) == true ||
                Build.MODEL?.contains("13 ultra", ignoreCase = true) == true ||
                Build.MODEL?.contains("14 ultra", ignoreCase = true) == true
            ) -> true
            // Huawei amiral gemisi modelleri
            Build.MANUFACTURER?.contains("huawei", ignoreCase = true) == true && (
                Build.MODEL?.contains("p30", ignoreCase = true) == true ||
                Build.MODEL?.contains("p40", ignoreCase = true) == true ||
                Build.MODEL?.contains("p50", ignoreCase = true) == true ||
                Build.MODEL?.contains("p60", ignoreCase = true) == true ||
                Build.MODEL?.contains("mate 20", ignoreCase = true) == true ||
                Build.MODEL?.contains("mate 30", ignoreCase = true) == true ||
                Build.MODEL?.contains("mate 40", ignoreCase = true) == true ||
                Build.MODEL?.contains("mate 50", ignoreCase = true) == true ||
                Build.MODEL?.contains("mate 60", ignoreCase = true) == true
            ) -> true
            // Sony Xperia amiral gemisi modelleri
            Build.MANUFACTURER?.contains("sony", ignoreCase = true) == true && (
                Build.MODEL?.contains("xperia 1", ignoreCase = true) == true ||
                Build.MODEL?.contains("xperia 5", ignoreCase = true) == true
            ) -> true
            // LG amiral gemisi modelleri
            Build.MANUFACTURER?.contains("lg", ignoreCase = true) == true && (
                Build.MODEL?.contains("v30", ignoreCase = true) == true ||
                Build.MODEL?.contains("v35", ignoreCase = true) == true ||
                Build.MODEL?.contains("v40", ignoreCase = true) == true ||
                Build.MODEL?.contains("v50", ignoreCase = true) == true ||
                Build.MODEL?.contains("v60", ignoreCase = true) == true ||
                Build.MODEL?.contains("g7", ignoreCase = true) == true ||
                Build.MODEL?.contains("g8", ignoreCase = true) == true ||
                Build.MODEL?.contains("velvet", ignoreCase = true) == true ||
                Build.MODEL?.contains("wing", ignoreCase = true) == true
            ) -> true
            else -> false
        }
        features["hasWirelessCharging"] = hasWirelessCharging
        
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
        
        // Detaylı GPU Modeli - Board ve Hardware'dan tahmin
        val gpuModel = getDetailedGpuModel()
        gpuInfo["gpuModel"] = gpuModel
        
        // Build.HARDWARE değeri (ham)
        gpuInfo["hardware"] = Build.HARDWARE ?: "Bilinmiyor"
        
        return gpuInfo
    }
}
