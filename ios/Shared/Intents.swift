import AppIntents
import SwiftData
import WidgetKit

/// Widget ○ → check the item without opening the app (iOS 17 interactive widgets).
/// Same behaviour as Android's `CheckItemAction`: completes it, clears its reminder,
/// and every widget redraws without that row. Undo = 완료 탭.
struct CompleteItemIntent: AppIntent {
    static var title: LocalizedStringResource = "항목 완료"
    static var isDiscoverable: Bool = false

    @Parameter(title: "항목")
    var itemUid: String

    init() {}
    init(itemUid: String) { self.itemUid = itemUid }

    @MainActor
    func perform() async throws -> some IntentResult {
        let ctx = DataStore.shared.mainContext
        if let item = ctx.item(uid: itemUid), !item.isCompleted {
            ctx.complete(item)
            ctx.commit()
        }
        return .result()
    }
}

/// A mart choice for the 목록 위젯 (long-press → 위젯 편집 → 보여줄 마트). "ALL" = 전체.
struct MartEntity: AppEntity {
    static var typeDisplayRepresentation: TypeDisplayRepresentation = "마트"
    static var defaultQuery = MartQuery()

    var id: String
    var name: String
    var emoji: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: "\(emoji) \(name)")
    }

    static let all = MartEntity(id: "ALL", name: "전체 마트", emoji: "🗂")
}

struct MartQuery: EntityQuery {
    func entities(for identifiers: [String]) async throws -> [MartEntity] {
        try await suggestedEntities().filter { identifiers.contains($0.id) }
    }

    func suggestedEntities() async throws -> [MartEntity] {
        await MainActor.run {
            [MartEntity.all] + DataStore.shared.mainContext.marts().map {
                MartEntity(id: $0.uid, name: $0.name, emoji: $0.emoji)
            }
        }
    }

    func defaultResult() async -> MartEntity? { .all }
}

struct SelectMartIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "보여줄 마트"
    static var description = IntentDescription("목록 위젯에 보여줄 마트를 골라요.")

    @Parameter(title: "보여줄 마트")
    var mart: MartEntity?

    init() {}
}

/// Deep links shared by the widget and the app.
enum DeepLink {
    static func store(_ uid: String) -> URL { URL(string: "martnote://store/\(uid)")! }
    static func addItem(_ uid: String) -> URL { URL(string: "martnote://add-item/\(uid)")! }
    static let home = URL(string: "martnote://open")!
}
