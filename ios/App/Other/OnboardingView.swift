import SwiftUI

/// 4 pages like Android (마트별 · 이름만 · 쇼핑 앱 · 위젯); last page: 위젯 추가 방법 / 바로 시작하기.
struct OnboardingView: View {
    let onDone: () -> Void
    @State private var page = 0
    @State private var showGuide = false

    private let pages: [(String, String, String)] = [
        ("cart", "마트별로 따로 정리하세요", "쿠팡, 다이소 따로따로, 어디 가서도 헷갈리지 않게"),
        ("pencil.line", "이름만 적으면 끝나요", "가격도 개수도 필요 없어요. 빠르고 심플하게!"),
        ("magnifyingglass", "적어둔 그대로 쇼핑 앱에서 검색", "항목 옆 🔍를 누르면 쿠팡·컬리 같은 쇼핑 앱에서 바로 찾아줘요"),
        ("checkmark.circle", "홈 화면에서 보고, 바로 체크", "위젯에서 동그라미만 누르면 앱을 안 열어도 체크돼요"),
    ]

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Spacer()
                Button("건너뛰기", action: onDone).foregroundStyle(Palette.textTertiary).padding(20)
            }
            TabView(selection: $page) {
                ForEach(pages.indices, id: \.self) { i in
                    VStack(spacing: 18) {
                        Spacer()
                        Image(systemName: pages[i].0)
                            .font(.system(size: 72, weight: .light))
                            .foregroundStyle(Palette.brand)
                            .frame(width: 180, height: 180)
                            .background(Circle().fill(Palette.brandSoft))
                        Spacer().frame(height: 20)
                        Text(pages[i].1).font(.system(size: 24, weight: .bold)).foregroundStyle(Palette.text)
                        Text(pages[i].2).font(.system(size: 15)).foregroundStyle(Palette.textSecondary)
                            .multilineTextAlignment(.center).padding(.horizontal, 32)
                        Spacer()
                    }
                    .tag(i)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .indexViewStyle(.page(backgroundDisplayMode: .always))

            if page == pages.count - 1 {
                HStack(spacing: 12) {
                    Button("바로 시작하기", action: onDone)
                        .foregroundStyle(Palette.textTertiary)
                        .padding(.horizontal, 12)
                    Button { showGuide = true } label: {
                        Text("위젯 추가하기").font(.system(size: 16, weight: .bold))
                            .frame(maxWidth: .infinity).frame(height: 52)
                            .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                            .foregroundStyle(.white)
                    }
                }
                .padding(20)
            } else {
                Button { withAnimation { page += 1 } } label: {
                    Text("다음").font(.system(size: 16, weight: .semibold))
                        .frame(maxWidth: .infinity).frame(height: 52)
                        .background(RoundedRectangle(cornerRadius: 12).fill(Palette.brand))
                        .foregroundStyle(.white)
                }
                .padding(20)
            }
        }
        .background(Palette.bg)
        .sheet(isPresented: $showGuide, onDismiss: onDone) { WidgetGuideView() }
    }
}
