
# BonjourConnect

![AndroidStudio 3.4](https://img.shields.io/badge/Android_Studio-3.3.2-brightgreen.svg)
![minSDK 16](https://img.shields.io/badge/minSDK-API_16-orange.svg?style=flat)
  ![targetSDK 28](https://img.shields.io/badge/targetSDK-API_28-blue.svg)

`BonjourConnect` wraps the boilerplate code that's needed to find a service on a network.

## Installation

Add `jitpack`to your repositories
```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
    ...
}
```

Add this dependency to your app _build.gradle_:
```gradle
implementation 'com.github.grumpyshoe:android-module-bonjourconnect:1.0.0'
```

## Usage

Get instance of BonjourConnect:
```kotlin
val bonjourConnect: BonjourConnect by lazy { BonjourConnectImpl(applicationContext) }
```

Start search for the network service type you are looking for:
```kotlin
bonjourConnect.getServiceInfo(
    type = "_my_service_type._tcp.",
    onServiceInfoReceived = { networkService ->
        ...
    },
    onError = { errorType ->
        ...
    })
```

If the requested service is found the functin located at `onServiceInfoReceived` is invoked with a parameter of type _NetworkService_. The object contains detail information about _name_, _type_, _host_ and _port_. of the service/server.

If no service is found within 3 seconds (default timeout) the function located at `onError` is invoked with a parameter of type _BonjourConnect.ErrorType_.

## Customize

### Timeout
If you want to in- or decrease the timeout you can define the delay in milliseconds at parameter `searchTimeout`:
```kotlin
bonjourConnect.getServiceInfo(
    type = "_my_service_type._tcp.",
    onServiceInfoReceived = { networkService ->
        ...
    },
    onError = { errorType ->
        ...
    },
    searchTimeout = 5000L)
```


## Need Help or something missing?

Please [submit an issue](https://github.com/grumpyshoe/android-module-bonjourconnect/issues) on GitHub.


## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE) file.


#### Build Environment
```
Android Studio 3.4
Build #AI-183.5429.30.34.5452501, built on April 10, 2019
JRE: 1.8.0_152-release-1343-b01 x86_64
JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
macOS 10.14
```
