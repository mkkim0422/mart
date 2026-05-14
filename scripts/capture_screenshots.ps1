# 폰의 현재 화면을 5장 자동 캡처합니다.
# 사용법:
#   1. 폰 USB 연결, USB 디버깅 활성화
#   2. 앱을 원하는 화면으로 이동
#   3. 이 스크립트 실행 → screenshots\ 폴더에 PNG 저장
#
# 권장 캡처 화면:
#   1. Home (마트 탭 + 항목 리스트)
#   2. AddItemSheet (자주 사는 항목 칩 보이게)
#   3. AddStoreSheet (이모지 + 컬러 보이게)
#   4. Completed (완료 목록 + 필터)
#   5. Settings
#   6. 위젯 (홈 화면)
#
# Mockup 도구 추천:
#   - https://mockuphone.com (무료, 폰 베젤 합성)
#   - https://smartmockups.com (5장 무료)

$adb = "C:\Users\minkk\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$out = Join-Path $PSScriptRoot "..\screenshots"
New-Item -ItemType Directory -Path $out -Force | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$fname = Join-Path $out "screen_$timestamp.png"

& $adb shell screencap -p /sdcard/_grocery_screenshot.png
& $adb pull /sdcard/_grocery_screenshot.png $fname
& $adb shell rm /sdcard/_grocery_screenshot.png

if (Test-Path $fname) {
  Write-Host "Saved: $fname" -ForegroundColor Green
} else {
  Write-Host "FAILED — adb device connected?" -ForegroundColor Red
}
