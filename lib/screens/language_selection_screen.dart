import 'package:flutter/material.dart';

class LanguageSelectionScreen extends StatelessWidget {
  final List<Map<String, dynamic>> languages;
  final Function(String) onLanguageSelected;
  final bool isDarkMode;

  const LanguageSelectionScreen({
    super.key,
    required this.languages,
    required this.onLanguageSelected,
    required this.isDarkMode,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: isDarkMode ? Colors.black : Colors.white,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 40),
              const Icon(
                Icons.language,
                size: 80,
                color: Colors.blue,
              ),
              const SizedBox(height: 24),
              const Text(
                'Phone Analyzer',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Select Language / Dil Seçimi / Выбрать язык',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  color: isDarkMode ? Colors.grey.shade400 : Colors.grey.shade600,
                ),
              ),
              const SizedBox(height: 40),
              Expanded(
                child: ListView.builder(
                  itemCount: languages.length,
                  itemBuilder: (context, index) {
                    final lang = languages[index];
                    final isSelected = lang['code'] == 'en';
                    return Card(
                      elevation: isSelected ? 4 : 1,
                      color: isSelected
                          ? (isDarkMode ? Colors.blue.shade900 : Colors.blue.shade50)
                          : null,
                      child: ListTile(
                        leading: Text(
                          _getFlagEmoji(lang['code']),
                          style: const TextStyle(fontSize: 24),
                        ),
                        title: Text(
                          lang['nativeName'],
                          style: TextStyle(
                            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                          ),
                        ),
                        subtitle: Text(lang['name']),
                        trailing: isSelected
                            ? const Icon(Icons.check_circle, color: Colors.blue)
                            : null,
                        onTap: () => onLanguageSelected(lang['code']),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _getFlagEmoji(String code) {
    final flags = {
      'ar': '🇸🇦',
      'zh': '🇨🇳',
      'de': '🇩🇪',
      'en': '🇬🇧',
      'es': '🇪🇸',
      'hi': '🇮🇳',
      'pt': '🇵🇹',
      'ru': '🇷🇺',
      'tr': '🇹🇷',
    };
    return flags[code] ?? '🌐';
  }
}
