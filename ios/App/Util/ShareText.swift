import Foundation

/// Sharing a list to a chat and receiving one (Android 1.5.0 parity).
enum ShareText {
    static let listPage = "https://mkkim0422.github.io/mart/l/"

    /// Rotating 부탁 placeholders (same 15 as Android).
    static let placeholders = [
        "이거 사다 줄래? 🥺", "오는 길에 이것 좀 부탁해~", "퇴근길에 이거 좀 부탁할게 🙏",
        "마트 가는 김에 이것도 부탁해 🛒", "혹시 이거 사다 줄 수 있어?", "사다 주면 정말 고마울 텐데 🥹",
        "지나가는 길에 이것만 부탁 🙏", "이것 좀 부탁해도 될까? 💕", "이거 떨어졌어… 부탁해 🥺",
        "사다 줄 사람~ 손 들어요~ 🙋", "오늘 이거 좀 사와 줄래?", "장 보는 김에 이거도 부탁 ㅎㅎ",
        "이거 좀 사다 줘! 너밖에 없어 🥲", "퇴근 전에 이것만 부탁해 🙇", "오늘 저녁용 사다 줄래? 🍚",
    ]

    /// `#` + base64url(UTF-8 "마트\n색\n이모지\n항목…") — the list page decodes it.
    static func listLink(mart: Mart, items: [GroceryItem]) -> String {
        var lines = [mart.name, mart.colorHex.replacingOccurrences(of: "#", with: ""), mart.emoji]
        lines += items.prefix(30).map { $0.name.replacingOccurrences(of: "\n", with: " ") }
        let b64 = Data(lines.joined(separator: "\n").utf8).base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
        return listPage + "#" + b64
    }

    /// is.gd → tinyurl → the long link (never blocks sharing).
    static func shorten(_ long: String) async -> String {
        let q = long.addingPercentEncoding(withAllowedCharacters: .alphanumerics) ?? long
        for api in ["https://is.gd/create.php?format=simple&url=", "https://tinyurl.com/api-create.php?url="] {
            guard let url = URL(string: api + q) else { continue }
            var req = URLRequest(url: url)
            req.timeoutInterval = 2.5
            if let (data, resp) = try? await URLSession.shared.data(for: req),
               (resp as? HTTPURLResponse)?.statusCode == 200,
               let s = String(data: data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines),
               s.hasPrefix("https://"), s.count < 60 {
                return s
            }
        }
        return long
    }

    static func message(mart: Mart, items: [GroceryItem], note: String, link: String) -> String {
        var s = ""
        let n = note.trimmingCharacters(in: .whitespacesAndNewlines).replacingOccurrences(of: "\n", with: " ")
        if !n.isEmpty { s += n + "\n\n" }
        s += "🛒 [\(mart.name)] 장보기 리스트\n\n"
        items.forEach { s += "• " + $0.name.replacingOccurrences(of: "\n", with: " ") + "\n" }
        s += "\n📝 마트노트에서 보냄"
        s += "\n\n👉 눌러서 마트노트에 바로 담기\n"
        s += link
        return s
    }

    /// A received list: `martnote://add?s=마트&c=색&e=이모지&i=항목&i=…` (from the list page).
    struct Received: Identifiable {
        let id = UUID()
        let martName: String?
        let colorHex: String?
        let emoji: String?
        let items: [String]
    }

    static func parse(_ url: URL) -> Received? {
        guard url.scheme == "martnote", url.host == "add",
              let comps = URLComponents(url: url, resolvingAgainstBaseURL: false) else { return nil }
        let q = comps.queryItems ?? []
        var seen = Set<String>()
        let items = q.filter { $0.name == "i" }
            .compactMap { $0.value?.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty && $0.count <= 30 && seen.insert($0).inserted }
            .prefix(50)
        guard !items.isEmpty else { return nil }
        let c = q.first { $0.name == "c" }?.value
        return Received(
            martName: q.first { $0.name == "s" }?.value?.trimmingCharacters(in: .whitespaces).nilIfEmpty,
            colorHex: c.flatMap { $0.range(of: "^[0-9A-Fa-f]{6}$", options: .regularExpression) != nil ? "#\($0)" : nil },
            emoji: q.first { $0.name == "e" }?.value?.nilIfEmpty,
            items: Array(items)
        )
    }
}

extension String {
    var nilIfEmpty: String? { isEmpty ? nil : self }

    /// 을/를 for the last Hangul syllable ("쿠팡을", "다이소를").
    var objectParticle: String {
        guard let s = unicodeScalars.last, (0xAC00...0xD7A3).contains(s.value) else { return "를" }
        return (s.value - 0xAC00) % 28 != 0 ? "을" : "를"
    }
}
