import Foundation
import SwiftData

/// A mart (쿠팡, 다이소 …). Same shape as Android's `Store`; `uid` is the stable id the
/// widget and deep links use (`martnote://store/<uid>`).
@Model
final class Mart {
    @Attribute(.unique) var uid: String
    var name: String
    var colorHex: String
    var emoji: String
    var order: Int
    /// "쇼핑몰 앱 바로가기" on for this mart (see `StoreSearch`). Default ON.
    var shopAppEnabled: Bool
    var createdAt: Date

    init(name: String, colorHex: String, emoji: String, order: Int) {
        self.uid = UUID().uuidString
        self.name = name
        self.colorHex = colorHex
        self.emoji = emoji
        self.order = order
        self.shopAppEnabled = true
        self.createdAt = .now
    }
}

/// One thing to buy — name only (no price/qty/memo, CLAUDE.md §3).
@Model
final class GroceryItem {
    @Attribute(.unique) var uid: String
    var martUid: String
    var name: String
    var isCompleted: Bool
    var completedAt: Date?
    var order: Int
    var createdAt: Date
    var reminderAt: Date?

    init(martUid: String, name: String, order: Int) {
        self.uid = UUID().uuidString
        self.martUid = martUid
        self.name = name
        self.isCompleted = false
        self.completedAt = nil
        self.order = order
        self.createdAt = .now
        self.reminderAt = nil
    }
}
