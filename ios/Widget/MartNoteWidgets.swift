import AppIntents
import SwiftData
import SwiftUI
import WidgetKit

// iOS port of the two Android widgets (CLAUDE.md §12):
//  - 개수 위젯: per-mart remaining counts (small/medium)
//  - 목록 위젯: one mart or 전체, ○ checks in place (iOS 17 Button(intent:)), "+" opens the app.
// iOS widgets can't scroll, so the list shows what fits and "+N개 더".

@main
struct MartNoteWidgetBundle: WidgetBundle {
    var body: some Widget {
        ListWidget()
        CountWidget()
    }
}

// MARK: - Snapshot data (plain values — widgets render off the main context)

struct WItem: Identifiable, Hashable {
    let id: String
    let name: String
}

struct WMart: Identifiable, Hashable {
    let id: String
    let name: String
    let emoji: String
    let colorHex: String
    let items: [WItem]
    var color: Color { Color(hex: colorHex) }
}

@MainActor
private func loadMarts(only martUid: String?) -> [WMart] {
    let ctx = DataStore.shared.mainContext
    let all = ctx.marts()
    let marts = martUid.flatMap { uid in all.filter { $0.uid == uid }.nilIfEmpty } ?? all
    return marts.map { m in
        WMart(id: m.uid, name: m.name, emoji: m.emoji, colorHex: m.colorHex,
              items: ctx.activeItems(martUid: m.uid).map { WItem(id: $0.uid, name: $0.name) })
    }
}

private extension Array {
    var nilIfEmpty: [Element]? { isEmpty ? nil : self }
}

struct MartsEntry: TimelineEntry {
    let date: Date
    let marts: [WMart]
    /// nil = 전체
    let selectedUid: String?
}

private let sampleMarts = [
    WMart(id: "a", name: "쿠팡", emoji: "🚀", colorHex: "#3182F6",
          items: ["생수 2L", "물티슈", "키친타월", "세탁세제"].map { WItem(id: $0, name: $0) }),
    WMart(id: "b", name: "다이소", emoji: "🏪", colorHex: "#F04452",
          items: ["건전지", "수세미", "지퍼백"].map { WItem(id: $0, name: $0) }),
]

// MARK: - 목록 위젯

struct ListProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> MartsEntry {
        MartsEntry(date: .now, marts: [sampleMarts[0]], selectedUid: "a")
    }

    func snapshot(for configuration: SelectMartIntent, in context: Context) async -> MartsEntry {
        if context.isPreview { return placeholder(in: context) }
        return await entry(for: configuration)
    }

    func timeline(for configuration: SelectMartIntent, in context: Context) async -> Timeline<MartsEntry> {
        // No polling (battery): the app/intent calls reloadAllTimelines() on every change.
        Timeline(entries: [await entry(for: configuration)], policy: .never)
    }

    private func entry(for configuration: SelectMartIntent) async -> MartsEntry {
        let uid = configuration.mart?.id == MartEntity.all.id ? nil : configuration.mart?.id
        let marts = await MainActor.run { loadMarts(only: uid) }
        let selected = uid.flatMap { u in marts.first { $0.id == u }?.id }
        return MartsEntry(date: .now, marts: marts, selectedUid: selected)
    }
}

struct ListWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: "ListWidget", intent: SelectMartIntent.self, provider: ListProvider()) { entry in
            ListWidgetView(entry: entry)
                .containerBackground(for: .widget) { Palette.bg }
        }
        .configurationDisplayName("목록 위젯")
        .description("살 것 목록, 동그라미 눌러 바로 체크")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct ListWidgetView: View {
    let entry: MartsEntry
    @Environment(\.widgetFamily) private var family

    private var maxRows: Int { family == .systemLarge ? 11 : 4 }

    var body: some View {
        if entry.marts.isEmpty {
            Link(destination: DeepLink.home) {
                Text("마트를 추가해주세요").font(.subheadline).foregroundStyle(Palette.textTertiary)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            }
        } else {
            ZStack(alignment: .bottomTrailing) {
                VStack(alignment: .leading, spacing: 0) {
                    ForEach(rows, id: \.self) { row in
                        switch row {
                        case .header(let m):
                            MartHeader(mart: m).padding(.top, row == rows.first ? 0 : 6)
                        case .item(let it):
                            ItemLine(item: it)
                        case .empty(let m):
                            Link(destination: DeepLink.addItem(m.id)) {
                                Text("아직 적은 게 없어요").font(.caption).foregroundStyle(Palette.textTertiary)
                                    .frame(maxWidth: .infinity, alignment: .leading).padding(.vertical, 6)
                            }
                        case .more(let n, let m):
                            Link(destination: DeepLink.store(m.id)) {
                                Text("+\(n)개 더").font(.caption).foregroundStyle(Palette.textTertiary)
                                    .padding(.leading, 30).padding(.vertical, 3)
                            }
                        }
                    }
                    Spacer(minLength: 0)
                }
                Link(destination: entry.selectedUid.map(DeepLink.addItem) ?? DeepLink.home) {
                    Image(systemName: "plus")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(.white)
                        .frame(width: 32, height: 32)
                        .background(Circle().fill(Palette.brand))
                }
            }
        }
    }

    enum Row: Hashable {
        case header(WMart), item(WItem), empty(WMart), more(Int, WMart)
    }

    /// Headers + items that fit; each header/item = one row, overflow → "+N개 더".
    private var rows: [Row] {
        var out: [Row] = []
        var budget = maxRows
        for m in entry.marts {
            guard budget > 1 else { break }
            out.append(.header(m)); budget -= 1
            if m.items.isEmpty { out.append(.empty(m)); budget -= 1; continue }
            let fit = min(m.items.count, max(budget - 1, 1))
            m.items.prefix(fit).forEach { out.append(.item($0)) }
            budget -= fit
            if m.items.count > fit { out.append(.more(m.items.count - fit, m)); budget -= 1 }
        }
        return out
    }
}

private struct MartHeader: View {
    let mart: WMart
    var body: some View {
        Link(destination: DeepLink.store(mart.id)) {
            HStack(spacing: 6) {
                Text(mart.emoji).font(.system(size: 12))
                    .frame(width: 20, height: 20)
                    .background(RoundedRectangle(cornerRadius: 6).fill(mart.color.opacity(0.12)))
                Text(mart.name).font(.system(size: 13, weight: .semibold)).foregroundStyle(Palette.textSecondary)
                Spacer()
                Text("\(mart.items.count)").font(.system(size: 11, weight: .bold)).foregroundStyle(mart.color)
                    .padding(.horizontal, 7).padding(.vertical, 1)
                    .background(Capsule().fill(mart.color.opacity(0.14)))
            }
            .padding(.bottom, 3)
        }
    }
}

private struct ItemLine: View {
    let item: WItem
    var body: some View {
        HStack(spacing: 8) {
            Button(intent: CompleteItemIntent(itemUid: item.id)) {
                Image(systemName: "circle")
                    .font(.system(size: 17))
                    .foregroundStyle(Palette.textDisabled)
                    .frame(width: 22, height: 22)
            }
            .buttonStyle(.plain)
            Text(item.name).font(.system(size: 14)).foregroundStyle(Palette.text).lineLimit(1)
            Spacer(minLength: 0)
        }
        .padding(.vertical, 2)
    }
}

// MARK: - 개수 위젯

struct CountProvider: TimelineProvider {
    func placeholder(in context: Context) -> MartsEntry {
        MartsEntry(date: .now, marts: sampleMarts, selectedUid: nil)
    }

    func getSnapshot(in context: Context, completion: @escaping (MartsEntry) -> Void) {
        if context.isPreview { completion(placeholder(in: context)); return }
        Task { @MainActor in completion(MartsEntry(date: .now, marts: loadMarts(only: nil), selectedUid: nil)) }
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<MartsEntry>) -> Void) {
        Task { @MainActor in
            completion(Timeline(entries: [MartsEntry(date: .now, marts: loadMarts(only: nil), selectedUid: nil)], policy: .never))
        }
    }
}

struct CountWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "CountWidget", provider: CountProvider()) { entry in
            CountWidgetView(entry: entry)
                .containerBackground(for: .widget) { Palette.bg }
        }
        .configurationDisplayName("개수 위젯")
        .description("마트별 남은 개수")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct CountWidgetView: View {
    let entry: MartsEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        if entry.marts.isEmpty {
            Text("마트를 추가해주세요").font(.caption).foregroundStyle(Palette.textTertiary)
        } else {
            VStack(spacing: 8) {
                ForEach(entry.marts.prefix(family == .systemSmall ? 4 : 4)) { m in
                    Link(destination: DeepLink.store(m.id)) {
                        HStack(spacing: 8) {
                            Text(m.emoji).font(.system(size: 13))
                                .frame(width: 24, height: 24)
                                .background(RoundedRectangle(cornerRadius: 7).fill(m.color.opacity(0.12)))
                            Text(m.name).font(.system(size: 14, weight: .semibold)).foregroundStyle(Palette.textSecondary)
                                .lineLimit(1)
                            Spacer(minLength: 4)
                            Text(m.items.isEmpty ? "—" : "\(m.items.count)")
                                .font(.system(size: 12, weight: .bold)).foregroundStyle(m.color)
                                .padding(.horizontal, 8).padding(.vertical, 2)
                                .background(Capsule().fill(m.color.opacity(0.14)))
                        }
                    }
                }
                Spacer(minLength: 0)
            }
        }
    }
}
