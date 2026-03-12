import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

// Translation files
import 'translations/en_translations.dart';
import 'translations/tr_translations.dart';
import 'translations/de_translations.dart';
import 'translations/ar_translations.dart';
import 'translations/zh_translations.dart';
import 'translations/es_translations.dart';
import 'translations/hi_translations.dart';
import 'translations/pt_translations.dart';
import 'translations/ru_translations.dart';

class AppLocalizations {
  final Locale locale;
  
  AppLocalizations(this.locale);
  
  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }
  
  static const LocalizationsDelegate<AppLocalizations> delegate = _AppLocalizationsDelegate();
  
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates = <LocalizationsDelegate<dynamic>>[
    delegate,
    GlobalMaterialLocalizations.delegate,
    GlobalWidgetsLocalizations.delegate,
    GlobalCupertinoLocalizations.delegate,
  ];
  
  static const List<Locale> supportedLocales = <Locale>[
    Locale('en'),
    Locale('tr'),
    Locale('de'),
    Locale('ar'),
    Locale('zh'),
    Locale('es'),
    Locale('hi'),
    Locale('pt'),
    Locale('ru'),
  ];
  
  static final Map<String, Map<String, String>> _translationMap = {
    'en': EnTranslations.translations,
    'tr': TrTranslations.translations,
    'de': DeTranslations.translations,
    'ar': ArTranslations.translations,
    'zh': ZhTranslations.translations,
    'es': EsTranslations.translations,
    'hi': HiTranslations.translations,
    'pt': PtTranslations.translations,
    'ru': RuTranslations.translations,
  };
  
  Map<String, String> get _localizedStrings {
    return _translationMap[locale.languageCode] ?? _translationMap['en']!;
  }
  
  Future<bool> load() async {
    return true;
  }
  
  String translate(String key) {
    return _localizedStrings[key] ?? key;
  }
  
  // Getters for all localized strings
  String get appTitle => translate('appTitle');
  String get selectLanguage => translate('selectLanguage');
  String get continueButton => translate('continueButton');
  String get deviceInformation => translate('deviceInformation');
  String get generalInformation => translate('generalInformation');
  String get phoneModel => translate('phoneModel');
  String get deviceCodeName => translate('deviceCodeName');
  String get deviceCodeNameTooltip => translate('deviceCodeNameTooltip');
  String get operatingSystem => translate('operatingSystem');
  String get platformInformation => translate('platformInformation');
  String get detailedModelInfo => translate('detailedModelInfo');
  String get hardwarePlatform => translate('hardwarePlatform');
  String get hardwareInformation => translate('hardwareInformation');
  String get ramMemory => translate('ramMemory');
  String get cpuArchitecture => translate('cpuArchitecture');
  String get board => translate('board');
  String get camera => translate('camera');
  String get supported => translate('supported');
  String get sensors => translate('sensors');
  String get loading => translate('loading');
  String get unknown => translate('unknown');
  String get settings => translate('settings');
  String get language => translate('language');
  String get theme => translate('theme');
  String get lightTheme => translate('lightTheme');
  String get darkTheme => translate('darkTheme');
  String get english => translate('english');
  String get turkish => translate('turkish');
  String get cameraCount => translate('cameraCount');
  String get backCamera => translate('backCamera');
  String get frontCamera => translate('frontCamera');
  String get autofocus => translate('autofocus');
  String get flash => translate('flash');
  String get cameraHardwareLevel => translate('cameraHardwareLevel');
  String get yes => translate('yes');
  String get no => translate('no');
  String get deviceIdentity => translate('deviceIdentity');
  String get manufacturer => translate('manufacturer');
  String get brand => translate('brand');
  String get productCode => translate('productCode');
  String get serialNumber => translate('serialNumber');
  String get bootloader => translate('bootloader');
  String get radioVersion => translate('radioVersion');
  String get storageInformation => translate('storageInformation');
  String get totalStorage => translate('totalStorage');
  String get usedStorage => translate('usedStorage');
  String get availableStorage => translate('availableStorage');
  String get sdCard => translate('sdCard');
  String get batteryInformation => translate('batteryInformation');
  String get batteryLevel => translate('batteryLevel');
  String get batteryStatus => translate('batteryStatus');
  String get chargeType => translate('chargeType');
  String get batteryTemperature => translate('batteryTemperature');
  String get batteryVoltage => translate('batteryVoltage');
  String get batteryTechnology => translate('batteryTechnology');
  String get cpuPerformance => translate('cpuPerformance');
  String get cpuCoreCount => translate('cpuCoreCount');
  String get cpuModel => translate('cpuModel');
  String get cpuFrequency => translate('cpuFrequency');
  String get gpuModel => translate('gpuModel');
  String get gpuInformation => translate('gpuInformation');
  String get openGlEsVersion => translate('openGlEsVersion');
  String get sensorInformation => translate('sensorInformation');
  String get fingerprint => translate('fingerprint');
  String get faceRecognition => translate('faceRecognition');
  String get proximitySensor => translate('proximitySensor');
  String get lightSensor => translate('lightSensor');
  String get barometer => translate('barometer');
  String get thermometer => translate('thermometer');
  String get networkInformation => translate('networkInformation');
  String get networkType => translate('networkType');
  String get ipAddress => translate('ipAddress');
  String get wifiMac => translate('wifiMac');
  String get bluetoothMac => translate('bluetoothMac');
  String get bluetoothName => translate('bluetoothName');
  String get imei => translate('imei');
  String get softwareSecurity => translate('softwareSecurity');
  String get androidVersion => translate('androidVersion');
  String get securityPatch => translate('securityPatch');
  String get kernelVersion => translate('kernelVersion');
  String get gmsVersion => translate('gmsVersion');
  String get rootStatus => translate('rootStatus');
  String get displayDetails => translate('displayDetails');
  String get panelType => translate('panelType');
  String get refreshRate => translate('refreshRate');
  String get hdrSupport => translate('hdrSupport');
  String get alwaysOnDisplay => translate('alwaysOnDisplay');
  String get otherFeatures => translate('otherFeatures');
  String get nfc => translate('nfc');
  String get irBlaster => translate('irBlaster');
  String get stereoSpeakers => translate('stereoSpeakers');
  String get waterResistance => translate('waterResistance');
  String get fastCharging => translate('fastCharging');
  String get enabled => translate('enabled');
  String get disabled => translate('disabled');
  String get ramInformation => translate('ramInformation');
  String get motherboardInformation => translate('motherboardInformation');
  String get cameraInformation => translate('cameraInformation');
  String get totalRAM => translate('totalRAM');
  String get usedRAM => translate('usedRAM');
  String get availableRAM => translate('availableRAM');
  String get ramUsagePercent => translate('ramUsagePercent');
  String get hardware => translate('hardware');
  String get device => translate('device');
  String get total => translate('total');
  String get wifiLinkSpeed => translate('wifiLinkSpeed');
  String get wifiSignalStrength => translate('wifiSignalStrength');
  String get wifiSignalQuality => translate('wifiSignalQuality');
  String get wifiFrequency => translate('wifiFrequency');
  String get wifiStandard => translate('wifiStandard');
  String get connectionSpeed => translate('connectionSpeed');
  String get signalStrength => translate('signalStrength');
  String get categories => translate('categories');
  String get systemStatus => translate('systemStatus');
  String get analyzingDevice => translate('analyzingDevice');
  String get quickInfo => translate('quickInfo');
  String get items => translate('items');
  String get available => translate('available');
  String get notAvailable => translate('notAvailable');
  String get notSupported => translate('notSupported');
  String get deviceInfo => translate('deviceInfo');
  String get systemInfo => translate('systemInfo');
  String get displaySpecs => translate('displaySpecs');
  String get cameraSpecs => translate('cameraSpecs');
  String get ramUsage => translate('ramUsage');
  String get storageUsage => translate('storageUsage');
  String get connectionInfo => translate('connectionInfo');
  String get wifiDetails => translate('wifiDetails');
  String get macAddresses => translate('macAddresses');
  String get biometricSensors => translate('biometricSensors');
  String get environmentalSensors => translate('environmentalSensors');
  String get motionSensors => translate('motionSensors');
  String get batterySpecs => translate('batterySpecs');
  String get internalStorage => translate('internalStorage');
  String get cpuCores => translate('cpuCores');
  String get maxFrequency => translate('maxFrequency');
  String get performance => translate('performance');
  String get cameraInfo => translate('cameraInfo');
  String get storageInfo => translate('storageInfo');
  String get networkInfo => translate('networkInfo');
  String get batteryInfo => translate('batteryInfo');
  String get generalInfo => translate('generalInfo');
  String get deviceModel => translate('deviceModel');
  String get cameras => translate('cameras');
  String get backCameraResolution => translate('backCameraResolution');
  String get frontCameraResolution => translate('frontCameraResolution');
  String get hardwareLevel => translate('hardwareLevel');
  String get gpuInfo => translate('gpuInfo');
  String get cpuInfo => translate('cpuInfo');
  String get sdCardTotal => translate('sdCardTotal');
  String get accelerometer => translate('accelerometer');
  String get gyroscope => translate('gyroscope');
  String get compass => translate('compass');
  String get wirelessCharging => translate('wirelessCharging');
  String get deviceFeatures => translate('deviceFeatures');
  String get deviceFeaturesSubtitle => translate('deviceFeaturesSubtitle');
}

class _AppLocalizationsDelegate extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();
  
  @override
  bool isSupported(Locale locale) {
    return ['en', 'tr', 'de', 'ar', 'zh', 'es', 'hi', 'pt', 'ru'].contains(locale.languageCode);
  }
  
  @override
  Future<AppLocalizations> load(Locale locale) {
    final localizations = AppLocalizations(locale);
    return localizations.load().then((_) => localizations);
  }
  
  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}
