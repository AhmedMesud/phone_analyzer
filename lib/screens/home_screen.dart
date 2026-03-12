import 'dart:io';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../l10n/app_localizations.dart';
import '../widgets/modern_widgets.dart';
import 'category_detail_screen.dart';

class HomeScreen extends StatefulWidget {
  final bool isDarkMode;
  final Function(bool) onThemeChanged;
  final List<Map<String, dynamic>> languages;
  final Locale currentLocale;
  final Function(Locale) onLanguageChanged;

  const HomeScreen({
    super.key,
    required this.isDarkMode,
    required this.onThemeChanged,
    required this.languages,
    required this.currentLocale,
    required this.onLanguageChanged,
  });

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  static const platform = MethodChannel('com.example.phone_analyzer/device');
  
  // Temel Bilgiler
  String _deviceModel = '';
  String _deviceCodeName = '';
  String _brand = '';
  String _androidVersion = '';
  String _totalRAM = '';
  String _totalStorage = '';
  String _cpuCoreCount = '';
  
  // Detaylı Bilgiler
  String _hardware = '';
  String _board = '';
  int _cameraCount = 0;
  String _backCameraResolution = '';
  String _frontCameraResolution = '';
  bool _hasAutofocus = false;
  String _cameraHardwareLevel = '';
  String _manufacturer = '';
  String _product = '';
  String _serial = '';
  String _bootloader = '';
  String _radioVersion = '';
  String _availableRAM = '';
  String _usedRAM = '';
  String _ramUsagePercent = '';
  String _availableStorage = '';
  String _usedStorage = '';
  bool _hasSDCard = false;
  String _sdCardTotal = '';
  String _batteryTemperature = '';
  String _batteryVoltage = '';
  String _batteryTechnology = '';
  String _batteryLevel = '';
  bool _isWirelessCharging = false;
  String _cpuArchitecture = '';
  String _cpuPart = '';
  String _cpuMaxFrequencies = '';
  String _gpuInfo = '';
  String _gpuModel = '';
  String _openGlEsVersion = '';
  String _hardwarePlatform = '';
  bool _hasFingerprint = false;
  String _fingerprintLocation = '';
  bool _hasFaceRecognition = false;
  bool _hasProximity = false;
  bool _hasLightSensor = false;
  bool _hasBarometer = false;
  bool _hasThermometer = false;
  bool _hasAccelerometer = false;
  bool _hasGyroscope = false;
  bool _hasCompass = false;
  String _wifiMac = '';
  String _bluetoothMac = '';
  String _bluetoothName = '';
  String _imei = '';
  String _ipAddress = '';
  String _networkType = '';
  String _wifiLinkSpeed = '';
  String _wifiSignalStrength = '';
  String _wifiSignalQuality = '';
  String _wifiFrequency = '';
  String _wifiStandard = '';
  String _securityPatch = '';
  String _gmsVersion = '';
  String _kernelVersion = '';
  String _isRooted = '';
  String _refreshRate = '';
  String _hdrSupport = '';
  String _alwaysOnDisplay = '';
  bool _hasStereoSpeakers = false;
  bool _hasNFC = false;
  bool _nfcEnabled = false;
  bool _hasIRBlaster = false;
  String _ipRating = '';
  String _fastCharging = '';
  bool _hasWirelessCharging = false;

  bool _isLoading = true;
  late AnimationController _animationController;
  late Animation<double> _fadeAnimation;

  @override
  void initState() {
    super.initState();
    _animationController = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 800),
    );
    _fadeAnimation = Tween<double>(begin: 0, end: 1).animate(
      CurvedAnimation(parent: _animationController, curve: Curves.easeOut),
    );
    _getDeviceInfo();
  }

  @override
  void dispose() {
    _animationController.dispose();
    super.dispose();
  }

  Future<void> _getDeviceInfo() async {
    final Map<String, dynamic> results = {};

    // Tüm bilgileri paralel olarak çek
    final futures = {
      'deviceModel': platform.invokeMethod('getDeviceModel'),
      'deviceCodeName': platform.invokeMethod('getDeviceCodeName'),
      'hardware': platform.invokeMethod('getHardware'),
      'board': platform.invokeMethod('getBoard'),
      'ram': platform.invokeMethod('getTotalRAM'),
      'camera': platform.invokeMethod('getCameraDetails'),
      'identity': platform.invokeMethod('getDeviceIdentity'),
      'storage': platform.invokeMethod('getStorageInfo'),
      'battery': platform.invokeMethod('getBatteryInfo'),
      'cpu': platform.invokeMethod('getCpuInfo'),
      'gpu': platform.invokeMethod('getGpuInfo'),
      'sensors': platform.invokeMethod('getSensorInfo'),
      'network': platform.invokeMethod('getNetworkInfo'),
      'software': platform.invokeMethod('getSoftwareInfo'),
      'display': platform.invokeMethod('getDisplayInfo'),
      'other': platform.invokeMethod('getOtherFeatures'),
    };

    for (final entry in futures.entries) {
      try {
        results[entry.key] = await entry.value;
      } catch (e) {
        results[entry.key] = null;
      }
    }

    setState(() {
      _deviceModel = results['deviceModel'] ?? 'Unknown';
      _deviceCodeName = results['deviceCodeName'] ?? 'Unknown';
      _hardware = results['hardware'] ?? 'Unknown';
      _board = results['board'] ?? 'Unknown';
      
      final ram = results['ram'] as Map<dynamic, dynamic>?;
      _totalRAM = ram?['total'] ?? 'Unknown';
      _availableRAM = ram?['available'] ?? 'Unknown';
      // Kullanılan RAM'i doğru hesapla (total - available)
      final totalBytes = _parseBytes(_totalRAM);
      final availableBytes = _parseBytes(_availableRAM);
      if (totalBytes > 0 && availableBytes > 0) {
        final usedBytes = totalBytes - availableBytes;
        _usedRAM = _formatBytes(usedBytes);
        _ramUsagePercent = '${((usedBytes * 100) / totalBytes).toInt()}%';
      } else {
        _usedRAM = ram?['used'] ?? 'Unknown';
        _ramUsagePercent = ram?['usagePercent'] ?? '0';
      }
      
      final camera = results['camera'] as Map<dynamic, dynamic>?;
      _cameraCount = camera?['cameraCount'] ?? 0;
      _backCameraResolution = camera?['backCameraResolution'] ?? 'Unknown';
      _frontCameraResolution = camera?['frontCameraResolution'] ?? 'Unknown';
      _hasAutofocus = camera?['hasAutofocus'] ?? false;
      _cameraHardwareLevel = camera?['hardwareLevel'] ?? 'Unknown';
      
      final identity = results['identity'] as Map<dynamic, dynamic>?;
      _manufacturer = identity?['manufacturer'] ?? 'Unknown';
      _brand = identity?['brand'] ?? 'Unknown';
      _product = identity?['product'] ?? 'Unknown';
      _serial = identity?['serial'] ?? 'Unknown';
      _bootloader = identity?['bootloader'] ?? 'Unknown';
      _radioVersion = identity?['radioVersion'] ?? 'Unknown';
      
      final storage = results['storage'] as Map<dynamic, dynamic>?;
      _totalStorage = storage?['totalStorage'] ?? 'Unknown';
      _availableStorage = storage?['availableStorage'] ?? 'Unknown';
      _usedStorage = storage?['usedStorage'] ?? 'Unknown';
      _hasSDCard = storage?['hasSDCard'] ?? false;
      _sdCardTotal = storage?['sdCardTotal'] ?? 'Yok';
      
      final battery = results['battery'] as Map<dynamic, dynamic>?;
      _batteryTemperature = battery?['temperature'] ?? 'Unknown';
      _batteryVoltage = battery?['voltage'] ?? 'Unknown';
      _batteryTechnology = battery?['technology'] ?? 'Unknown';
      _batteryLevel = battery?['level'] ?? 'Unknown';
      _isWirelessCharging = battery?['isWirelessCharging'] ?? false;
      
      final cpu = results['cpu'] as Map<dynamic, dynamic>?;
      _cpuCoreCount = cpu?['coreCount'] ?? 'Unknown';
      _cpuArchitecture = cpu?['architecture'] ?? 'Unknown';
      _cpuPart = cpu?['cpuPart'] ?? 'Unknown';
      _cpuMaxFrequencies = cpu?['maxFrequencies'] ?? 'Unknown';
      
      final gpu = results['gpu'] as Map<dynamic, dynamic>?;
      _gpuModel = gpu?['model'] ?? 'Unknown';
      _openGlEsVersion = gpu?['openGlEsVersion'] ?? 'Unknown';
      _hardwarePlatform = gpu?['hardware'] ?? 'Unknown';
      
      final sensors = results['sensors'] as Map<dynamic, dynamic>?;
      _hasFingerprint = sensors?['hasFingerprint'] ?? false;
      _fingerprintLocation = sensors?['fingerprintLocation'] ?? '';
      _hasFaceRecognition = sensors?['hasFaceRecognition'] ?? false;
      _hasProximity = sensors?['hasProximity'] ?? false;
      _hasLightSensor = sensors?['hasLightSensor'] ?? false;
      _hasBarometer = sensors?['hasBarometer'] ?? false;
      _hasThermometer = sensors?['hasThermometer'] ?? false;
      _hasAccelerometer = sensors?['hasAccelerometer'] ?? false;
      _hasGyroscope = sensors?['hasGyroscope'] ?? false;
      _hasCompass = sensors?['hasCompass'] ?? false;
      
      final network = results['network'] as Map<dynamic, dynamic>?;
      _wifiMac = network?['wifiMac'] ?? 'Unknown';
      _bluetoothMac = network?['bluetoothMac'] ?? 'Unknown';
      _bluetoothName = network?['bluetoothName'] ?? 'Unknown';
      _imei = network?['imei'] ?? 'Unknown';
      _ipAddress = network?['ipAddress'] ?? 'Unknown';
      _networkType = network?['networkType'] ?? 'Unknown';
      _wifiLinkSpeed = network?['wifiLinkSpeed'] ?? 'Unknown';
      _wifiSignalStrength = network?['wifiSignalStrength'] ?? 'Unknown';
      _wifiSignalQuality = network?['wifiSignalQuality'] ?? 'Unknown';
      _wifiFrequency = network?['wifiFrequency'] ?? 'Unknown';
      _wifiStandard = network?['wifiStandard'] ?? 'Unknown';
      
      final software = results['software'] as Map<dynamic, dynamic>?;
      _securityPatch = software?['securityPatch'] ?? 'Unknown';
      _gmsVersion = software?['gmsVersion'] ?? 'Unknown';
      _kernelVersion = software?['kernelVersion'] ?? 'Unknown';
      _isRooted = software?['isRooted'] ?? 'Unknown';
      _androidVersion = software?['androidVersion'] ?? 'Unknown';
      
      final display = results['display'] as Map<dynamic, dynamic>?;
      _refreshRate = display?['refreshRate'] ?? 'Unknown';
      _hdrSupport = display?['hdrSupport'] ?? 'Unknown';
      _alwaysOnDisplay = display?['alwaysOnDisplay'] ?? 'Unknown';
      
      final other = results['other'] as Map<dynamic, dynamic>?;
      _hasStereoSpeakers = other?['hasStereoSpeakers'] ?? false;
      _hasNFC = other?['hasNFC'] ?? false;
      _nfcEnabled = other?['nfcEnabled'] ?? false;
      _hasIRBlaster = other?['hasIRBlaster'] ?? false;
      _ipRating = other?['ipRating'] ?? 'Bilinmiyor';
      _fastCharging = other?['fastCharging'] ?? 'Bilinmiyor';
      
      _isLoading = false;
    });

    _animationController.forward();
  }

  double _parsePercentage(String value) {
    try {
      return double.parse(value.replaceAll('%', '').trim()) / 100;
    } catch (e) {
      return 0;
    }
  }

  // Byte string'i parse et (örn: "1 GB" -> 1073741824)
  int _parseBytes(String value) {
    try {
      final parts = value.trim().split(' ');
      if (parts.length != 2) return 0;
      final num = double.parse(parts[0]);
      final unit = parts[1].toUpperCase();
      switch (unit) {
        case 'B': return num.toInt();
        case 'KB': return (num * 1024).toInt();
        case 'MB': return (num * 1024 * 1024).toInt();
        case 'GB': return (num * 1024 * 1024 * 1024).toInt();
        case 'TB': return (num * 1024 * 1024 * 1024 * 1024).toInt();
        default: return 0;
      }
    } catch (e) {
      return 0;
    }
  }

  // Byte'ı formatla (örn: 1073741824 -> "1 GB")
  String _formatBytes(int bytes) {
    if (bytes <= 0) return '0 B';
    const suffixes = ['B', 'KB', 'MB', 'GB', 'TB'];
    var i = (log(bytes) / log(1024)).floor();
    if (i >= suffixes.length) i = suffixes.length - 1;
    return '${(bytes / pow(1024, i)).toStringAsFixed(2)} ${suffixes[i]}';
  }

  void _showLanguageDialog() {
    final isDark = widget.isDarkMode;
    showDialog(
      context: context,
      builder: (dialogContext) => AlertDialog(
        backgroundColor: isDark ? AppColors.darkCard : Colors.white,
        title: Text(
          'Language',
          style: TextStyle(
            color: isDark ? AppColors.textPrimary : Colors.black87,
          ),
        ),
        content: SizedBox(
          width: double.maxFinite,
          child: ListView.builder(
            shrinkWrap: true,
            itemCount: widget.languages.length,
            itemBuilder: (dialogContext, index) {
              final lang = widget.languages[index];
              final isSelected = lang['code'] == widget.currentLocale.languageCode;
              
              return ListTile(
                leading: Text(
                  lang['flag'] ?? '🏳️',
                  style: const TextStyle(fontSize: 24),
                ),
                title: Text(
                  lang['name'] ?? 'Unknown',
                  style: TextStyle(
                    color: isDark ? AppColors.textPrimary : Colors.black87,
                  ),
                ),
                trailing: isSelected
                    ? const Icon(Icons.check, color: AppColors.accentGreen)
                    : null,
                onTap: () {
                  widget.onLanguageChanged(Locale(lang['code']!));
                  Navigator.pop(dialogContext);
                },
              );
            },
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final isDark = widget.isDarkMode;
    final platform = Platform.operatingSystem;

    if (_isLoading) {
      return Scaffold(
        backgroundColor: isDark ? AppColors.darkBackground : Colors.grey.shade50,
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  gradient: const LinearGradient(
                    colors: [AppColors.primaryStart, AppColors.primaryEnd],
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: AppColors.primaryStart.withOpacity(0.4),
                      blurRadius: 30,
                      spreadRadius: 5,
                    ),
                  ],
                ),
                child: const CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 3,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                l10n?.analyzingDevice ?? 'Analyzing Device...',
                style: TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w500,
                  color: isDark ? AppColors.textSecondary : Colors.grey.shade700,
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: isDark ? AppColors.darkBackground : Colors.grey.shade50,
      body: RefreshIndicator(
        onRefresh: _getDeviceInfo,
        color: AppColors.primaryStart,
        backgroundColor: isDark ? AppColors.darkCard : Colors.white,
        child: CustomScrollView(
          physics: const BouncingScrollPhysics(),
          slivers: [
            // Modern App Bar
            SliverAppBar(
              expandedHeight: 220,
              pinned: true,
              elevation: 0,
              backgroundColor: Colors.transparent,
              flexibleSpace: FlexibleSpaceBar(
                background: Container(
                  decoration: const BoxDecoration(
                    gradient: LinearGradient(
                      begin: Alignment.topLeft,
                      end: Alignment.bottomRight,
                      colors: [
                        AppColors.primaryStart,
                        AppColors.primaryEnd,
                      ],
                    ),
                  ),
                  child: Stack(
                    children: [
                      // Dekoratif daireler
                      Positioned(
                        right: -60,
                        top: -60,
                        child: Container(
                          width: 250,
                          height: 250,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.white.withOpacity(0.1),
                          ),
                        ),
                      ),
                      Positioned(
                        left: -40,
                        bottom: 60,
                        child: Container(
                          width: 150,
                          height: 150,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: Colors.white.withOpacity(0.08),
                          ),
                        ),
                      ),
                      // Cihaz Bilgisi
                      Positioned(
                        left: 24,
                        right: 24,
                        bottom: 80,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.2),
                                borderRadius: BorderRadius.circular(20),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    Icons.phone_android,
                                    color: Colors.white.withOpacity(0.9),
                                    size: 16,
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    _brand.isNotEmpty && _brand != 'Unknown' ? _brand : 'Device',
                                    style: TextStyle(
                                      color: Colors.white.withOpacity(0.9),
                                      fontSize: 13,
                                      fontWeight: FontWeight.w500,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              _deviceModel,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 28,
                                fontWeight: FontWeight.bold,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                            const SizedBox(height: 6),
                            Text(
                              '${platform.toUpperCase()} • Android $_androidVersion',
                              style: TextStyle(
                                color: Colors.white.withOpacity(0.8),
                                fontSize: 14,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              actions: [
                // Dil seçimi
                IconButton(
                  icon: const Icon(Icons.language, color: Colors.white),
                  onPressed: _showLanguageDialog,
                ),
                // Tema değiştirme
                IconButton(
                  icon: Icon(
                    widget.isDarkMode ? Icons.light_mode : Icons.dark_mode,
                    color: Colors.white,
                  ),
                  onPressed: () => widget.onThemeChanged(!widget.isDarkMode),
                ),
              ],
            ),
            
            // İçerik
            SliverPadding(
              padding: const EdgeInsets.all(16),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  // Özet İstatistikler
                  FadeTransition(
                    opacity: _fadeAnimation,
                    child: Row(
                      children: [
                        Expanded(
                          child: SummaryStat(
                            icon: Icons.memory,
                            value: _totalRAM,
                            label: l10n?.totalRAM ?? 'RAM',
                            color: AppColors.accentCyan,
                          ),
                        ),
                        Expanded(
                          child: SummaryStat(
                            icon: Icons.storage,
                            value: _totalStorage,
                            label: l10n?.totalStorage ?? 'Storage',
                            color: AppColors.accentOrange,
                          ),
                        ),
                        Expanded(
                          child: SummaryStat(
                            icon: Icons.camera_alt,
                            value: '$_cameraCount',
                            label: l10n?.cameraCount ?? 'Cameras',
                            color: AppColors.accentPurple,
                          ),
                        ),
                      ],
                    ),
                  ),
                  
                  const SizedBox(height: 24),
                  
                  // RAM ve Depolama Kullanımı
                  FadeTransition(
                    opacity: _fadeAnimation,
                    child: ModernCard(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Container(
                                padding: const EdgeInsets.all(8),
                                decoration: BoxDecoration(
                                  borderRadius: BorderRadius.circular(10),
                                  color: AppColors.accentCyan.withOpacity(0.15),
                                ),
                                child: const Icon(
                                  Icons.speed,
                                  color: AppColors.accentCyan,
                                  size: 20,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Text(
                                l10n?.systemStatus ?? 'System Status',
                                style: const TextStyle(
                                  fontSize: 16,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 20),
                          ModernProgressBar(
                            progress: _parsePercentage(_ramUsagePercent),
                            label: 'Kullanılan: $_usedRAM / Toplam: $_totalRAM',
                            value: _ramUsagePercent,
                            gradientColors: [
                              AppColors.accentCyan,
                              AppColors.primaryStart,
                            ],
                          ),
                          const SizedBox(height: 16),
                          ModernProgressBar(
                            progress: _totalStorage != 'Unknown' && _totalStorage.isNotEmpty
                                ? (double.tryParse(_usedStorage.replaceAll(RegExp(r'[^0-9.]'), '')) ?? 0) /
                                  (double.tryParse(_totalStorage.replaceAll(RegExp(r'[^0-9.]'), '')) ?? 1)
                                : 0,
                            label: 'Kullanılan: $_usedStorage / Toplam: $_totalStorage',
                            value: _availableStorage != 'Unknown' ? 'Boş: $_availableStorage' : '',
                            gradientColors: [
                              AppColors.accentOrange,
                              AppColors.accentYellow,
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  
                  const SizedBox(height: 32),
                  
                  // Kategoriler Başlığı
                  Builder(
                    builder: (context) {
                      final categories = _getCategories(l10n);
                      return Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceBetween,
                            children: [
                              Text(
                                l10n?.categories ?? 'Categories',
                                style: TextStyle(
                                  fontSize: 20,
                                  fontWeight: FontWeight.bold,
                                  color: isDark ? AppColors.textPrimary : Colors.black87,
                                ),
                              ),
                              Text(
                                '${categories.length} ${l10n?.items ?? 'items'}',
                                style: TextStyle(
                                  fontSize: 14,
                                  color: isDark ? AppColors.textMuted : Colors.grey.shade600,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 16),
                          // Kategori Grid
                          FadeTransition(
                            opacity: _fadeAnimation,
                            child: GridView.builder(
                              shrinkWrap: true,
                              physics: const NeverScrollableScrollPhysics(),
                              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: 2,
                                childAspectRatio: 1.1,
                                crossAxisSpacing: 12,
                                mainAxisSpacing: 12,
                              ),
                              itemCount: categories.length,
                              itemBuilder: (context, index) {
                                final category = categories[index];
                                return CategoryCard(
                                  icon: category['icon'] as IconData,
                                  title: category['title'] as String,
                                  subtitle: category['subtitle'] as String,
                                  accentColor: category['color'] as Color,
                                  onTap: () => _navigateToCategory(category['id'] as String, l10n),
                                  badgeCount: category['badge'] as int?,
                                );
                              },
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                  
                  const SizedBox(height: 32),
                  
                  // Cihaz Özellikleri
                  FadeTransition(
                    opacity: _fadeAnimation,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          l10n?.deviceFeatures ?? 'Device Features',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: isDark ? AppColors.textPrimary : Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          l10n?.deviceFeaturesSubtitle ?? 'Hardware features supported by your phone',
                          style: TextStyle(
                            fontSize: 13,
                            color: isDark ? AppColors.textMuted : Colors.grey.shade600,
                          ),
                        ),
                        const SizedBox(height: 8),
                        Wrap(
                          spacing: 12,
                          runSpacing: 12,
                          children: [
                            if (_hasFingerprint)
                              FeatureChip(
                                label: l10n?.fingerprint ?? 'Fingerprint',
                                icon: Icons.fingerprint,
                              ),
                            if (_hasFaceRecognition)
                              FeatureChip(
                                label: l10n?.faceRecognition ?? 'Face ID',
                                icon: Icons.face,
                              ),
                            if (_hasNFC)
                              FeatureChip(
                                label: 'NFC ${_nfcEnabled ? "(On)" : "(Off)"}',
                                icon: Icons.nfc,
                              ),
                            if (_hasWirelessCharging)
                              FeatureChip(
                                label: 'Wireless Charging',
                                icon: Icons.battery_charging_full,
                              ),
                            if (_hasIRBlaster)
                              FeatureChip(
                                label: 'IR Blaster',
                                icon: Icons.settings_remote,
                              ),
                            if (_hasStereoSpeakers)
                              FeatureChip(
                                label: 'Stereo Speakers',
                                icon: Icons.speaker,
                              ),
                            FeatureChip(
                              label: _refreshRate != 'Unknown' ? _refreshRate : '60Hz',
                              icon: Icons.refresh,
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  
                  const SizedBox(height: 40),
                ]),
              ),
            ),
          ],
        ),
      ),
    );
  }

  List<Map<String, dynamic>> _getCategories(AppLocalizations? l10n) {
    return [
      {
        'id': 'general',
        'icon': Icons.devices,
        'title': l10n?.generalInfo ?? 'Genel Bilgiler',
        'subtitle': '${_brand.isNotEmpty && _brand != 'Unknown' ? _brand : 'Device'} • $_androidVersion',
        'color': AppColors.accentCyan,
        'badge': null,
      },
      {
        'id': 'display',
        'icon': Icons.screen_rotation,
        'title': l10n?.displayDetails ?? 'Ekran Detayları',
        'subtitle': _refreshRate != 'Unknown' ? _refreshRate : 'Display Info',
        'color': AppColors.accentPurple,
        'badge': null,
      },
      {
        'id': 'camera',
        'icon': Icons.camera_alt,
        'title': l10n?.cameraInfo ?? 'Kamera',
        'subtitle': '$_cameraCount ${l10n?.cameras ?? 'Kamera'} • $_backCameraResolution',
        'color': AppColors.accentOrange,
        'badge': _cameraCount > 0 ? _cameraCount : null,
      },
      {
        'id': 'performance',
        'icon': Icons.speed,
        'title': '${l10n?.performance ?? 'Performans'} (CPU/GPU)',
        'subtitle': '${_cpuCoreCount != 'Unknown' ? '$_cpuCoreCount Çekirdek' : 'CPU ve GPU Bilgileri'}',
        'color': AppColors.accentGreen,
        'badge': null,
      },
      {
        'id': 'storage',
        'icon': Icons.storage,
        'title': l10n?.storageInfo ?? 'Depolama',
        'subtitle': _hasSDCard 
            ? '$_totalStorage • SD Kart Var'
            : (_totalStorage != 'Unknown' ? '$_totalStorage Toplam' : 'Depolama Bilgisi'),
        'color': AppColors.accentYellow,
        'badge': null,
      },
      {
        'id': 'network',
        'icon': Icons.wifi,
        'title': l10n?.networkInfo ?? 'Ağ',
        'subtitle': _networkType != 'Unknown' ? _networkType : 'Wi-Fi & Mobil',
        'color': const Color(0xFF4dabf7),
        'badge': null,
      },
      {
        'id': 'sensors',
        'icon': Icons.sensors,
        'title': l10n?.sensors ?? 'Sensörler',
        'subtitle': _countActiveSensors() > 0 ? '${_countActiveSensors()} Aktif' : 'Sensör Bilgisi',
        'color': const Color(0xFFff8787),
        'badge': _countActiveSensors() > 0 ? _countActiveSensors() : null,
      },
      {
        'id': 'battery',
        'icon': Icons.battery_full,
        'title': l10n?.batteryInfo ?? 'Pil',
        'subtitle': _batteryTechnology != 'Unknown' ? _batteryTechnology : 'Pil Bilgisi',
        'color': const Color(0xFF69db7c),
        'badge': null,
      },
    ];
  }

  int _countActiveSensors() {
    int count = 0;
    if (_hasFingerprint) count++;
    if (_hasFaceRecognition) count++;
    if (_hasProximity) count++;
    if (_hasLightSensor) count++;
    if (_hasBarometer) count++;
    if (_hasThermometer) count++;
    if (_hasAccelerometer) count++;
    if (_hasGyroscope) count++;
    if (_hasCompass) count++;
    return count;
  }

  void _navigateToCategory(String categoryId, AppLocalizations? l10n) {
    switch (categoryId) {
      case 'general':
        _showGeneralInfo(l10n);
        break;
      case 'display':
        _showDisplayInfo(l10n);
        break;
      case 'camera':
        _showCameraInfo(l10n);
        break;
      case 'performance':
        _showPerformanceInfo(l10n);
        break;
      case 'storage':
        _showStorageInfo(l10n);
        break;
      case 'network':
        _showNetworkInfo(l10n);
        break;
      case 'sensors':
        _showSensorsInfo(l10n);
        break;
      case 'battery':
        _showBatteryInfo(l10n);
        break;
    }
  }

  void _showGeneralInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.generalInfo ?? 'General Information',
          icon: Icons.devices,
          accentColor: AppColors.accentCyan,
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.deviceInfo ?? 'Device Information'),
            ModernListTile(
              leadingIcon: Icons.phone_android,
              iconColor: AppColors.accentCyan,
              title: l10n?.deviceModel ?? 'Device Model',
              value: _deviceModel,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.code,
              iconColor: AppColors.accentCyan,
              title: l10n?.deviceCodeName ?? 'Device Code Name',
              value: _deviceCodeName,
            ),
            ModernListTile(
              leadingIcon: Icons.branding_watermark,
              iconColor: AppColors.accentCyan,
              title: l10n?.brand ?? 'Brand',
              value: _brand,
            ),
            ModernListTile(
              leadingIcon: Icons.android,
              iconColor: AppColors.accentGreen,
              title: l10n?.androidVersion ?? 'Android Version',
              value: _androidVersion,
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.systemInfo ?? 'System Information'),
            ModernListTile(
              leadingIcon: Icons.memory,
              iconColor: AppColors.accentOrange,
              title: l10n?.totalRAM ?? 'Total RAM',
              value: _totalRAM,
            ),
            ModernListTile(
              leadingIcon: Icons.storage,
              iconColor: AppColors.accentYellow,
              title: l10n?.totalStorage ?? 'Total Storage',
              value: _totalStorage,
            ),
            ModernListTile(
              leadingIcon: Icons.developer_board,
              iconColor: AppColors.accentPurple,
              title: l10n?.board ?? 'Board',
              value: _board,
            ),
            ModernListTile(
              leadingIcon: Icons.computer,
              iconColor: AppColors.primaryStart,
              title: l10n?.cpuModel ?? 'CPU Model',
              value: _cpuPart,
            ),
            ModernListTile(
              leadingIcon: Icons.camera_alt,
              iconColor: AppColors.accentOrange,
              title: l10n?.cameraCount ?? 'Camera Count',
              value: '$_cameraCount',
            ),
            ModernListTile(
              leadingIcon: Icons.network_cell,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.networkType ?? 'Network Type',
              value: _networkType,
            ),
            ModernListTile(
              leadingIcon: Icons.wifi,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.ipAddress ?? 'IP Address',
              value: _ipAddress,
            ),
          ],
        ),
      ),
    );
  }

  void _showDisplayInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.displayDetails ?? 'Display Details',
          icon: Icons.screen_rotation,
          accentColor: AppColors.accentPurple,
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.displaySpecs ?? 'Display Specifications'),
            ModernListTile(
              leadingIcon: Icons.refresh,
              iconColor: AppColors.accentPurple,
              title: l10n?.refreshRate ?? 'Refresh Rate',
              value: _refreshRate,
            ),
            ModernListTile(
              leadingIcon: Icons.hdr_on,
              iconColor: AppColors.accentOrange,
              title: l10n?.hdrSupport ?? 'HDR Support',
              value: _hdrSupport,
            ),
            ModernListTile(
              leadingIcon: Icons.access_time,
              iconColor: AppColors.accentCyan,
              title: l10n?.alwaysOnDisplay ?? 'Always-on Display',
              value: _alwaysOnDisplay,
            ),
            ModernListTile(
              leadingIcon: Icons.memory,
              iconColor: AppColors.accentGreen,
              title: l10n?.openGlEsVersion ?? 'OpenGL ES',
              value: _openGlEsVersion,
            ),
          ],
        ),
      ),
    );
  }

  void _showCameraInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.cameraInfo ?? 'Camera Information',
          icon: Icons.camera_alt,
          accentColor: AppColors.accentOrange,
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.cameraSpecs ?? 'Camera Specifications'),
            ModernListTile(
              leadingIcon: Icons.camera_alt,
              iconColor: AppColors.accentOrange,
              title: l10n?.cameraCount ?? 'Camera Count',
              value: '$_cameraCount',
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.camera_rear,
              iconColor: AppColors.accentOrange,
              title: l10n?.backCameraResolution ?? 'Back Camera',
              value: _backCameraResolution,
            ),
            ModernListTile(
              leadingIcon: Icons.camera_front,
              iconColor: AppColors.accentOrange,
              title: l10n?.frontCameraResolution ?? 'Front Camera',
              value: _frontCameraResolution,
            ),
            ModernListTile(
              leadingIcon: Icons.center_focus_strong,
              iconColor: AppColors.accentPurple,
              title: l10n?.autofocus ?? 'Autofocus',
              value: _hasAutofocus ? (l10n?.yes ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.hardware,
              iconColor: AppColors.primaryStart,
              title: l10n?.hardwareLevel ?? 'Hardware Level',
              value: _cameraHardwareLevel,
            ),
          ],
        ),
      ),
    );
  }

  void _showPerformanceInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: '${l10n?.performance ?? 'Performance'} (CPU/GPU)',
          icon: Icons.speed,
          accentColor: AppColors.accentGreen,
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.cpuInfo ?? 'CPU Information'),
            ModernListTile(
              leadingIcon: Icons.memory,
              iconColor: AppColors.accentGreen,
              title: l10n?.cpuCores ?? 'CPU Cores',
              value: _cpuCoreCount,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.architecture,
              iconColor: AppColors.accentGreen,
              title: l10n?.cpuArchitecture ?? 'Architecture',
              value: _cpuArchitecture,
            ),
            ModernListTile(
              leadingIcon: Icons.computer,
              iconColor: AppColors.accentGreen,
              title: l10n?.cpuModel ?? 'CPU Model',
              value: _cpuPart,
            ),
            ModernListTile(
              leadingIcon: Icons.speed,
              iconColor: AppColors.accentYellow,
              title: l10n?.maxFrequency ?? 'Max Frequency',
              value: _cpuMaxFrequencies,
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.gpuInfo ?? 'GPU Information'),
            ModernListTile(
              leadingIcon: Icons.memory,
              iconColor: AppColors.accentPurple,
              title: l10n?.gpuModel ?? 'GPU Model',
              value: _gpuModel,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.games,
              iconColor: AppColors.accentPurple,
              title: l10n?.openGlEsVersion ?? 'OpenGL ES',
              value: _openGlEsVersion,
            ),
            ModernListTile(
              leadingIcon: Icons.developer_board,
              iconColor: AppColors.primaryStart,
              title: l10n?.hardwarePlatform ?? 'Hardware Platform',
              value: _hardwarePlatform,
            ),
          ],
        ),
      ),
    );
  }

  void _showStorageInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.storageInfo ?? 'Storage Information',
          icon: Icons.storage,
          accentColor: AppColors.accentYellow,
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.internalStorage ?? 'Internal Storage'),
            ModernListTile(
              leadingIcon: Icons.storage,
              iconColor: AppColors.accentYellow,
              title: l10n?.totalStorage ?? 'Total Storage',
              value: _totalStorage,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.folder,
              iconColor: AppColors.accentOrange,
              title: l10n?.usedStorage ?? 'Used Storage',
              value: _usedStorage,
            ),
            ModernListTile(
              leadingIcon: Icons.check_circle,
              iconColor: AppColors.accentGreen,
              title: l10n?.availableStorage ?? 'Available Storage',
              value: _availableStorage,
            ),
            if (_hasSDCard) ...[
              const Divider(height: 32),
              SectionHeader(title: l10n?.sdCard ?? 'SD Card'),
              ModernListTile(
                leadingIcon: Icons.sd_storage,
                iconColor: AppColors.accentCyan,
                title: l10n?.sdCardTotal ?? 'SD Card Total',
                value: _sdCardTotal,
                isBold: true,
              ),
            ],
          ],
        ),
      ),
    );
  }

  void _showNetworkInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.networkInfo ?? 'Network Information',
          icon: Icons.wifi,
          accentColor: const Color(0xFF4dabf7),
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.connectionInfo ?? 'Connection Info'),
            ModernListTile(
              leadingIcon: Icons.network_cell,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.networkType ?? 'Network Type',
              value: _networkType,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.wifi,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.ipAddress ?? 'IP Address',
              value: _ipAddress,
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.wifiDetails ?? 'Wi-Fi Details'),
            ModernListTile(
              leadingIcon: Icons.wifi_tethering,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.wifiStandard ?? 'Wi-Fi Standard',
              value: _wifiStandard,
            ),
            ModernListTile(
              leadingIcon: Icons.wifi_channel,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.wifiFrequency ?? 'Frequency',
              value: _wifiFrequency,
            ),
            ModernListTile(
              leadingIcon: Icons.speed,
              iconColor: AppColors.accentGreen,
              title: l10n?.connectionSpeed ?? 'Connection Speed',
              value: _wifiLinkSpeed,
            ),
            ModernListTile(
              leadingIcon: Icons.signal_cellular_alt,
              iconColor: AppColors.accentGreen,
              title: l10n?.signalStrength ?? 'Signal Strength',
              value: _wifiSignalStrength != 'Unknown' 
                  ? '$_wifiSignalStrength dBm ($_wifiSignalQuality)' 
                  : 'Unknown',
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.macAddresses ?? 'MAC Addresses'),
            ModernListTile(
              leadingIcon: Icons.wifi_lock,
              iconColor: AppColors.accentPurple,
              title: l10n?.wifiMac ?? 'Wi-Fi MAC',
              value: _wifiMac,
            ),
            ModernListTile(
              leadingIcon: Icons.bluetooth,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.bluetoothMac ?? 'Bluetooth MAC',
              value: _bluetoothMac,
            ),
            ModernListTile(
              leadingIcon: Icons.bluetooth_audio,
              iconColor: const Color(0xFF4dabf7),
              title: l10n?.bluetoothName ?? 'Bluetooth Name',
              value: _bluetoothName,
            ),
            ModernListTile(
              leadingIcon: Icons.sim_card,
              iconColor: AppColors.accentOrange,
              title: l10n?.imei ?? 'IMEI',
              value: _imei,
            ),
          ],
        ),
      ),
    );
  }

  void _showSensorsInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.sensors ?? 'Sensors',
          icon: Icons.sensors,
          accentColor: const Color(0xFFff8787),
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.biometricSensors ?? 'Biometric Sensors'),
            ModernListTile(
              leadingIcon: Icons.fingerprint,
              iconColor: _hasFingerprint ? AppColors.accentGreen : Colors.grey,
              title: l10n?.fingerprint ?? 'Fingerprint',
              value: _hasFingerprint 
                  ? '${l10n?.available ?? 'Available'}${_fingerprintLocation.isNotEmpty ? " ($_fingerprintLocation)" : ""}' 
                  : (l10n?.notAvailable ?? 'Not Available'),
            ),
            ModernListTile(
              leadingIcon: Icons.face,
              iconColor: _hasFaceRecognition ? AppColors.accentGreen : Colors.grey,
              title: l10n?.faceRecognition ?? 'Face Recognition',
              value: _hasFaceRecognition 
                  ? (l10n?.available ?? 'Available') 
                  : (l10n?.notAvailable ?? 'Not Available'),
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.environmentalSensors ?? 'Environmental Sensors'),
            ModernListTile(
              leadingIcon: Icons.sensors,
              iconColor: _hasProximity ? AppColors.accentGreen : Colors.grey,
              title: l10n?.proximitySensor ?? 'Proximity Sensor',
              value: _hasProximity ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.wb_sunny,
              iconColor: _hasLightSensor ? AppColors.accentGreen : Colors.grey,
              title: l10n?.lightSensor ?? 'Light Sensor',
              value: _hasLightSensor ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.speed,
              iconColor: _hasBarometer ? AppColors.accentGreen : Colors.grey,
              title: l10n?.barometer ?? 'Barometer',
              value: _hasBarometer ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.thermostat,
              iconColor: _hasThermometer ? AppColors.accentGreen : Colors.grey,
              title: l10n?.thermometer ?? 'Thermometer',
              value: _hasThermometer ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            const Divider(height: 32),
            SectionHeader(title: l10n?.motionSensors ?? 'Motion Sensors'),
            ModernListTile(
              leadingIcon: Icons.vibration,
              iconColor: _hasAccelerometer ? AppColors.accentGreen : Colors.grey,
              title: l10n?.accelerometer ?? 'Accelerometer',
              value: _hasAccelerometer ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.rotate_90_degrees_ccw,
              iconColor: _hasGyroscope ? AppColors.accentGreen : Colors.grey,
              title: l10n?.gyroscope ?? 'Gyroscope',
              value: _hasGyroscope ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
            ModernListTile(
              leadingIcon: Icons.explore,
              iconColor: _hasCompass ? AppColors.accentGreen : Colors.grey,
              title: l10n?.compass ?? 'Compass',
              value: _hasCompass ? (l10n?.available ?? 'Yes') : (l10n?.no ?? 'No'),
            ),
          ],
        ),
      ),
    );
  }

  void _showBatteryInfo(AppLocalizations? l10n) {
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => CategoryDetailScreen(
          title: l10n?.batteryInfo ?? 'Battery Information',
          icon: Icons.battery_full,
          accentColor: const Color(0xFF69db7c),
          l10n: l10n,
          children: [
            SectionHeader(title: l10n?.batterySpecs ?? 'Battery Specifications'),
            ModernListTile(
              leadingIcon: Icons.battery_charging_full,
              iconColor: const Color(0xFF69db7c),
              title: l10n?.batteryTechnology ?? 'Technology',
              value: _batteryTechnology,
              isBold: true,
            ),
            ModernListTile(
              leadingIcon: Icons.thermostat,
              iconColor: AppColors.accentOrange,
              title: l10n?.batteryTemperature ?? 'Temperature',
              value: _batteryTemperature,
            ),
            ModernListTile(
              leadingIcon: Icons.electric_bolt,
              iconColor: AppColors.accentYellow,
              title: l10n?.batteryVoltage ?? 'Voltage',
              value: _batteryVoltage,
            ),
            ModernListTile(
              leadingIcon: Icons.wifi_tethering,
              iconColor: _isWirelessCharging ? AppColors.accentGreen : Colors.grey,
              title: l10n?.wirelessCharging ?? 'Wireless Charging',
              value: _isWirelessCharging ? (l10n?.supported ?? 'Supported') : (l10n?.notSupported ?? 'Not Supported'),
            ),
          ],
        ),
      ),
    );
  }
}
