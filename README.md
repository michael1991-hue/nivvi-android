# Nivvi for Android

Calm, dark heart-rate app for Android. Package name: `app.nivvi`.

## What this first version does

- Scans for and connects to any Bluetooth Low Energy device that advertises the standard Heart Rate Service (180D), including a Neebo band.
- Shows the live pulse. Oxygen is shown only when the band also sends the Pulse Oximeter service.
- High and low alerts set by the person using the phone (thresholds and dwell time).
- Local history kept on the phone.
- Family sharing so another phone can follow the reading (same Wi-Fi first).
- Foreground notification while monitoring. This keeps Bluetooth alive when the screen locks — the notification is the Android lock-screen heart.

This is **not a medical device**. It does not diagnose, treat or replace professional care.

No Fitbit login. No Google Health API. Only devices that share a live Bluetooth heart rate are supported (Fitbit Air, Charge 6 or Pixel Watch 2+ after “Share heart rate” is turned on).

## Look

Calm dark theme matching the iOS app: midnight blue backgrounds, mint accents, coral heart-rate number, lavender secondary. Rounded cards, generous spacing, light typography on the live reading. British English throughout.

## Building

Open the folder in Android Studio. Sync Gradle and run on a physical device with Bluetooth.

Permissions for Bluetooth, notifications and foreground service are requested at runtime as needed.

## Progress

First-version scaffolding is in place and aligned with the iOS source (`neebo-connect`):

- Foreground `HeartRateMonitorService` keeps Bluetooth alive and updates the lock-screen notification.
- Standard Heart Rate (180D/2A37) parsing with basic sensor-contact rejection.
- Optional Pulse Oximeter continuous (2A5F) oxygen when present.
- Alert evaluation: strict high/low, dwell time, gap reset.
- Local history snapshots (≥30 s) via Room, shown as a list.
- DataStore for alert settings; Alerts screen with toggles and sliders.
- `MonitorViewModel` binds the service, polls live state, and drives the screens.
- Runtime Bluetooth and notification permission request on launch.
- Live screen shows coral heart-rate number and optional SpO₂.
- Device screen starts monitoring (foreground service + scan).
- British English, midnight-blue / mint / coral / lavender theme, medical disclaimer.

Open in Android Studio, sync Gradle, grant permissions on a physical device, then start monitoring from the Device tab.
