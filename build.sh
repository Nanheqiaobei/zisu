#!/bin/bash
# 自塑 APK 手动构建脚本（不依赖 Gradle）
#
# 依赖：JDK 17、Android SDK 的 build-tools 35.0.0 与 platforms/android-35。
# 路径都可用环境变量覆盖，不设则用本机默认值：
#   APP_DIR      工程根目录，默认 /root/app
#   ANDROID_SDK  Android SDK 根目录，默认 /root/android-sdk
#   BUILD_TOOLS  build-tools 版本目录名，默认 35.0.0
#   PLATFORM     platform 目录名，默认 android-35
#   AAPT2        aapt2 可执行文件，默认 /root/bin/aapt2
#   ZIPALIGN     zipalign 可执行文件，默认 /root/bin/zipalign
#   JAVA         运行 d8 / apksigner 的 java 命令，默认 java
#   VERSION_CODE 版本号（整数），默认 48
#   VERSION_NAME 版本名，默认 0.48
#   KEYSTORE     签名用 keystore，默认 $APP_DIR/debug.keystore
#   KS_PASS      仓库口令，默认 android
#   KEY_ALIAS    别名，默认 androiddebugkey
#   KEY_PASS     别名口令，默认 android
#
# 注：本机 aapt2 / zipalign 是套在 qemu-user 外的包装脚本（官方只有 x86_64 版）。
#     在 x86_64 PC 上直接用 SDK 里 build-tools 下的同名可执行文件即可。

APP="${APP_DIR:-/root/app}"
SDK="${ANDROID_SDK:-/root/android-sdk}"
BT_NAME="${BUILD_TOOLS:-35.0.0}"
PLATFORM="${PLATFORM:-android-35}"
BT=$SDK/build-tools/$BT_NAME
JAR=$SDK/platforms/$PLATFORM/android.jar
AAPT2="${AAPT2:-/root/bin/aapt2}"
ZIPALIGN="${ZIPALIGN:-/root/bin/zipalign}"
JAVA="${JAVA:-java}"
VERSION_CODE="${VERSION_CODE:-50}"
VERSION_NAME="${VERSION_NAME:-0.50}"
OUT=$APP/out

# 签名密钥：默认优先用 release 密钥（放在仓库之外，不进版本库），找不到才退回 debug
RELEASE_KS="${RELEASE_KS:-/root/keystore/zisu-release.jks}"
if [ -z "$KEYSTORE" ]; then
  if [ -f "$RELEASE_KS" ]; then
    KEYSTORE="$RELEASE_KS"
    KS_PASS="${KS_PASS:-$(cat /root/keystore/password.txt 2>/dev/null)}"
    KEY_ALIAS="${KEY_ALIAS:-zisu}"
    KEY_PASS="${KEY_PASS:-$KS_PASS}"
    echo "签名：使用 release 密钥 $KEYSTORE"
  else
    KEYSTORE="$APP/debug.keystore"
    KS_PASS="${KS_PASS:-android}"
    KEY_ALIAS="${KEY_ALIAS:-androiddebugkey}"
    KEY_PASS="${KEY_PASS:-android}"
    echo "签名：未找到 release 密钥，退回 debug"
  fi
fi

rm -rf $OUT
mkdir -p $OUT/res $OUT/gen $OUT/classes $OUT/dex
echo "[1/6] aapt2 compile"
$AAPT2 compile --dir $APP/src/res -o $OUT/res.zip || { echo FAIL1; exit 11; }
echo "[2/6] aapt2 link"
$AAPT2 link -o $OUT/base.apk -I $JAR \
  --manifest $APP/src/AndroidManifest.xml --java $OUT/gen \
  --min-sdk-version 24 --target-sdk-version 35 \
  --version-code $VERSION_CODE --version-name $VERSION_NAME \
  $OUT/res.zip || { echo FAIL2; exit 12; }
echo "[3/6] javac"
find $APP/src/java $OUT/gen -name "*.java" > $OUT/sources.txt
javac --release 8 -nowarn -encoding UTF-8 -J-Dfile.encoding=UTF-8 -cp $JAR -d $OUT/classes @$OUT/sources.txt || { echo FAIL3; exit 13; }
echo "[4/6] d8"
find $OUT/classes -name "*.class" > $OUT/classes.txt
$JAVA -cp $BT/lib/d8.jar com.android.tools.r8.D8 --release --min-api 24 \
  --lib $JAR --output $OUT/dex @$OUT/classes.txt || { echo FAIL4; exit 14; }
echo "[5/6] package + align"
cp $OUT/base.apk $OUT/app-unsigned.apk
( cd $OUT/dex && zip -q $OUT/app-unsigned.apk classes.dex ) || { echo FAIL5; exit 15; }
( cd $OUT && $ZIPALIGN -f -p 4 app-unsigned.apk app-aligned.apk ) || { echo FAIL6; exit 16; }
echo "[6/6] sign"
$JAVA -jar $BT/lib/apksigner.jar sign \
  --ks $KEYSTORE --ks-pass pass:$KS_PASS --key-pass pass:$KEY_PASS \
  --ks-key-alias $KEY_ALIAS --v1-signing-enabled true --v2-signing-enabled true \
  --out $APP/自塑.apk $OUT/app-aligned.apk || { echo FAIL7; exit 17; }
echo "BUILD OK"
ls -la $APP/*.apk