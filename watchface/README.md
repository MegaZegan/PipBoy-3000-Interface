# VaultWatch Face for Galaxy Watch 4

This folder contains the Samsung Galaxy Watch 4 / Wear OS version of the Pip-Boy project.

It includes two parts:

- A Watch Face Format face, shown in the Wear OS face picker as `VaultWatch Face`.
- A native Android activity, shown as the interactive Pip-Boy terminal app.

The terminal app is designed for Galaxy Watch 4 performance: Canvas/text UI, no WebView runtime, no heavy 3D, no constant GPS scanning, and no background sensor polling. Extra Pip-OS pages include STAT, INV, DATA, MAP, RAD, COM, SYS, SEC, SURV, and WX.

## Build

Open this `watchface/` folder in Android Studio, let Gradle sync, then build the `watchface` module.

If Android Studio creates or installs a Gradle wrapper, you can also build from this folder:

```powershell
.\gradlew.bat :watchface:assembleDebug
```

## Install

Pair the Galaxy Watch 4 with ADB wireless debugging, then install:

```powershell
adb install -r .\watchface\build\outputs\apk\debug\watchface-debug.apk
```

On the watch, long-press the current face, scroll to `VaultWatch Face`, and select it.

The interactive Pip-Boy terminal is available from the watch app launcher after installation.

## Files

- `watchface/src/main/res/raw/watchface.xml` defines the live face.
- `watchface/src/main/res/drawable-nodpi/pipboy_face_bg.png` is the static Pip-Boy face background.
- `watchface/src/main/res/drawable-nodpi/preview.png` is shown in the Wear OS face picker.
- `watchface/src/main/java/com/megazegan/vaultwatch/face/TerminalActivity.java` draws the touchable STAT/INV/DATA/MAP/RAD terminal app.
- `watchface/src/main/assets/` stores the bundled Pip-Boy art, icons, map, CSS, and JavaScript assets.
