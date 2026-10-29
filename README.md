# Life Hack or Urban Myth?

A **native Kotlin Android Studio** assignment project. XML layouts and a platform Android Activity; no web technologies, Expo, database, login, or API keys.

## Open and run

1. Extract this ZIP. In Android Studio choose **Open** and select the `LifeHackOrMyth` folder containing `settings.gradle.kts` (not only the `app` folder).
2. Use Android Studio **Meerkat (2024.3.1) or newer**, with a Gradle JDK of **17 or 21**. JDK 17 is the pinned CI default. Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK.
3. In SDK Manager install **Android SDK Platform 35**, **Build Tools 35.0.0**, and Platform Tools. Accept any SDK licence prompts.
4. Let Gradle sync finish. First sync requires internet to download dependencies. The included wrapper downloads Gradle 8.11.1 automatically; you do not need to install Gradle yourself.
5. Select the `app` run configuration and an Android device/emulator running **Android 6.0 / API 23 or later**. Click **Run**.

If asked for an SDK path, let Android Studio create `local.properties` for your machine. Do not copy another computer’s SDK path or commit this file.

Pinned versions: AGP 8.9.2, Kotlin 2.1.20, Gradle 8.11.1, compile/target SDK 35, minimum SDK 23. These are intentionally compatible versions, not a claim to be the latest.

## Assignment features

| Requirement from supplied screenshots | Implementation |
|---|---|
| Welcome message and app description | Welcome screen and instructions |
| Start button | Opens card 1 and resets previous progress |
| Specific statement + Hack (True)/Myth (False) | Ten cards in `QuestionBank.kt` |
| Feedback after each answer | Correct/incorrect message plus explanation and source |
| Next button and question iteration | Locked until answered; final button opens score |
| Score counter | `QuizEngine.score`, one score per question |
| Personalised score feedback | 80–100%: Master Hacker; 50–79%: Good instincts; below 50%: Stay Safe Online |
| Review statements and correct explanations | Review screen loops over all cards, also showing your choices |
| Version control and automated builds | GitHub instructions below and `.github/workflows/android.yml` |
| Testing (missing page; sensible defaults) | 17 JVM unit tests, 2 device tests, and manual checklist |

The quiz advances through the list one card at a time after Next. A `for` loop creates every review card; full-bank unit tests also demonstrate iteration over all questions. Navigation is deliberately event-driven so a loop does not block the Android UI thread.

## Important files

```text
app/src/main/java/com/example/lifehackormyth/
  MainActivity.kt       Four screens, answer feedback, navigation, saved state
  QuizEngine.kt         Scoring, question iteration, state, feedback (plain Kotlin)
  QuestionBank.kt       Statements, true/false values, explanations, source links
app/src/main/res/layout/
  activity_welcome.xml
  activity_quiz.xml
  activity_score.xml
  activity_review.xml
  item_review.xml
app/src/test/.../QuizEngineTest.kt          JVM unit tests
app/src/androidTest/.../QuizFlowTest.kt     On-device UI tests
.github/workflows/android.yml              Build/test/lint automation
```

To change cards, edit `QuestionBank.questions`. The score and question count adapt automatically. If you change the number of cards, also update the welcome instruction text and the tests that intentionally expect ten cards.

## How the code works

1. Start creates a fresh quiz; `currentIndex` begins at 0.
2. `answer(true)` means Hack; `answer(false)` means Myth. Only the first answer is stored, preventing repeated-tap score inflation.
3. Feedback is shown, answer buttons are disabled, and Next becomes enabled.
4. `next()` moves to the next card, or returns false at the final answered card so the score screen is displayed.
5. Review displays every card with its correct answer, explanation, source, and the user’s answer.
6. `onSaveInstanceState` saves the screen, index, and answers. Rotation/activity recreation restores these values and calculates the score from answers.

Progress is session-based: not permanently stored after deliberately closing/restarting the app. Android may restore it after system process recreation when saved instance state is available. Pressing Back during a quiz asks before clearing answers.

The quiz works offline. Tapping an official-source link opens a browser and requires internet. No dangerous tip is presented as something to attempt.

## Tests

From the project folder (macOS/Linux):

```sh
chmod +x gradlew
./gradlew testDebugUnitTest lintDebug
# With an emulator or USB device connected:
./gradlew connectedDebugAndroidTest
```

On Windows use `gradlew.bat testDebugUnitTest lintDebug` or the Android Studio terminal. In Android Studio you can also right-click a test class and select Run.

JVM tests cover correct/wrong answers, duplicate taps, no skipping, progression, full scores, restart, restore validation, score bands, and card data. Device tests cover a perfect quiz → score → review → restart and activity recreation. See `VERIFICATION.md` for what was actually run during preparation.

### Manual checklist
- Welcome shows description and Start.
- Next is disabled before an answer.
- Correct and wrong choices show appropriate feedback.
- Repeated taps cannot change an answer or inflate the score.
- All 10 cards appear; Next on the final card opens results.
- Try perfect, partial, and zero scores; verify feedback bands.
- Review shows every statement, correct answer, explanation, and user choice.
- Play again resets score and returns to card 1.
- Rotate before/after answering and on score/review; progress remains intact.
- Check on a small screen, large font size, and API 35 for scrolling and system-bar clearance.
- Back from review returns to score; Back in quiz asks before clearing progress.

## GitHub and GitHub Actions

No GitHub repository was created or pushed on your behalf. Create a new **empty** repository on GitHub, then run these locally inside the project folder (replace `YOUR_USERNAME`):

```sh
git init
git add .
git commit -m "Add native Kotlin flashcard quiz with tests and CI"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/LifeHackOrMyth.git
git push -u origin main
```

The included README becomes your repository README. Commit genuine progress as you make changes—do not invent previous development history:

```sh
git add .
git commit -m "Describe the change you actually made"
git push
```

Pushes to main/master and pull requests run unit tests, Android lint, and a debug APK build. Download the APK and reports from the run’s artifacts. In **Actions → Android checks → Run workflow**, optionally enable emulator UI tests. Emulator runs consume more GitHub Actions minutes; check your account allowance before enabling them. No workflow has been run on GitHub during preparation.

Do not commit signing keys or secrets. The workflow produces a debug APK only, not a Play Store release. Build/run Android binaries on your own machine or GitHub Actions.

## Troubleshooting
- **SDK location not found:** configure SDK Manager or your local `local.properties`.
- **JDK/Gradle error:** select JDK 17 or 21 in Gradle settings and retain the pinned wrapper/plugin versions.
- **Dependencies cannot download:** check network/proxy settings; sync again when connected.
- **Device is not listed:** start an emulator or enable USB debugging and accept the computer prompt.
- **Permission denied on gradlew:** run `chmod +x gradlew` or use `gradlew.bat` on Windows.

## Before submission

The screenshots did not include the complete testing section or any later rubric pages. Compare this project with the full assignment, personalise it, add any required student details/screenshots, and make sure you can explain the code. Check your institution’s rules for AI-assisted work.

Official card sources are linked in the app. Build compatibility reference: https://developer.android.com/build/releases/agp-8-9-0-release-notes
