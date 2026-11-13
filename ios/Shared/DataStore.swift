import Foundation
import SwiftData
import WidgetKit

enum AppGroup {
    static let id = "group.com.rldjrgo.grocerynote"
    /// Shared with the widget (dark mode etc. live in the app's own defaults).
    static let defaults = UserDefaults(suiteName: id) ?? .standard
}

/// One SwiftData store in the App Group so the app, the widget and the widget's
/// check button (`CompleteItemIntent`) all see the same lists.
enum DataStore {
    /// Replaced by `refresh()` when the widget changed data from its own process — on
    /// iOS 17 SwiftData doesn't merge another process's saves into this one.
    private(set) static var shared: ModelContainer = make()

    /// A context that reads the store as it is now (widget/intent calls: the widget
    /// process stays alive between reloads and a long-lived context returns stale rows).
    static func freshContext() -> ModelContext { ModelContext(shared) }

    /// Set by the widget's ○ (other process); the app reloads its container when it comes back.
    static let externalChangeKey = "externalChange"

    static func refresh() { shared = make() }

    private static func make() -> ModelContainer {
        let schema = Schema([Mart.self, GroceryItem.self])
        let dir: URL
        if let group = FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: AppGroup.id) {
            dir = group
        } else {
            // Unsigned simulator builds have no App Group — fall back to the app's own folder.
            // The widget then reads a different store (shows nothing) — expected only there.
            print("⚠️ App Group \(AppGroup.id) unavailable — app and widget will NOT share data")
            dir = URL.applicationSupportDirectory
            try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        let config = ModelConfiguration(schema: schema, url: dir.appendingPathComponent("martnote.store"))
        do {
            return try ModelContainer(for: schema, configurations: config)
        } catch {
            fatalError("ModelContainer: \(error)")
        }
    }

    /// First launch: 쿠팡 / 다이소 (same seed as Android).
    @MainActor
    static func seedIfNeeded() {
        let ctx = shared.mainContext
        guard !AppGroup.defaults.bool(forKey: "seeded") else { return }
        if ((try? ctx.fetchCount(FetchDescriptor<Mart>())) ?? 0) == 0 {
            ctx.insert(Mart(name: "쿠팡", colorHex: "#3182F6", emoji: "🚀", order: 0))
            ctx.insert(Mart(name: "다이소", colorHex: "#F04452", emoji: "🏪", order: 1))
        }
        AppGroup.defaults.set(true, forKey: "seeded")
        ctx.commit()
    }
}

extension ModelContext {
    /// Save + redraw every widget. Call after every change.
    func commit() {
        try? save()
        WidgetCenter.shared.reloadAllTimelines()
    }

    func marts() -> [Mart] {
        (try? fetch(FetchDescriptor<Mart>(sortBy: [SortDescriptor(\.order), SortDescriptor(\.createdAt)]))) ?? []
    }

    func mart(uid: String) -> Mart? {
        var d = FetchDescriptor<Mart>(predicate: #Predicate { $0.uid == uid })
        d.fetchLimit = 1
        return (try? fetch(d))?.first
    }

    func item(uid: String) -> GroceryItem? {
        var d = FetchDescriptor<GroceryItem>(predicate: #Predicate { $0.uid == uid })
        d.fetchLimit = 1
        return (try? fetch(d))?.first
    }

    func activeItems(martUid: String) -> [GroceryItem] {
        let d = FetchDescriptor<GroceryItem>(
            predicate: #Predicate { $0.martUid == martUid && $0.isCompleted == false },
            sortBy: [SortDescriptor(\.order), SortDescriptor(\.createdAt)]
        )
        return (try? fetch(d)) ?? []
    }

    func allActiveItems() -> [GroceryItem] {
        let d = FetchDescriptor<GroceryItem>(
            predicate: #Predicate { $0.isCompleted == false },
            sortBy: [SortDescriptor(\.order), SortDescriptor(\.createdAt)]
        )
        return (try? fetch(d)) ?? []
    }

    @discardableResult
    func addItem(martUid: String, name: String) -> GroceryItem? {
        let clean = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !clean.isEmpty else { return nil }
        let next = (activeItems(martUid: martUid).map(\.order).max() ?? -1) + 1
        let item = GroceryItem(martUid: martUid, name: String(clean.prefix(100)), order: next)
        insert(item)
        return item
    }

    /// Add received/shared names, skipping ones already on that mart's list (same name,
    /// ignoring case) — sharing the same list twice shouldn't double it.
    func addItems(martUid: String, names: [String]) -> (added: Int, skipped: Int) {
        let clean = names.map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }.filter { !$0.isEmpty }
        var existing = Set(activeItems(martUid: martUid).map { $0.name.lowercased() })
        var added = 0
        for n in clean where !existing.contains(n.lowercased()) {
            addItem(martUid: martUid, name: n)
            existing.insert(n.lowercased())
            added += 1
        }
        return (added, clean.count - added)
    }

    /// Reminder times that already passed (fired while the app was closed) → clear the 🔔 chip.
    func clearPastReminders() {
        let now = Date.now
        let d = FetchDescriptor<GroceryItem>(predicate: #Predicate { $0.reminderAt != nil })
        var changed = false
        for it in (try? fetch(d)) ?? [] where (it.reminderAt ?? .distantFuture) <= now {
            it.reminderAt = nil
            changed = true
        }
        if changed { commit() }
    }

    func complete(_ item: GroceryItem) {
        item.isCompleted = true
        item.completedAt = .now
        if item.reminderAt != nil {
            item.reminderAt = nil
            Reminders.cancel(itemUid: item.uid)
        }
    }

    func restore(_ item: GroceryItem) {
        item.isCompleted = false
        item.completedAt = nil
        item.order = (activeItems(martUid: item.martUid).map(\.order).max() ?? -1) + 1
    }

    func deleteItem(_ item: GroceryItem) {
        Reminders.cancel(itemUid: item.uid)
        delete(item)
    }

    func deleteMart(_ mart: Mart) {
        let uid = mart.uid
        let items = (try? fetch(FetchDescriptor<GroceryItem>(predicate: #Predicate { $0.martUid == uid }))) ?? []
        items.forEach { deleteItem($0) }
        delete(mart)
    }

    @discardableResult
    func addMart(name: String, colorHex: String, emoji: String) -> Mart {
        let next = (marts().map(\.order).max() ?? -1) + 1
        let m = Mart(name: String(name.trimmingCharacters(in: .whitespaces).prefix(20)), colorHex: colorHex, emoji: emoji, order: next)
        insert(m)
        return m
    }

    /// Widget order: marts the user put first in 노출순서설정, then tab order. (Same list for now.)
    func activeCounts() -> [String: Int] {
        Dictionary(grouping: allActiveItems(), by: \.martUid).mapValues(\.count)
    }
}
