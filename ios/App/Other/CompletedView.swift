import SwiftData
import SwiftUI

/// 완료 — bought items grouped 오늘 / 어제 / 이번 주 / 이전, filter by mart, swipe to 되돌리기 / 삭제.
struct CompletedView: View {
    @Environment(\.modelContext) private var ctx
    @Query(sort: [SortDescriptor<Mart>(\.order)]) private var marts: [Mart]
    @Query(filter: CompletedView.donePredicate,
           sort: [SortDescriptor<GroceryItem>(\.completedAt, order: .reverse)])
    private var done: [GroceryItem]

    private static let donePredicate = #Predicate<GroceryItem> { item in item.isCompleted == true }
    @State private var filter: String?

    private var shown: [GroceryItem] { filter.map { f in done.filter { $0.martUid == f } } ?? done }

    struct DaySection: Identifiable {
        let id: String
        let items: [GroceryItem]
    }

    private var sections: [DaySection] {
        let cal = Calendar.current
        let now = Date.now
        var groups: [String: [GroceryItem]] = [:]
        for it in shown {
            let d = it.completedAt ?? it.createdAt
            let key: String
            if cal.isDateInToday(d) { key = "오늘" }
            else if cal.isDateInYesterday(d) { key = "어제" }
            else if cal.isDate(d, equalTo: now, toGranularity: .weekOfYear) { key = "이번 주" }
            else { key = "이전" }
            groups[key, default: []].append(it)
        }
        return ["오늘", "어제", "이번 주", "이전"].compactMap { k in groups[k].map { DaySection(id: k, items: $0) } }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("완료").font(.system(size: 20, weight: .bold)).foregroundStyle(Palette.text)
                .padding(.horizontal, 20).padding(.top, 16).padding(.bottom, 12)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    chip("전체", on: filter == nil) { filter = nil }
                    ForEach(marts) { m in chip("\(m.emoji) \(m.name)", on: filter == m.uid, color: m.color) { filter = m.uid } }
                }
                .padding(.horizontal, 16).padding(.bottom, 8)
            }
            if shown.isEmpty {
                Spacer()
                Text("아직 완료한 항목이 없어요").foregroundStyle(Palette.textTertiary).frame(maxWidth: .infinity)
                Spacer()
            } else {
                List {
                    ForEach(sections) { section in
                        Section(section.id) {
                            ForEach(section.items) { it in
                                HStack {
                                    Image(systemName: "checkmark").foregroundStyle(Palette.textDisabled)
                                    Text(it.name).strikethrough().foregroundStyle(Palette.textTertiary)
                                    Spacer()
                                    if let m = marts.first(where: { $0.uid == it.martUid }) {
                                        Text("\(m.emoji) \(m.name)").font(.system(size: 11))
                                            .foregroundStyle(m.color)
                                            .padding(.horizontal, 8).padding(.vertical, 2)
                                            .background(Capsule().fill(m.color.opacity(0.12)))
                                    }
                                }
                                .swipeActions(edge: .trailing) {
                                    Button("삭제", role: .destructive) { ctx.deleteItem(it); ctx.commit() }
                                    Button("되돌리기") { ctx.restore(it); ctx.commit() }.tint(Palette.brand)
                                }
                            }
                        }
                    }
                }
                .listStyle(.plain)
            }
            HStack {
                Text("이번 주 \(count(.weekOfYear))개")
                Spacer()
                Text("이번 달 \(count(.month))개")
            }
            .font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
            .padding(.horizontal, 20).padding(.vertical, 8)
        }
        .background(Palette.bg)
    }

    private func count(_ unit: Calendar.Component) -> Int {
        done.filter { Calendar.current.isDate($0.completedAt ?? .distantPast, equalTo: .now, toGranularity: unit) }.count
    }

    private func chip(_ t: String, on: Bool, color: Color = Palette.text, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(t).font(.system(size: 13, weight: .semibold))
                .foregroundStyle(on ? .white : Palette.textSecondary)
                .padding(.horizontal, 14).frame(height: 34)
                .background(Capsule().fill(on ? color : Palette.bgTertiary))
        }
    }
}
