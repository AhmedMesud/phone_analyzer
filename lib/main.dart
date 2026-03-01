import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'l10n/app_localizations.dart';
import 'screens/language_selection_screen.dart';
import 'screens/home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();

  static _MyAppState of(BuildContext context) {
    return context.findAncestorStateOfType<_MyAppState>()!;
  }
}

class _MyAppState extends State<MyApp> {
  static const platform = MethodChannel('com.example.phone_analyzer/device');
  ThemeMode _themeMode = ThemeMode.dark;
  Locale _locale = const Locale('en');
  bool _isFirstLaunch = true;
  bool _isLoading = true;

  // Desteklenen diller (alfabetik sırayla)
  final List<Map<String, dynamic>> _languages = [
    {'code': 'ar', 'name': 'Arabic', 'nativeName': 'العربية', 'flag': '🇸🇦'},
    {'code': 'zh', 'name': 'Chinese', 'nativeName': '中文', 'flag': '🇨🇳'},
    {'code': 'de', 'name': 'German', 'nativeName': 'Deutsch', 'flag': '🇩🇪'},
    {'code': 'en', 'name': 'English', 'nativeName': 'English', 'flag': '🇬🇧'},
    {'code': 'es', 'name': 'Spanish', 'nativeName': 'Español', 'flag': '🇪🇸'},
    {'code': 'hi', 'name': 'Hindi', 'nativeName': 'हिन्दी', 'flag': '🇮🇳'},
    {'code': 'pt', 'name': 'Portuguese', 'nativeName': 'Português', 'flag': '🇧🇷'},
    {'code': 'ru', 'name': 'Russian', 'nativeName': 'Русский', 'flag': '🇷🇺'},
    {'code': 'tr', 'name': 'Turkish', 'nativeName': 'Türkçe', 'flag': '🇹🇷'},
  ];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadSettings();
    });
  }

  Future<void> _loadSettings() async {
    try {
      final Map<dynamic, dynamic> result = await platform.invokeMethod('getLanguage');
      final String savedLanguage = result['language'] ?? 'en';
      final bool firstLaunch = result['firstLaunch'] ?? true;

      setState(() {
        _locale = Locale(savedLanguage);
        _isFirstLaunch = firstLaunch;
        _isLoading = false;
      });
    } catch (e) {
      setState(() {
        _locale = const Locale('en');
        _isFirstLaunch = true;
        _isLoading = false;
      });
    }
  }

  Future<void> _saveLanguage(String languageCode) async {
    try {
      await platform.invokeMethod('saveLanguage', {'languageCode': languageCode});
    } catch (e) {
      // Hata durumunda sessizce devam et
    }
  }

  void setLocale(Locale locale) {
    setState(() {
      _locale = locale;
    });
    _saveLanguage(locale.languageCode);
  }

  void setThemeMode(ThemeMode mode) {
    setState(() {
      _themeMode = mode;
    });
  }

  void _toggleTheme(bool isDark) {
    setThemeMode(isDark ? ThemeMode.dark : ThemeMode.light);
  }

  void _setLanguageAndContinue(String languageCode) {
    setLocale(Locale(languageCode));
    setState(() {
      _isFirstLaunch = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const MaterialApp(
        home: Scaffold(
          body: Center(child: CircularProgressIndicator()),
        ),
      );
    }

    final isRtl = _locale.languageCode == 'ar';

    return MaterialApp(
      title: 'Phone Analyzer',
      debugShowCheckedModeBanner: false,
      themeMode: _themeMode,
      locale: _locale,
      supportedLocales: _languages.map((l) => Locale(l['code'])).toList(),
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blue,
          brightness: Brightness.light,
        ),
        useMaterial3: true,
      ),
      darkTheme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.blue,
          brightness: Brightness.dark,
        ),
        useMaterial3: true,
      ),
      builder: (context, child) {
        return Directionality(
          textDirection: isRtl ? TextDirection.rtl : TextDirection.ltr,
          child: child!,
        );
      },
      home: _isFirstLaunch
          ? LanguageSelectionScreen(
              languages: _languages,
              onLanguageSelected: _setLanguageAndContinue,
              isDarkMode: _themeMode == ThemeMode.dark,
            )
          : HomeScreen(
              key: ValueKey(_locale.languageCode), // Locale değişiminde yeniden oluştur
              isDarkMode: _themeMode == ThemeMode.dark,
              onThemeChanged: _toggleTheme,
              languages: _languages,
              currentLocale: _locale,
              onLanguageChanged: setLocale,
            ),
    );
  }
}
