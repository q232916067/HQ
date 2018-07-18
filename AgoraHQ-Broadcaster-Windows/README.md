# Agora Live Quiz host client on Windows

*Other languages: [简体中文](README.zh.md)*

The AgoraHQ Windows Sample App is an open-source demo that will help you get live video chat integrated directly into your windows applications using the Agora Video SDK. This part is the function of HQ Three
- Update the Language pack
- fix the crash when the number of choose less than four
- add operation navigation
- add update config restart

With this sample app, you can:
- Role: host guide broadcast
- Media section: Live Speaker; yy partner to achieve video effects
- Signaling part: control the answer

This demo is written in **C++**

Biz service for Agora live quiz

- [Live-Quiz-Server-Nodejs](https://github.com/AgoraIO/HQ/tree/master/AgoraHQ-Server-Nodejs)

Agora Video SDK supports iOS / Android / Windows etc. You can find demos of these platform here:

- [Live-Quiz-iOS](https://github.com/AgoraIO/HQ/tree/Solution-for-TeamUpMode-Http/AgoraHQ-iOS-Swift)
- [Live-Quiz-Android](https://github.com/AgoraIO/HQ/tree/Solution-for-TeamUpMode-Http/AgoraHQ-Android)

## Running the App
First, create a developer account at [Agora.io](https://dashboard.agora.io/signin/), and obtain an App ID. 

```
[LoginInfo]

AppId=

AppCertificateId=

LoginUid=

AppCertEnable=

VideoSolutinIndex=

CameraDeviceName=

CameraDeviceID=
```

Finally, Open AgoraHQ.sln with your Visual Studio and build all solution and run.

There is a Restart.bat script in the home directory. You need to copy the script to the execution directory during the actual running.

## Developer Environment Requirements
* Visual Studio 2013(or higher)
* Windows 7(or higher)

## Connect Us

- You can find full API document at [Document Center](https://docs.agora.io/en/)
- You can file bugs about this demo at [issue](https://github.com/AgoraIO/HQ/issues)

## License

The MIT License (MIT).
