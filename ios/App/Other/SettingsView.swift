import SwiftData
import SwiftUI

struct SettingsView: View {
    @Environment(\.modelContext) private var ctx
    @AppStorage("darkMode") private var darkMode = 0
    @State private var showWidgetGuide = false
    @State private var showManage = false
    @State private var confirmWipe = false

    private var version: String {
        Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? ""
    }

    var body: some View {
        NavigationStack {
            List {
                Section("위젯") {
                    Button { showWidgetGuide = true } label: { row("square.grid.2x2", "위젯 추가하기") }
                }
                Section("일반") {
                    Button { showManage = true } label: { row("cart", "마트 관리") }
                    Picker(selection: $darkMode) {
                        Text("시스템 설정").tag(0)
                        Text("라이트").tag(1)
                        Text("다크").tag(2)
                    } label: { row("moon", "다크 모드") }
                }
                Section("지원") {
                    Link(destination: URL(string: "mailto:mkkim850422@gmail.com?subject=%EB%A7%88%ED%8A%B8%EB%85%B8%ED%8A%B8%20%EB%AC%B8%EC%9D%98")!) {
                        row("envelope", "문의·피드백 보내기")
                    }
                    Link(destination: URL(string: "https://github.com/mkkim0422/mart/blob/main/docs/privacy_policy_ko.md")!) {
                        row("lock", "개인정보처리방침")
                    }
                    HStack { row("info.circle", "버전"); Spacer(); Text(version).foregroundStyle(Palette.textTertiary) }
                }
                Section {
                    Button(role: .destructive) { confirmWipe = true } label: { Text("모든 데이터 지우기") }
                }
            }
            .navigationTitle("설정")
            .sheet(isPresented: $showWidgetGuide) { WidgetGuideView() }
            .sheet(isPresented: $showManage) { MartManageView() }
            .confirmationDialog("모든 마트와 항목을 지울까요?", isPresented: $confirmWipe, titleVisibility: .visible) {
                Button("모두 지우기", role: .destructive, action: wipe)
            }
        }
    }

    private func row(_ icon: String, _ title: String) -> some View {
        Label { Text(title).foregroundStyle(Palette.text) } icon: { Image(systemName: icon).foregroundStyle(Palette.textSecondary) }
    }

    private func wipe() {
        ctx.marts().forEach { ctx.deleteMart($0) }
        ctx.insert(Mart(name: "쿠팡", colorHex: "#3182F6", emoji: "🚀", order: 0))
        ctx.insert(Mart(name: "다이소", colorHex: "#F04452", emoji: "🏪", order: 1))
        ctx.commit()
    }
}

/// iOS has no "add this widget" API — show the steps.
struct WidgetGuideView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Text("홈 화면에 위젯 추가하기").font(.system(size: 20, weight: .bold))
            step(1, "홈 화면의 빈 곳을 길게 눌러요", "앱들이 흔들리기 시작해요")
            step(2, "왼쪽 위 ‘편집’ → ‘위젯 추가’", "또는 ＋ 버튼")
            step(3, "‘마트노트’를 찾아 눌러요", "목록 위젯 · 개수 위젯 중 골라요")
            step(4, "‘위젯 추가’ → 원하는 자리에 놓기", "목록 위젯을 길게 누르고 ‘위젯 편집’에서 보여줄 마트를 바꿀 수 있어요")
            Text("목록 위젯의 ○를 누르면 앱을 안 열어도 바로 체크돼요.")
                .font(.system(size: 14)).foregroundStyle(Palette.brand)
                .padding(12).frame(maxWidth: .infinity, alignment: .leading)
                .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brandSoft))
            Spacer()
            Button("확인") { dismiss() }
                .font(.system(size: 16, weight: .semibold))
                .frame(maxWidth: .infinity).frame(height: 52)
                .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                .foregroundStyle(.white)
        }
        .padding(24)
    }

    private func step(_ n: Int, _ title: String, _ sub: String) -> some View {
        HStack(alignment: .top, spacing: 12) {
            Text("\(n)").font(.system(size: 14, weight: .bold)).foregroundStyle(.white)
                .frame(width: 26, height: 26).background(Circle().fill(Palette.brand))
            VStack(alignment: .leading, spacing: 2) {
                Text(title).font(.system(size: 16, weight: .semibold)).foregroundStyle(Palette.text)
                Text(sub).font(.system(size: 13)).foregroundStyle(Palette.textTertiary)
            }
        }
    }
}

/// 마트 관리 — drag to reorder, edit, delete, add.
struct MartManageView: View {
    @Environment(\.modelContext) private var ctx
    @Environment(\.dismiss) private var dismiss
    @Query(sort: [SortDescriptor(\Mart.order), SortDescriptor(\Mart.createdAt)]) private var marts: [Mart]
    @State private var editing: Mart?
    @State private var adding = false
    @State private var deleting: Mart?

    var body: some View {
        NavigationStack {
            List {
                ForEach(marts) { m in
                    HStack(spacing: 12) {
                        Text(m.emoji).font(.system(size: 20)).frame(width: 36, height: 36)
                            .background(RoundedRectangle(cornerRadius: 10).fill(m.color.opacity(0.12)))
                        VStack(alignment: .leading) {
                            Text(m.name).font(.system(size: 16, weight: .semibold))
                            Text("\(ctx.activeItems(martUid: m.uid).count)개 항목").font(.system(size: 12)).foregroundStyle(Palette.textTertiary)
                        }
                        Spacer()
                        Menu {
                            Button("수정") { editing = m }
                            Button("삭제", role: .destructive) { deleting = m }
                        } label: {
                            Image(systemName: "ellipsis").foregroundStyle(Palette.textTertiary).frame(width: 36, height: 36)
                        }
                    }
                }
                .onMove { from, to in
                    var list = marts
                    list.move(fromOffsets: from, toOffset: to)
                    for (i, m) in list.enumerated() { m.order = i }
                    ctx.commit()
                }
                Button { adding = true } label: { Label("마트 추가하기", systemImage: "plus") }
            }
            .environment(\.editMode, .constant(.active))
            .navigationTitle("마트 관리")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("완료") { dismiss() } } }
            .sheet(item: $editing) { MartEditSheet(mart: $0) }
            .sheet(isPresented: $adding) { MartEditSheet(mart: nil) }
            .confirmationDialog(deleting.map { "'\($0.name)' 마트와 담긴 항목이 모두 삭제돼요" } ?? "",
                                isPresented: Binding(get: { deleting != nil }, set: { if !$0 { deleting = nil } }),
                                titleVisibility: .visible) {
                Button("삭제", role: .destructive) {
                    if let d = deleting { ctx.deleteMart(d); ctx.commit() }
                    deleting = nil
                }
            }
        }
    }
}
