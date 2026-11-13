import SwiftData
import SwiftUI

/// 구매예정 — mart tabs + that mart's list (Android HomeScreen).
struct HomeView: View {
    @Environment(\.modelContext) private var ctx
    @EnvironmentObject private var router: Router
    @Query(sort: [SortDescriptor(\Mart.order), SortDescriptor(\Mart.createdAt)]) private var marts: [Mart]
    @Query(filter: #Predicate<GroceryItem> { $0.isCompleted == false },
           sort: [SortDescriptor(\GroceryItem.order), SortDescriptor(\GroceryItem.createdAt)])
    private var active: [GroceryItem]

    @State private var selectedUid: String?
    @State private var completing: Set<String> = []
    @State private var toast: Toast?

    @State private var showAdd = false
    @State private var showAddMart = false
    @State private var showManage = false
    @State private var showShare = false
    @State private var showVoice = false
    @State private var editItem: GroceryItem?
    @State private var reminderItem: GroceryItem?

    private var selected: Mart? { marts.first { $0.uid == selectedUid } ?? marts.first }
    private var items: [GroceryItem] { active.filter { $0.martUid == selected?.uid } }
    private var counts: [String: Int] { Dictionary(grouping: active, by: \.martUid).mapValues(\.count) }
    private var shop: StoreSearch.Shop? { selected.flatMap { StoreSearch.shop(for: $0.name) } }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("마트노트")
                .font(.system(size: 20, weight: .bold))
                .foregroundStyle(Palette.text)
                .padding(.horizontal, 20).padding(.top, 16).padding(.bottom, 12)

            MartTabs(marts: marts, selectedUid: selected?.uid, counts: counts,
                     onSelect: { selectedUid = $0 },
                     onAdd: { showAddMart = true },
                     onManage: { showManage = true })

            Divider().overlay(Palette.divider)

            if let selected, let shop {
                HStack {
                    Text("\(shop.label) 앱 바로가기").font(.system(size: 13)).foregroundStyle(Palette.textSecondary)
                    Spacer()
                    Toggle("", isOn: Binding(
                        get: { selected.shopAppEnabled },
                        set: { selected.shopAppEnabled = $0; ctx.commit() }
                    ))
                    .labelsHidden()
                }
                .padding(.horizontal, 20).padding(.vertical, 6)
            }

            if items.isEmpty {
                EmptyList { showAdd = true }
            } else {
                List {
                    ForEach(items) { item in
                        ItemRowView(
                            item: item,
                            martColor: selected?.color ?? Palette.brand,
                            highlighted: item.uid == router.highlightItemUid,
                            completing: completing.contains(item.uid),
                            searchLabel: (selected?.shopAppEnabled ?? false) ? shop.map { $0.searchesItem ? "\($0.label)에서 찾기" : "\($0.label) 앱 열기" } : nil,
                            onComplete: { complete(item) },
                            onEdit: { editItem = item },
                            onReminder: { reminderItem = item },
                            onSearch: { if let shop { StoreSearch.open(shop, item: item.name) } }
                        )
                        .listRowInsets(EdgeInsets(top: 0, leading: 20, bottom: 0, trailing: 12))
                        .listRowSeparator(.hidden)
                        .listRowBackground(Palette.bg)
                    }
                    .onMove(perform: move)
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(Palette.bg)
        .overlay(alignment: .bottomTrailing) { actionButtons }
        .overlay(alignment: .bottom) { ToastView(toast: $toast).padding(.bottom, 84) }
        .sheet(isPresented: $showAdd) {
            if let selected { AddItemSheet(mart: selected, marts: marts) }
        }
        .sheet(isPresented: $showAddMart) {
            MartEditSheet(mart: nil) { selectedUid = $0 }
        }
        .sheet(isPresented: $showManage) { MartManageView() }
        .sheet(isPresented: $showShare) {
            if let selected { ShareComposeSheet(mart: selected, items: items) }
        }
        .sheet(isPresented: $showVoice) {
            if let selected { VoiceAddSheet(mart: selected) }
        }
        .sheet(item: $editItem) { item in
            ItemEditSheet(item: item, marts: marts,
                          onReminder: { editItem = nil; DispatchQueue.main.asyncAfter(deadline: .now() + 0.4) { reminderItem = item } })
        }
        .sheet(item: $reminderItem) { item in
            ReminderSheet(item: item, martName: marts.first { $0.uid == item.martUid }?.name ?? "")
        }
        .sheet(item: $router.received) { received in
            ReceivedListSheet(received: received, marts: marts, currentUid: selected?.uid) { msg, martUid in
                selectedUid = martUid
                toast = Toast(text: msg)
            }
        }
        .onChange(of: router.selectedMartUid) { _, uid in
            if let uid { selectedUid = uid; router.selectedMartUid = nil }
        }
        .onChange(of: router.openAddFor) { _, uid in
            if let uid { selectedUid = uid; router.openAddFor = nil; showAdd = true }
        }
        .onChange(of: router.highlightItemUid) { _, uid in
            guard uid != nil else { return }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { router.highlightItemUid = nil }
        }
    }

    private var actionButtons: some View {
        HStack(spacing: 10) {
            if !items.isEmpty {
                Button { showShare = true } label: {
                    Image(systemName: "square.and.arrow.up")
                        .font(.system(size: 16, weight: .semibold))
                        .foregroundStyle(Palette.brand)
                        .frame(width: 44, height: 44)
                        .background(Circle().fill(Palette.bg))
                        .overlay(Circle().stroke(Palette.brand, lineWidth: 1.5))
                }
                .accessibilityLabel("장보기 리스트 공유")
            }
            Button { showVoice = true } label: {
                Label("음성추가", systemImage: "mic.fill")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(Palette.brand)
                    .padding(.horizontal, 16).frame(height: 48)
                    .background(Capsule().fill(Palette.bg))
                    .overlay(Capsule().stroke(Palette.brand, lineWidth: 1.5))
            }
            Button { showAdd = true } label: {
                Label("추가", systemImage: "plus")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundStyle(.white)
                    .padding(.horizontal, 20).frame(height: 48)
                    .background(Capsule().fill(Palette.brand))
                    .shadow(color: .black.opacity(0.12), radius: 6, y: 3)
            }
        }
        .disabled(selected == nil)
        .padding(.trailing, 16).padding(.bottom, 16)
    }

    /// 완료: strikethrough sweeps for 1s (row shows it), then the item leaves + [되돌리기].
    private func complete(_ item: GroceryItem) {
        guard !completing.contains(item.uid) else { return }
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        completing.insert(item.uid)
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
            completing.remove(item.uid)
            ctx.complete(item)
            ctx.commit()
            toast = Toast(text: "'\(item.name)' 완료", actionLabel: "되돌리기") {
                ctx.restore(item)
                ctx.commit()
            }
        }
    }

    private func move(from: IndexSet, to: Int) {
        var list = items
        list.move(fromOffsets: from, toOffset: to)
        for (i, it) in list.enumerated() { it.order = i }
        ctx.commit()
    }
}

// MARK: - Mart tabs

struct MartTabs: View {
    let marts: [Mart]
    let selectedUid: String?
    let counts: [String: Int]
    let onSelect: (String) -> Void
    let onAdd: () -> Void
    let onManage: () -> Void

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(marts) { m in
                        let on = m.uid == selectedUid
                        Button { onSelect(m.uid) } label: {
                            HStack(spacing: 6) {
                                Text(m.emoji)
                                Text(m.name).font(.system(size: 15, weight: .bold))
                                Text("\(counts[m.uid] ?? 0)")
                                    .font(.system(size: 12, weight: .semibold))
                                    .padding(.horizontal, 7).padding(.vertical, 1)
                                    .background(Capsule().fill(on ? Color.white.opacity(0.25) : Palette.bgTertiary))
                                    .foregroundStyle(on ? .white : Palette.textTertiary)
                            }
                            .foregroundStyle(on ? .white : Palette.text)
                            .padding(.horizontal, 14).frame(height: 40)
                            .background(Capsule().fill(on ? m.color : Palette.bg))
                            .overlay(Capsule().stroke(on ? Color.clear : Palette.divider, lineWidth: 1))
                        }
                        .id(m.uid)
                    }
                    Button(action: onAdd) {
                        Label("추가", systemImage: "plus")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundStyle(Palette.textTertiary)
                            .padding(.horizontal, 14).frame(height: 40)
                            .overlay(Capsule().stroke(Palette.textDisabled, style: StrokeStyle(lineWidth: 1.2, dash: [4, 3])))
                    }
                    Button(action: onManage) {
                        Image(systemName: "ellipsis")
                            .foregroundStyle(Palette.textSecondary)
                            .frame(width: 40, height: 40)
                            .overlay(Circle().stroke(Palette.divider, lineWidth: 1))
                    }
                    .accessibilityLabel("마트 관리")
                }
                .padding(.horizontal, 16).padding(.bottom, 12)
            }
            .onChange(of: selectedUid) { _, uid in
                if let uid { withAnimation { proxy.scrollTo(uid, anchor: .center) } }
            }
        }
    }
}

// MARK: - Item row

struct ItemRowView: View {
    let item: GroceryItem
    let martColor: Color
    let highlighted: Bool
    let completing: Bool
    let searchLabel: String?
    let onComplete: () -> Void
    let onEdit: () -> Void
    let onReminder: () -> Void
    let onSearch: () -> Void

    @State private var strike: CGFloat = 0

    var body: some View {
        HStack(spacing: 10) {
            VStack(alignment: .leading, spacing: 2) {
                Text(item.name)
                    .font(.system(size: 16))
                    .foregroundStyle(Palette.text)
                    .overlay(alignment: .leading) {
                        GeometryReader { g in
                            Rectangle().fill(Palette.text.opacity(0.7))
                                .frame(width: g.size.width * strike, height: 2)
                                .frame(maxHeight: .infinity, alignment: .center)
                        }
                    }
                if let at = item.reminderAt, !completing {
                    Button(action: onReminder) {
                        Label(Reminders.short(at), systemImage: "bell")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(martColor)
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
            .onTapGesture { if !completing { onEdit() } }

            if let searchLabel {
                Button(action: onSearch) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(martColor)
                        .frame(width: 32, height: 32)
                        .background(Circle().fill(martColor.opacity(0.12)))
                }
                .buttonStyle(.plain)
                .accessibilityLabel(searchLabel)
            }

            ZStack {
                if completing {
                    Circle().fill(Palette.success).frame(width: 28, height: 28)
                        .overlay(Image(systemName: "checkmark").font(.system(size: 13, weight: .bold)).foregroundStyle(.white))
                        .transition(.scale(scale: 0.6).combined(with: .opacity))
                } else {
                    Button(action: onComplete) {
                        Text("완료")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(Color(hex: "#6B6B6B"))
                            .frame(width: 56, height: 32)
                            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Palette.divider, lineWidth: 1.5))
                    }
                    .buttonStyle(.plain)
                }
            }
            .frame(width: 56, height: 32)

            Image(systemName: "line.3.horizontal")
                .font(.system(size: 16))
                .foregroundStyle(Palette.textDisabled)
                .frame(width: 24, height: 32)
                .accessibilityLabel("순서 바꾸기")
        }
        .frame(minHeight: 56)
        .background(highlighted ? martColor.opacity(0.15) : Color.clear)
        .onChange(of: completing) { _, now in
            if now {
                withAnimation(.easeInOut(duration: 1.0)) { strike = 1 }
            } else {
                strike = 0
            }
        }
        .animation(.easeOut(duration: 0.25), value: completing)
    }
}

struct EmptyList: View {
    let onAdd: () -> Void
    var body: some View {
        VStack(spacing: 8) {
            Spacer()
            Image(systemName: "bag").font(.system(size: 44)).foregroundStyle(Palette.textDisabled)
            Text("아직 살 게 없네요").font(.system(size: 17, weight: .semibold)).foregroundStyle(Palette.textSecondary)
            Text("오른쪽 아래 + 버튼을 눌러 추가하세요").font(.system(size: 14)).foregroundStyle(Palette.textTertiary)
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .contentShape(Rectangle())
        .onTapGesture(perform: onAdd)
    }
}

// MARK: - Toast (snackbar)

struct Toast: Equatable {
    let id = UUID()
    let text: String
    var actionLabel: String? = nil
    var action: (() -> Void)? = nil
    static func == (a: Toast, b: Toast) -> Bool { a.id == b.id }
}

struct ToastView: View {
    @Binding var toast: Toast?

    var body: some View {
        if let t = toast {
            HStack {
                Text(t.text).font(.system(size: 14)).foregroundStyle(.white).lineLimit(2)
                Spacer()
                if let label = t.actionLabel {
                    Button(label) { t.action?(); toast = nil }
                        .font(.system(size: 14, weight: .semibold))
                        .foregroundStyle(Color(hex: "#7FB2FF"))
                }
            }
            .padding(.horizontal, 16).padding(.vertical, 14)
            .background(RoundedRectangle(cornerRadius: 12).fill(Color(hex: "#191F28")))
            .padding(.horizontal, 16)
            .transition(.move(edge: .bottom).combined(with: .opacity))
            .task(id: t.id) {
                try? await Task.sleep(for: .seconds(3))
                if toast?.id == t.id { withAnimation { toast = nil } }
            }
        }
    }
}
