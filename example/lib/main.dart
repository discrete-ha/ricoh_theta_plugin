import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ricoh_theta_plugin/ricoh_theta_plugin.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _imageUri;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    String platformVersion;
    try {
      platformVersion = await RicohThetaPlugin.platformVersion;
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  Future startCamera() async {
    try {
      String imageUri = await RicohThetaPlugin.startCamera();
      setState(() => this._imageUri = imageUri);
    } on PlatformException catch (e) {
      print('error: $e');
    } on FormatException{
      print('null (User returned using the "back"-button before scanning anything. Result)');
    } catch (e) {
      print('error: $e');
    }
  }

  Widget _showUri(){
    if(_imageUri != null && _imageUri.isNotEmpty){
      File file = new File(_imageUri.replaceAll("file:", ""));
      return new Container(
          width: 300.0,
          child:new Column(
            children:[
              Text(
                '$_imageUri\n',
                style: TextStyle(fontSize: 10),
              ),
              new Image.file(
                file,
                width: 600.0,
                height: 240.0,
                fit: BoxFit.cover,
              ),
            ]
          )


      );
    }
    return Container();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Ricoh Theta Plugin Sample App'),
        ),
        body: Center(
            child:
            new Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children:[
                  Text('Running on: $_platformVersion\n'),
                  _showUri(),
                  RaisedButton(
                    splashColor: Colors.pinkAccent,
                    color: Colors.black,
                    child: new Text(
                      "Start Camera",
                      style: new TextStyle(fontSize: 20.0, color: Colors.white),
                    ),
                    onPressed: () {
                      startCamera();
                    },
                  ),
                ])
        ),
      ),
    );
  }
}
