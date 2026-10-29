# Verification — 27 October 2026

## Passed

- Android app Kotlin sources and generated Java view-binding classes compiled.
- All **17 JVM unit tests passed**, with zero failures, errors, or skipped tests.
- Android lint completed with **zero errors** and six non-blocking warnings.
- Both on-device test methods compiled successfully.
- Application XML files parsed successfully.

Command used:

```sh
./gradlew testDebugUnitTest lintDebug compileDebugAndroidTestKotlin --no-daemon --max-workers=2
```

Environment: Linux, OpenJDK 21, Android SDK Platform 35, Build Tools 35.0.0, Gradle 8.11.1.

## Not performed

- App installation, emulator/device execution, visual inspection, and on-device UI test execution.
- APK assembly or release signing. This deliverable is source code, not an APK.
- GitHub repository creation, pushes, or workflow execution.
- Comparison against unseen assignment rubric pages.

Run the app and `connectedDebugAndroidTest` on your own device/emulator before submission. Compile verification does not establish that device tests pass.

## Remaining warnings

- Two newer XML attributes are ignored on older Android versions: predictive-back opt-out (API 33) and accessibility heading (API 28). Basic navigation and text remain available.
- Three AndroidX testing dependency update suggestions; versions are deliberately pinned to a compatible set.
- Backup configuration suggestion for Android 12+: the project disables cloud backup and does not persist quiz data to files. Device-transfer-specific rules are not defined.

The legacy `onBackPressed()` callback also produces a compiler deprecation warning. Predictive-back handling is opted out in the manifest; this is a deliberate simplification for the assignment project.

Machine-readable JVM test results are included in `verification/unit-test-results.xml`.