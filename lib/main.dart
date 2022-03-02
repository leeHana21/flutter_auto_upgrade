import 'package:app_installer/app_installer.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:flutter_app_installer/flutter_app_installer.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key? key, required this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Permission _permission = Permission.requestInstallPackages;
  PermissionStatus _permissionStatus = PermissionStatus.denied;

  static const channel = const MethodChannel("DOWNLOAD_CHANNEL");
  static const String apkDownLoadMethod = "APK_DOWNLOAD";
  static const String apkInstallMethod = "APK_INSTALL";

  @override
  void initState() {
    super.initState();
    _listenForPermissionStatus();
  }

  void _listenForPermissionStatus() async {
    final status = await _permission.status;
    setState(() => _permissionStatus = status);
  }

  Future<void> requestPermission(Permission permission) async {
    final status = await permission.request();
    setState(() {
      print(status);
      _permissionStatus = status;
      print(_permissionStatus);
    });
    if (_permissionStatus == PermissionStatus.granted) {
      _startApkDownLoad();
    }
  }

  Future<void> _startApkDownLoad() async {
    final String status = "SUCCESS";
    String functionStatus = "";
    try {
      const apkDownLoadLink = {
        'apkLink':
            "https://firebasestorage.googleapis.com/v0/b/ajoumc-bedsores.appspot.com/o/app-release.apk?alt=media&token=db856d97-b35e-48c2-a8df-5397c091be4b"
      };
      final String result =
          await channel.invokeMethod(apkDownLoadMethod, apkDownLoadLink);
      functionStatus = result;
      print("Result of downLoad !!!!! ==> $functionStatus");
      if (functionStatus == status) {
        //_startApkInstall();
      }
    } on PlatformException catch (e) {
      functionStatus = "sdk result = ${e.message}";
      print("Result of downLoad platform Exception  !!!!! ==> $functionStatus");
    }
  }

  Future<void> _startApkInstall() async {
    final String status = "SUCCESS";
    try {
      final String result = await channel.invokeMethod(apkInstallMethod);
      if (result == status) {
        print("Result of Install $result");
      }
    } on PlatformException catch (e) {
      print("Result of Install platform exception $e");
    }
  }

  int _counter = 0;

  void _incrementCounter() {
    setState(() {
      _counter++;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text(
              _permissionStatus.toString(),
            ),
            Text(
              '$_counter',
              style: Theme.of(context).textTheme.headline4,
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          requestPermission(_permission);
          _incrementCounter();
        },
        tooltip: 'Increment',
        child: Icon(Icons.add),
      ),
    );
  }
}
