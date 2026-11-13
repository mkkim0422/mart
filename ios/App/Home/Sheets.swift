import SwiftData
import SwiftUI

// MARK: - 추가 (name only, stays open for the next one)

struct AddItemSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let marts: [Mart]
    @State private var martUid: String
    @State private var text = ""
    @State private var added: [String] = []
    @FocusState private var focused: Bool

    init(mart: Mart, marts: [Mart]) {
        self.marts = marts
        _martUid = State(initialValue: mart.uid)
    }

    private var mart: Mart? { marts.first { $0.uid == martUid } }

    /// This mart's recent names (completed ones too) — tap to add again.
    private var recent: [String] {
        let uid = martUid
        var d = FetchDescriptor<GroceryItem>(predicate: #Predicate { $0.martUid == uid },
                                             sortBy: [SortDescriptor(\.createdAt, order: .reverse)])
        d.fetchLimit = 60
        let active = Set(ctx.activeItems(martUid: uid).map(\.name))
        var seen = Set<String>()
        return ((try? ctx.fetch(d)) ?? []).map(\.name)
            .filter { !active.contains($0) && seen.insert($0).inserted }
            .prefix(10).map { $0 }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Menu {
                    ForEach(marts) { m in
                        Button("\(m.emoji) \(m.name)") { martUid = m.uid }
                    }
                } label: {
                    HStack(spacing: 4) {
                        Text("\(mart?.emoji ?? "") \(mart?.name ?? "")").font(.system(size: 15, weight: .bold))
                        Image(systemName: "chevron.down").font(.system(size: 11, weight: .bold))
                    }
                    .foregroundStyle(.white)
                    .padding(.horizontal, 12).frame(height: 34)
                    .background(Capsule().fill(mart?.color ?? Palette.brand))
                }
                Text("에 추가해요").font(.system(size: 15)).foregroundStyle(Palette.textSecondary)
                Spacer()
                Button { dismiss() } label: { Image(systemName: "xmark").foregroundStyle(Palette.textTertiary) }
            }

            HStack {
                Image(systemName: "plus").foregroundStyle(Palette.textTertiary)
                TextField("무엇을 살까요?", text: $text)
                    .focused($focused)
                    .submitLabel(.done)
                    .onSubmit(add)
            }
            .padding(.horizontal, 16).frame(height: 52)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(focused ? Palette.brand : Palette.divider, lineWidth: focused ? 1.5 : 1))

            if !recent.isEmpty {
                Text("최근 등록 상품 · 누르면 바로 추가").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(recent, id: \.self) { n in
                            Button { addName(n) } label: {
                                Text(n).font(.system(size: 13)).foregroundStyle(Palette.textSecondary)
                                    .padding(.horizontal, 12).frame(height: 32)
                                    .background(Capsule().fill(Palette.bgTertiary))
                            }
                        }
                    }
                }
            }
            if !added.isEmpty {
                Text("담았어요: " + added.joined(separator: ", "))
                    .font(.system(size: 13)).foregroundStyle(Palette.success).lineLimit(2)
            }
            Button(added.isEmpty && text.isEmpty ? "닫기" : "완료") {
                add()
                dismiss()
            }
            .font(.system(size: 16, weight: .semibold))
            .frame(maxWidth: .infinity).frame(height: 52)
            .background(RoundedRectangle(cornerRadius: 12).fill(Palette.bgTertiary))
            .foregroundStyle(Palette.textSecondary)
            Spacer(minLength: 0)
        }
        .padding(20)
        .presentationDetents([.medium])
        .onAppear { focused = true }
    }

    private func add() {
        let n = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !n.isEmpty else { return }
        addName(n)
        text = ""
    }

    private func addName(_ n: String) {
        ctx.addItem(martUid: martUid, name: n)
        ctx.commit()
        added.append(n)
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
}

// MARK: - 마트 추가 / 수정

struct MartEditSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let mart: Mart?
    var onCreated: (String) -> Void = { _ in }
    @State private var name = ""
    @State private var colorHex = Palette.martColors[0]
    @State private var emoji = Palette.martEmojis[0]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(mart == nil ? "마트 추가" : "마트 수정").font(.system(size: 17, weight: .bold))
            TextField("마트 이름 (예: 이마트)", text: $name)
                .padding(.horizontal, 16).frame(height: 52)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.brand, lineWidth: 1.5))
            Text("색").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
            HStack(spacing: 10) {
                ForEach(Palette.martColors, id: \.self) { hex in
                    Circle().fill(Color(hex: hex)).frame(width: 30, height: 30)
                        .overlay(Circle().stroke(Palette.text, lineWidth: colorHex == hex ? 2.5 : 0).padding(-3))
                        .onTapGesture { colorHex = hex }
                }
            }
            Text("아이콘").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 6), spacing: 10) {
                ForEach(Palette.martEmojis, id: \.self) { e in
                    Text(e).font(.system(size: 24)).frame(width: 44, height: 44)
                        .background(RoundedRectangle(cornerRadius: 10).fill(emoji == e ? Color(hex: colorHex).opacity(0.18) : Palette.bgTertiary))
                        .onTapGesture { emoji = e }
                }
            }
            Button(mart == nil ? "추가" : "저장") {
                let n = name.trimmingCharacters(in: .whitespaces)
                guard !n.isEmpty else { return }
                if let mart {
                    mart.name = n; mart.colorHex = colorHex; mart.emoji = emoji
                } else {
                    onCreated(ctx.addMart(name: n, colorHex: colorHex, emoji: emoji).uid)
                }
                ctx.commit()
                dismiss()
            }
            .font(.system(size: 16, weight: .semibold))
            .frame(maxWidth: .infinity).frame(height: 52)
            .background(RoundedRectangle(cornerRadius: 12).fill(name.trimmingCharacters(in: .whitespaces).isEmpty ? Palette.bgTertiary : Palette.brand))
            .foregroundStyle(.white)
            Spacer(minLength: 0)
        }
        .padding(20)
        .presentationDetents([.large])
        .onAppear {
            if let mart { name = mart.name; colorHex = mart.colorHex; emoji = mart.emoji }
        }
    }
}

// MARK: - 항목 편집 (이름 · 마트 · 알림 · 삭제)

struct ItemEditSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let item: GroceryItem
    let marts: [Mart]
    let onReminder: () -> Void
    @State private var name = ""
    @State private var martUid = ""
    @State private var confirmDelete = false

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("항목 편집").font(.system(size: 17, weight: .bold))
            TextField("이름", text: $name)
                .padding(.horizontal, 16).frame(height: 52)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.brand, lineWidth: 1.5))
                .submitLabel(.done).onSubmit(save)
            if marts.count > 1 {
                Text("마트").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(marts) { m in
                            let on = m.uid == martUid
                            Text("\(m.emoji) \(m.name)").font(.system(size: 13))
                                .foregroundStyle(on ? .white : Palette.textSecondary)
                                .padding(.horizontal, 14).frame(height: 36)
                                .background(Capsule().fill(on ? m.color : Palette.bgTertiary))
                                .onTapGesture { martUid = m.uid }
                        }
                    }
                }
            }
            Button(action: onReminder) {
                HStack {
                    Image(systemName: "bell")
                    Text("알림").foregroundStyle(Palette.text)
                    Spacer()
                    Text(item.reminderAt.map { Reminders.short($0) } ?? "설정 안 함").foregroundStyle(Palette.textTertiary)
                    Image(systemName: "chevron.right").foregroundStyle(Palette.textTertiary)
                }
                .font(.system(size: 15)).foregroundStyle(Palette.textSecondary)
                .frame(height: 52)
            }
            HStack(spacing: 8) {
                Button("삭제") { confirmDelete = true }
                    .frame(width: 96, height: 52)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Palette.danger.opacity(0.1)))
                    .foregroundStyle(Palette.danger)
                Button("저장", action: save)
                    .frame(maxWidth: .infinity).frame(height: 52)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                    .foregroundStyle(.white)
            }
            .font(.system(size: 16, weight: .semibold))
            Spacer(minLength: 0)
        }
        .padding(20)
        .presentationDetents([.medium])
        .onAppear { name = item.name; martUid = item.martUid }
        .confirmationDialog("'\(item.name)'\(item.name.objectParticle) 삭제할까요?", isPresented: $confirmDelete, titleVisibility: .visible) {
            Button("삭제", role: .destructive) {
                ctx.deleteItem(item)
                ctx.commit()
                dismiss()
            }
        }
    }

    private func save() {
        let n = name.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !n.isEmpty else { return }
        item.name = String(n.prefix(100))
        if martUid != item.martUid {
            item.martUid = martUid
            item.order = (ctx.activeItems(martUid: martUid).map(\.order).max() ?? -1) + 1
        }
        ctx.commit()
        dismiss()
    }
}

// MARK: - 알림

struct ReminderSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let item: GroceryItem
    let martName: String
    @State private var date = Calendar.current.date(byAdding: .hour, value: 1, to: .now) ?? .now
    @State private var error: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("'\(item.name)' 알림").font(.system(size: 17, weight: .bold))
            DatePicker("", selection: $date, in: Date.now..., displayedComponents: [.date, .hourAndMinute])
                .datePickerStyle(.wheel).labelsHidden()
                .environment(\.locale, Locale(identifier: "ko_KR"))
            if let error { Text(error).font(.system(size: 13)).foregroundStyle(Palette.danger) }
            HStack(spacing: 8) {
                if item.reminderAt != nil {
                    Button("알림 끄기") {
                        Reminders.cancel(itemUid: item.uid)
                        item.reminderAt = nil
                        ctx.commit()
                        dismiss()
                    }
                    .frame(width: 110, height: 52)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Palette.bgTertiary))
                    .foregroundStyle(Palette.textSecondary)
                }
                Button("알림 설정") {
                    Task {
                        guard date > .now else { error = "이미 지난 시간이에요. 다시 선택해주세요"; return }
                        guard await Reminders.requestPermission() else {
                            error = "설정 → 마트노트 → 알림을 켜 주세요"; return
                        }
                        Reminders.schedule(itemUid: item.uid, martUid: item.martUid, martName: martName,
                                           itemName: item.name, at: date)
                        item.reminderAt = date
                        ctx.commit()
                        dismiss()
                    }
                }
                .frame(maxWidth: .infinity).frame(height: 52)
                .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                .foregroundStyle(.white)
            }
            .font(.system(size: 16, weight: .semibold))
        }
        .padding(20)
        .presentationDetents([.medium])
        .onAppear { if let at = item.reminderAt, at > .now { date = at } }
    }
}

// MARK: - 공유 (부탁 문구 + 짧은 링크)

struct ShareComposeSheet: View {
    @Environment(\.dismiss) private var dismiss
    let mart: Mart
    let items: [GroceryItem]
    @State private var note = ""
    @State private var placeholder = ShareText.placeholders.randomElement() ?? ""
    @State private var sharing = false
    @State private var shareItem: ShareItem?

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("\(mart.emoji) \(mart.name) 장보기 리스트 보내기").font(.system(size: 17, weight: .bold))
            Text("부탁 한마디 (안 적으면 아래 문구로 보내요)").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
            TextField(placeholder, text: $note, axis: .vertical)
                .lineLimit(2...4)
                .padding(14)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Palette.divider))
                .onChange(of: note) { _, v in if v.count > 200 { note = String(v.prefix(200)) } }
            Text("\(items.count)개 · 받은 사람이 링크를 누르면 마트노트에 바로 담겨요")
                .font(.system(size: 13)).foregroundStyle(Palette.textSecondary)
            Button {
                sharing = true
                Task {
                    let link = await ShareText.shorten(ShareText.listLink(mart: mart, items: items))
                    shareItem = ShareItem(text: ShareText.message(mart: mart, items: items,
                                                                  note: note.isEmpty ? placeholder : note, link: link))
                    sharing = false
                }
            } label: {
                HStack {
                    if sharing { ProgressView().tint(.white) }
                    Text("공유하기")
                }
                    .font(.system(size: 16, weight: .semibold))
                    .frame(maxWidth: .infinity).frame(height: 52)
                    .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                    .foregroundStyle(.white)
            }
            .disabled(sharing)
            Spacer(minLength: 0)
        }
        .padding(20)
        .presentationDetents([.medium])
        .sheet(item: $shareItem, onDismiss: { dismiss() }) { s in
            ActivityView(text: s.text)
        }
    }
}

struct ShareItem: Identifiable {
    let id = UUID()
    let text: String
}

struct ActivityView: UIViewControllerRepresentable {
    let text: String
    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: [text], applicationActivities: nil)
    }
    func updateUIViewController(_ vc: UIActivityViewController, context: Context) {}
}

// MARK: - 음성추가

struct VoiceAddSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let mart: Mart
    @StateObject private var speech = SpeechAdder()
    @State private var added: [String] = []

    var body: some View {
        VStack(spacing: 16) {
            Text("\(mart.emoji) \(mart.name)에 말로 추가").font(.system(size: 17, weight: .bold))
            Text(speech.denied ? "설정에서 마이크·음성 인식을 허용해 주세요" :
                    speech.text.isEmpty ? (speech.listening ? "듣고 있어요… \"라면\"" : "마이크를 누르고 말하세요") : speech.text)
                .font(.system(size: 20, weight: .semibold))
                .foregroundStyle(speech.text.isEmpty ? Palette.textTertiary : Palette.text)
                .frame(maxWidth: .infinity, minHeight: 60)
            Button {
                if speech.listening {
                    speech.stop()
                    let n = SpeechAdder.clean(speech.text)
                    if !n.isEmpty {
                        ctx.addItem(martUid: mart.uid, name: n)
                        ctx.commit()
                        added.append(n)
                        speech.text = ""
                    }
                } else {
                    Task { await speech.start() }
                }
            } label: {
                Image(systemName: speech.listening ? "stop.fill" : "mic.fill")
                    .font(.system(size: 28)).foregroundStyle(.white)
                    .frame(width: 76, height: 76)
                    .background(Circle().fill(speech.listening ? Palette.danger : Palette.brand))
            }
            Text(speech.listening ? "다 말했으면 눌러서 담기" : "")
                .font(.system(size: 13)).foregroundStyle(Palette.textTertiary)
            if !added.isEmpty {
                Text("담았어요: " + added.joined(separator: ", ")).font(.system(size: 13)).foregroundStyle(Palette.success)
            }
            Button("닫기") { speech.stop(); dismiss() }
                .foregroundStyle(Palette.textSecondary)
        }
        .padding(24)
        .presentationDetents([.medium])
        .onDisappear { speech.stop() }
    }
}

// MARK: - 받은 목록 (링크) — 같은 이름 마트 자동 선택, 없으면 "새로 만들기"

struct ReceivedListSheet: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    let received: ShareText.Received
    let marts: [Mart]
    let currentUid: String?
    let onDone: (String, String) -> Void

    private static let newId = "__new__"
    @State private var pick = ""
    @State private var unchecked: Set<String> = []

    private var sameName: Mart? {
        guard let n = received.martName else { return nil }
        return marts.first { $0.name.trimmingCharacters(in: .whitespaces) == n }
    }
    private var offerNew: Bool { received.martName != nil && sameName == nil }
    private var newColor: Color { Color(hex: received.colorHex ?? "#6B7684") }
    private var creating: Bool { offerNew && pick == Self.newId }
    private var picked: [String] { received.items.filter { !unchecked.contains($0) } }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(received.martName.map { "받은 \($0) 장보기 \(received.items.count)개" } ?? "받은 장보기 리스트 \(received.items.count)개")
                .font(.system(size: 17, weight: .bold))
            Text("담을 마트를 고르고 한 번에 넣으세요").font(.system(size: 13)).foregroundStyle(Palette.textTertiary)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    if offerNew, let name = received.martName {
                        Text("＋ \(received.emoji ?? "🛒") \(name) 새로 만들기").font(.system(size: 13))
                            .foregroundStyle(creating ? .white : newColor)
                            .padding(.horizontal, 14).frame(height: 36)
                            .background(Capsule().fill(creating ? newColor : .clear))
                            .overlay(Capsule().stroke(newColor, style: StrokeStyle(lineWidth: 1.5, dash: creating ? [] : [4, 3])))
                            .onTapGesture { pick = Self.newId }
                    }
                    ForEach(marts) { m in
                        let on = pick == m.uid
                        Text("\(m.emoji) \(m.name)").font(.system(size: 13))
                            .foregroundStyle(on ? .white : Palette.textSecondary)
                            .padding(.horizontal, 14).frame(height: 36)
                            .background(Capsule().fill(on ? m.color : Palette.bgTertiary))
                            .onTapGesture { pick = m.uid }
                    }
                }
            }
            if creating, let name = received.martName {
                Text("\(name) 마트를 새로 만들고 여기에 담아요").font(.system(size: 12)).foregroundStyle(newColor)
                    .padding(.horizontal, 10).padding(.vertical, 6)
                    .background(RoundedRectangle(cornerRadius: 10).fill(newColor.opacity(0.1)))
            }
            ScrollView {
                VStack(spacing: 0) {
                    ForEach(received.items, id: \.self) { n in
                        let on = !unchecked.contains(n)
                        HStack(spacing: 12) {
                            Image(systemName: on ? "checkmark.square.fill" : "square")
                                .font(.system(size: 22)).foregroundStyle(on ? Palette.brand : Palette.textDisabled)
                            Text(n).foregroundStyle(on ? Palette.text : Palette.textTertiary)
                            Spacer()
                        }
                        .frame(height: 46)
                        .contentShape(Rectangle())
                        .onTapGesture { if on { unchecked.insert(n) } else { unchecked.remove(n) } }
                    }
                }
            }
            .frame(maxHeight: 300)
            Button(action: add) {
                Text(picked.isEmpty ? "담을 항목을 골라주세요" :
                        creating ? "\(received.martName ?? "") 만들고 \(picked.count)개 담기" : "\(picked.count)개 담기")
                    .font(.system(size: 16, weight: .semibold))
                    .frame(maxWidth: .infinity).frame(height: 52)
                    .background(RoundedRectangle(cornerRadius: 12).fill(picked.isEmpty ? Palette.bgTertiary : creating ? newColor : Palette.brand))
                    .foregroundStyle(picked.isEmpty ? Palette.textDisabled : .white)
            }
            .disabled(picked.isEmpty)
        }
        .padding(20)
        .presentationDetents([.large])
        .onAppear {
            pick = offerNew ? Self.newId : (sameName?.uid ?? currentUid ?? marts.first?.uid ?? "")
        }
    }

    private func add() {
        var martUid = pick
        var created: String?
        if creating, let name = received.martName {
            martUid = ctx.addMart(name: name, colorHex: received.colorHex ?? "#6B7684", emoji: received.emoji ?? "🛒").uid
            created = name
        }
        guard !martUid.isEmpty, martUid != Self.newId else { return }
        let r = ctx.addItems(martUid: martUid, names: picked)
        ctx.commit()
        let msg: String
        if let created {
            msg = "\(created)\(created.objectParticle) 만들고 \(r.added)개를 담았어요"
        } else if r.added == 0 {
            msg = "이미 다 담겨 있어요"
        } else if r.skipped > 0 {
            msg = "\(r.added)개를 담았어요 · \(r.skipped)개는 이미 있어요"
        } else {
            msg = "\(r.added)개를 담았어요"
        }
        onDone(msg, martUid)
        dismiss()
    }
}
