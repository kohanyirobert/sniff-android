# Requirements

* JDK 1.8
* Gradle 3.3
* Create a virtual Android device. After launching it update the emulator's config regarding rendering capabilitites ([see this link](https://stackoverflow.com/questions/50595704)). Otherwise the emulator just shows a black screen.
* Restart the emulator.
* `./gradlew installDebug` or `installRelease` to install app (**note**: `installRelease` is only available if the APK can be signed an all that jazz).
* `./gradlew initSettings` to initialize app settings automatically using the `local.properties`.
* `./gradlew connectedAndroidTest` or `./gradlew cAT` to run instrumented tests on device.

**Important**: `connectedAndroidTest` doesn't seem to work using MIUI 12 with MIUI optimizations turned on for some reason.
It worked prior MIUI 10 or so, so use an emulator for instrumented tests.

**Note**: for some reason `.\gradlew installDebug` reports success, however install fails silently, `pm list packages` doesn't contain the package.
If this happens run `./gradlew uninstallAll` then install again.

**Note**: running instrumented tests will fail if the app was already instrumented with `SniffInstrumentation`, not sure why...
Android tries to run tests with this instrumentation class instead of `AndroidJUnitRunner` in this case.
If the `<instrumentation>` element is removed from the manifest then this isn't a problem, but `./gradlew initSettings` cannot be run anymore.
To run tests remove the `<instrumentation>` element from the manifest, run the tests, then revert the manifest.
