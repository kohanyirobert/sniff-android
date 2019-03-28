# Requirements

* JDK 1.8
* Gradle 3.3
* Create a virtual Android device. After launching it update the emulator's config regarding rendering capabilitites ([see this link](https://stackoverflow.com/questions/50595704)). Otherwise the emulator just shows a black screen.
* Restart the emulator.
* `./gradlew installDebug` (or `installRelease`) to install app.
* `./gradlew initSettings` to initialize app settings automatically using the `local.properties`.
* `./gradlew connectedAndroidTest` to run instrumented tests on device.

**Note**: wanted to update the build files and stuff, but couldn't get to run instrumented tests from Android Studio.
If I update anymore dependencies after *this* point I cannot run test even from the terminal with `gradle`.
