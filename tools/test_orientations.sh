#!/usr/bin/env bash
set -euo pipefail
device="${1:?Pass phone or tablet}"
mkdir -p "validation/$device"
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell settings put system accelerometer_rotation 0
for rotation in 0 1; do
  if [ "$rotation" = 0 ]; then orientation=portrait; else orientation=landscape; fi
  adb shell settings put system user_rotation "$rotation"
  adb shell am start -W -n com.example.practical2/.MainActivity
  sleep 3
  result="validation/$device/$orientation-tests.txt"
  adb shell am instrument -w -r -e class com.example.practical2.ResponsiveLayoutTest com.example.practical2.test/androidx.test.runner.AndroidJUnitRunner | tee "$result"
  grep -q 'OK (6 tests)' "$result"
  adb shell am start -W -n com.example.practical2/.MainActivity
  sleep 2
  adb exec-out screencap -p > "validation/$device/$orientation.png"
  adb shell dumpsys activity activities > "validation/$device/$orientation-activity.txt"
done
