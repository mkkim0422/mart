import Foundation
import UserNotifications

/// Per-item one-shot reminder (Android Phase 11): local notification "마트 · 항목" /
/// "이거 살 시간이예요! 🛒"; tapping it opens that mart with the item highlighted.
enum Reminders {
    static func requestPermission() async -> Bool {
        let center = UNUserNotificationCenter.current()
        let settings = await center.notificationSettings()
        if settings.authorizationStatus == .authorized { return true }
        return (try? await center.requestAuthorization(options: [.alert, .sound, .badge])) ?? false
    }

    static func schedule(itemUid: String, martUid: String, martName: String, itemName: String, at date: Date) {
        let content = UNMutableNotificationContent()
        content.title = "\(martName) · \(itemName)"
        content.body = "이거 살 시간이예요! 🛒"
        content.sound = .default
        content.userInfo = ["martUid": martUid, "itemUid": itemUid]
        let comps = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute], from: date)
        let trigger = UNCalendarNotificationTrigger(dateMatching: comps, repeats: false)
        let req = UNNotificationRequest(identifier: "reminder-\(itemUid)", content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(req)
    }

    static func cancel(itemUid: String) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["reminder-\(itemUid)"])
    }

    /// "오늘 오후 3:00" / "내일 오전 9:00" / "11/20(목) 오후 2:00"
    static func short(_ date: Date, now: Date = .now) -> String {
        let cal = Calendar.current
        let time = date.formatted(Date.FormatStyle(locale: Locale(identifier: "ko_KR")).hour().minute())
        if cal.isDate(date, inSameDayAs: now) { return "오늘 \(time)" }
        if let tomorrow = cal.date(byAdding: .day, value: 1, to: now), cal.isDate(date, inSameDayAs: tomorrow) {
            return "내일 \(time)"
        }
        let f = DateFormatter()
        f.locale = Locale(identifier: "ko_KR")
        f.dateFormat = "M/d(E)"
        return "\(f.string(from: date)) \(time)"
    }
}
