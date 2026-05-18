# MartNote cold-start measurement
#
# Goal: budget phones -> Cold Start TotalTime <= 1000ms
#
# NOTE: ASCII-only on purpose. Windows PowerShell 5.1 misreads UTF-8(no BOM)
#       .ps1 files that contain Korean text -> parser error. Keep this file ASCII.
#
# Usage:
#   1) Install debug APK:  ./gradlew installDebug  (or adb install -r app-debug.apk)
#   2) adb devices  (confirm a device/emulator is connected)
#   3) powershell -ExecutionPolicy Bypass -File scripts/measure_coldstart.ps1
#      e.g.  ... -File scripts/measure_coldstart.ps1 -Serial RXXXXXX -Iter 5 -StoreId 1
#
# SPEC CORRECTION: this app has NO 'martnote://' URI deep-link scheme.
#   Manifest intent-filters = MAIN/LAUNCHER + SEND(text/plain) only. Widget
#   deep links arrive via Glance actionStartActivity = MainActivity component
#   + Intent extras (selected_store_id / action=OPEN_HOME / action=ADD_ITEM
#   + store_id). Scenarios 3-5 simulate those extras, NOT a URI.

param(
  [string]$Serial = "",
  [int]$Iter = 5,
  [long]$StoreId = 1
)

$ErrorActionPreference = "Stop"
$pkg = "com.rldjrgo.grocerynote"
$act = "$pkg/.MainActivity"

# Resolve adb to an ABSOLUTE exe path. A bare "adb" + a function named Adb
# collide (PS function names are case-insensitive and win over external apps),
# which previously caused infinite recursion. Always use the full path.
$gcmd = Get-Command adb -ErrorAction SilentlyContinue
if ($gcmd -and $gcmd.Source) {
  $adbExe = $gcmd.Source
} else {
  $cand = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
  if (Test-Path $cand) { $adbExe = $cand } else { throw "adb not found on PATH or $cand" }
}
if ($Serial -ne "") { $adbArgs = @("-s", $Serial) } else { $adbArgs = @() }
function RunAdb { & $adbExe @adbArgs @args }

function Measure-Scenario {
  param([string]$Name, [string[]]$StartArgs, [bool]$ColdStop = $true)
  $totals = @()
  for ($i = 1; $i -le $Iter; $i++) {
    if ($ColdStop) {
      RunAdb shell am force-stop $pkg | Out-Null
      Start-Sleep -Milliseconds 800
    }
    $out = (RunAdb shell am start -W @StartArgs) 2>&1 | Out-String
    $total = ([regex]::Match($out, "TotalTime:\s*(\d+)")).Groups[1].Value
    $this  = ([regex]::Match($out, "ThisTime:\s*(\d+)")).Groups[1].Value
    $wait  = ([regex]::Match($out, "WaitTime:\s*(\d+)")).Groups[1].Value
    if ($total) {
      $totals += [int]$total
      Write-Host ("  [{0}] {1,-22} ThisTime={2,6} TotalTime={3,6} WaitTime={4,6}" -f $i, $Name, $this, $total, $wait)
    } else {
      Write-Host "  [$i] $Name -> parse failed. raw:"
      Write-Host $out
    }
    RunAdb shell am force-stop $pkg | Out-Null
  }
  if ($totals.Count -gt 0) {
    $avg = [int][math]::Round(($totals | Measure-Object -Average).Average)
    $mx  = ($totals | Measure-Object -Maximum).Maximum
    $mn  = ($totals | Measure-Object -Minimum).Minimum
    if ($avg -le 1000) { $ok = "OK" } else { $ok = "FAIL" }
    [pscustomobject]@{ Scenario = $Name; AvgMs = $avg; MaxMs = $mx; MinMs = $mn; "Goal_1000" = $ok }
  }
}

$model = (RunAdb shell getprop ro.product.model)
$rel   = (RunAdb shell getprop ro.build.version.release)
$sdk   = (RunAdb shell getprop ro.build.version.sdk)
Write-Host "=== Device: $model / Android $rel (API $sdk) / iter=$Iter ===" -ForegroundColor Cyan

$results = @()
$results += Measure-Scenario "1.Cold(launcher)"  @("-n", $act)
$results += Measure-Scenario "2.Warm"            @("-n", $act) $false
$results += Measure-Scenario "3.Cold+widget-tap" @("-n", $act, "--es", "action", "OPEN_HOME")
$results += Measure-Scenario "4.Cold+widget-mart" @("-n", $act, "--el", "selected_store_id", "$StoreId")
$results += Measure-Scenario "5.Cold+widget-add"  @("-n", $act, "--es", "action", "ADD_ITEM", "--el", "store_id", "$StoreId")

Write-Host ""
Write-Host "=== Summary: $model ===" -ForegroundColor Cyan
$results | Format-Table -AutoSize | Out-String | Write-Host

Write-Host "=== Cross-check: am_activity_launch_time (last 5) ===" -ForegroundColor Cyan
$ev = (RunAdb logcat -d -b events) 2>&1 | Select-String "am_activity_launch_time" | Select-Object -Last 5
$ev | ForEach-Object { Write-Host $_.Line }
