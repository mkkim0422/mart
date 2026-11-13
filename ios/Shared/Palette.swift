import SwiftUI
import UIKit

/// Toss-style colors (CLAUDE.md §10). Light/dark pairs resolve from the trait collection.
enum Palette {
    static let brand = Color(light: "#3182F6", dark: "#4592FF")
    static let brandSoft = Color(light: "#E8F3FF", dark: "#1A2D4D")
    static let success = Color(light: "#2BA471", dark: "#3FBE85")
    static let danger = Color(hex: "#F04452")
    static let bg = Color(light: "#FFFFFF", dark: "#17171B")
    static let bgSecondary = Color(light: "#F9FAFB", dark: "#1F1F23")
    static let bgTertiary = Color(light: "#F2F4F6", dark: "#2B2D31")
    static let divider = Color(light: "#E5E8EB", dark: "#3A3D42")
    static let text = Color(light: "#191F28", dark: "#F2F4F6")
    static let textSecondary = Color(light: "#4E5968", dark: "#A8B0BA")
    static let textTertiary = Color(light: "#8B95A1", dark: "#6B7684")
    static let textDisabled = Color(light: "#C9CDD2", dark: "#4E5968")

    /// Mart colors the user picks from (same 8 as Android).
    static let martColors: [String] = ["#3182F6", "#FFB800", "#F04452", "#2BA471", "#8B5CF6", "#F564A9", "#FF8A3D", "#6B7684"]
    static let martEmojis: [String] = ["🛒", "🚀", "🏪", "🥬", "🍞", "🥩", "🐟", "🍎", "🧴", "💊", "🛍", "📦"]
}

extension Color {
    init(hex: String) {
        var s = hex.trimmingCharacters(in: .whitespaces)
        if s.hasPrefix("#") { s.removeFirst() }
        var v: UInt64 = 0
        Scanner(string: s).scanHexInt64(&v)
        if s.count == 8 {
            self.init(.sRGB,
                      red: Double((v >> 16) & 0xFF) / 255, green: Double((v >> 8) & 0xFF) / 255,
                      blue: Double(v & 0xFF) / 255, opacity: Double((v >> 24) & 0xFF) / 255)
        } else {
            self.init(.sRGB,
                      red: Double((v >> 16) & 0xFF) / 255, green: Double((v >> 8) & 0xFF) / 255,
                      blue: Double(v & 0xFF) / 255, opacity: 1)
        }
    }

    init(light: String, dark: String) {
        self.init(UIColor { trait in
            UIColor(Color(hex: trait.userInterfaceStyle == .dark ? dark : light))
        })
    }
}

extension Mart {
    var color: Color { Color(hex: colorHex) }
}
