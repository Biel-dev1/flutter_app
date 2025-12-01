import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:permission_handler/permission_handler.dart';
import 'dart:io';
import 'services/accessibility_service.dart';

void main() {
  runApp(const AccessApp());
}

class AccessApp extends StatelessWidget {
  const AccessApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Access App',
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF2196F3)),
        appBarTheme: const AppBarTheme(elevation: 0, backgroundColor: Color(0xFF2196F3), foregroundColor: Colors.white),
      ),
      home: const HomePage(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  File? _selectedImage;
  bool _isAccessibilityEnabled = false;
  String _statusMessage = 'Verificando...';
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _checkAccessibilityStatus();
  }

  Future<void> _pickImage() async {
    try {
      final status = await Permission.storage.request();
      if (status.isGranted) {
        final picker = ImagePicker();
        final pickedFile = await picker.pickImage(source: ImageSource.gallery);
        if (pickedFile != null) {
          setState(() => _selectedImage = File(pickedFile.path));
        }
      }
    } catch (e) {
      _showSnackBar('Erro ao selecionar foto');
    }
  }

  Future<void> _checkAccessibilityStatus() async {
    setState(() => _isLoading = true);
    try {
      final isEnabled = await AccessibilityServiceHelper.isAccessibilityEnabled();
      setState(() {
        _isAccessibilityEnabled = isEnabled;
        _statusMessage = isEnabled ? '✓ ATIVADO' : '✗ DESATIVADO';
      });
    } finally {
      setState(() => _isLoading = false);
    }
  }

  void _openAccessibilitySettings() {
    AccessibilityServiceHelper.openAccessibilitySettings();
  }

  void _showSnackBar(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), duration: const Duration(seconds: 3)),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Access App', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
        centerTitle: true,
      ),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text('📸 Galeria de Fotos', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: const Color(0xFF2196F3), fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              ElevatedButton.icon(onPressed: _pickImage, icon: const Icon(Icons.photo_library), label: const Text('Escolher Foto')),
              const SizedBox(height: 16),
              Container(
                height: 200,
                decoration: BoxDecoration(borderRadius: BorderRadius.circular(16), color: Colors.grey[200]),
                child: _selectedImage != null
                    ? ClipRRect(borderRadius: BorderRadius.circular(14), child: Image.file(_selectedImage!, fit: BoxFit.cover))
                    : Center(child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [Icon(Icons.image, size: 48, color: Colors.grey[400]), const SizedBox(height: 12), Text('Nenhuma foto', style: TextStyle(color: Colors.grey[600]))])),
              ),
              const SizedBox(height: 32),
              Text('🔐 Acessibilidade', style: Theme.of(context).textTheme.titleLarge?.copyWith(color: const Color(0xFFFF9800), fontWeight: FontWeight.bold)),
              const SizedBox(height: 12),
              ElevatedButton.icon(onPressed: _openAccessibilitySettings, icon: const Icon(Icons.security), label: const Text('Ativar Acessibilidade'), style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFFFF9800))),
              const SizedBox(height: 12),
              ElevatedButton.icon(onPressed: _isLoading ? null : _checkAccessibilityStatus, icon: const Icon(Icons.refresh), label: const Text('Verificar Status'), style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF4CAF50))),
              const SizedBox(height: 24),
              Card(
                elevation: 4,
                child: Container(
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(borderRadius: BorderRadius.circular(12), color: _isAccessibilityEnabled ? Colors.green[50] : Colors.red[50]),
                  child: Column(
                    children: [
                      Icon(_isAccessibilityEnabled ? Icons.check_circle : Icons.cancel, size: 48, color: _isAccessibilityEnabled ? Colors.green : Colors.red),
                      const SizedBox(height: 12),
                      Text(_statusMessage, style: Theme.of(context).textTheme.titleLarge?.copyWith(color: _isAccessibilityEnabled ? Colors.green : Colors.red, fontWeight: FontWeight.bold)),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
