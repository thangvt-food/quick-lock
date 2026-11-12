#!/usr/bin/env bash
#
# Audits the built APK: package identity, SDK levels, declared permissions,
# accessibility service wiring and signature.
#
# Any permission at all fails the build. QuickLock must declare none.
set -euo pipefail

APK="${1:-app/build/outputs/apk/debug/app-debug.apk}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"

fail() { echo "FAIL: $*" >&2; exit 1; }
ok() { echo "OK:   $*"; }

[ -f "$APK" ] || fail "APK not found: $APK"
[ -n "$SDK" ] || fail "ANDROID_HOME / ANDROID_SDK_ROOT is not set"

find_tool() {
  local name="$1"
  # shellcheck disable=SC2012
  ls -1 "$SDK"/build-tools/*/"$name" 2>/dev/null | sort -V | tail -1
}

AAPT2="$(find_tool aapt2)"
APKSIGNER="$(find_tool apksigner)"
[ -x "$AAPT2" ] || fail "aapt2 not found under $SDK/build-tools"
[ -x "$APKSIGNER" ] || fail "apksigner not found under $SDK/build-tools"

echo "== APK =="
echo "path: $APK"
echo "size: $(du -h "$APK" | cut -f1)"

BADGING="$("$AAPT2" dump badging "$APK")"

PKG="$(printf '%s\n' "$BADGING" | sed -n "s/^package: name='\([^']*\)'.*/\1/p")"
VERSION_NAME="$(printf '%s\n' "$BADGING" | sed -n "s/.*versionName='\([^']*\)'.*/\1/p" | head -1)"
MIN_SDK="$(printf '%s\n' "$BADGING" | sed -n "s/^sdkVersion:'\([^']*\)'.*/\1/p")"
TARGET_SDK="$(printf '%s\n' "$BADGING" | sed -n "s/^targetSdkVersion:'\([^']*\)'.*/\1/p")"

echo "package: $PKG ($VERSION_NAME)"
echo "minSdk: $MIN_SDK  targetSdk: $TARGET_SDK"

[ "$PKG" = "com.quicklock.app" ] || fail "unexpected package name: $PKG"
[ "$MIN_SDK" = "26" ] || fail "unexpected minSdk: $MIN_SDK"
[ "$TARGET_SDK" = "35" ] || fail "unexpected targetSdk: $TARGET_SDK"
ok "package identity and SDK levels"

echo
echo "== Declared permissions =="
PERMS="$(printf '%s\n' "$BADGING" | sed -n "s/^uses-permission: name='\([^']*\)'.*/\1/p")"
if [ -n "$PERMS" ]; then
  printf '%s\n' "$PERMS"
  fail "QuickLock must declare no permissions (found the list above)"
fi
ok "no <uses-permission> entries at all"

echo
echo "== Components =="
TREE="$("$AAPT2" dump xmltree --file AndroidManifest.xml "$APK")"

# shellcheck disable=SC2016
printf '%s\n' "$BADGING" | sed -n 's/^launchable-activity: name=.\([^ ]*\).*/activity: \1/p' || true

for needle in \
  "com.quicklock.app.MainActivity" \
  "com.quicklock.app.LockActivity" \
  "com.quicklock.app.LockShortcutAlias" \
  "com.quicklock.app.accessibility.LockAccessibilityService" \
  "android.permission.BIND_ACCESSIBILITY_SERVICE" \
  "android.accessibilityservice.AccessibilityService" ; do
  printf '%s' "$TREE" | grep -qF "$needle" || fail "manifest is missing: $needle"
done
ok "activities, alias and accessibility service are wired"

# The accessibility service must not request event types or window content.
if printf '%s' "$TREE" | grep -qF "accessibilityEventTypes"; then
  fail "accessibility service declares accessibilityEventTypes"
fi
ok "no accessibility event types declared"

echo
echo "== Signature =="
"$APKSIGNER" verify --print-certs "$APK" | sed -n '1,4p'
ok "APK signature verifies"

echo
echo "AUDIT PASSED"
