import SwiftData
import SwiftUI
import UserNotifications

@main
struct MartNoteApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var delegate
    @StateObject private var router = Router.shared
    @AppStorage("darkMode") private var darkMode = 0  // 0 시스템, 1 라이트, 2 다크
    @AppStorage("onboarded") private var onboarded = false
    @Environment(\.scenePhase) private var scenePhase
    @State private var container = DataStore.shared

    init() {
        Task { @MainActor in DataStore.seedIfNeeded() }
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if onboarded {
                    MainTabs()
                } else {
                    OnboardingView { onboarded = true }
                }
            }
            .environmentObject(router)
            .preferredColorScheme(darkMode == 1 ? .light : darkMode == 2 ? .dark : nil)
            .tint(Palette.brand)
            .onOpenURL { router.handle($0) }
            .modelContainer(container)
            .onChange(of: scenePhase) { _, phase in
                guard phase == .active else { return }
                // ○ ticked on the widget (another process) → reload so @Query sees it.
                if AppGroup.defaults.bool(forKey: DataStore.externalChangeKey) {
                    AppGroup.defaults.set(false, forKey: DataStore.externalChangeKey)
                    DataStore.refresh()
                    container = DataStore.shared
                }
                container.mainContext.clearPastReminders()
            }
        }
    }
}

/// Deep links from widgets, notifications and received list links.
@MainActor
final class Router: ObservableObject {
    static let shared = Router()

    enum Tab: Hashable { case home, completed, settings }

    @Published var tab: Tab = .home
    @Published var selectedMartUid: String?
    @Published var highlightItemUid: String?
    @Published var openAddFor: String?
    @Published var received: ShareText.Received?

    func handle(_ url: URL) {
        guard url.scheme == "martnote" else { return }
        tab = .home
        switch url.host {
        case "add":
            received = ShareText.parse(url)
        case "store":
            selectedMartUid = url.lastPathComponent
        case "add-item":
            selectedMartUid = url.lastPathComponent
            openAddFor = url.lastPathComponent
        default:
            break
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification) async
        -> UNNotificationPresentationOptions { [.banner, .sound] }

    /// Reminder tap → that mart, item highlighted.
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let info = response.notification.request.content.userInfo
        let mart = info["martUid"] as? String
        let item = info["itemUid"] as? String
        await MainActor.run {
            Router.shared.tab = .home
            Router.shared.selectedMartUid = mart
            Router.shared.highlightItemUid = item
            if let item, let it = DataStore.shared.mainContext.item(uid: item) {
                it.reminderAt = nil
                DataStore.shared.mainContext.commit()
            }
        }
    }
}

struct MainTabs: View {
    @EnvironmentObject private var router: Router

    var body: some View {
        TabView(selection: $router.tab) {
            HomeView()
                .tabItem { Label("구매예정", systemImage: "list.bullet") }
                .tag(Router.Tab.home)
            CompletedView()
                .tabItem { Label("완료", systemImage: "checkmark.circle") }
                .tag(Router.Tab.completed)
            SettingsView()
                .tabItem { Label("설정", systemImage: "gearshape") }
                .tag(Router.Tab.settings)
        }
    }
}
