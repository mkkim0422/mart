import UIKit

/// "쇼핑몰 앱 바로가기" for iOS. The mart NAME picks the shop (same table as Android's
/// `util/StoreSearch.kt`). iOS opens a shop's https link straight in its app when the
/// app is installed (universal links) and in Safari otherwise — so one URL per shop,
/// no package names or store fallbacks needed.
/// Search URLs are the shops' own mobile-web search pages; which ones jump into the
/// app depends on each shop's universal-link setup (not yet checked on an iPhone).
enum StoreSearch {
    struct Shop {
        let label: String
        fileprivate let hints: [String]
        fileprivate let exact: [String]
        fileprivate let search: ((String) -> String)?
        fileprivate let home: String

        var searchesItem: Bool { search != nil }

        func matches(_ name: String) -> Bool {
            hints.contains { name.range(of: $0, options: .caseInsensitive) != nil } || exact.contains { name.contains($0) }
        }

        func url(for item: String) -> URL? {
            // Strict: "&", "+", "=" inside a name ("R&B", "1+1") must not break the query.
            let q = item.trimmingCharacters(in: .whitespaces)
                .addingPercentEncoding(withAllowedCharacters: .alphanumerics) ?? ""
            return URL(string: search?(q) ?? home)
        }
    }

    private static func shop(_ label: String, _ hints: [String], exact: [String] = [], home: String,
                             search: ((String) -> String)? = nil) -> Shop {
        Shop(label: label, hints: hints, exact: exact, search: search, home: home)
    }

    /// First match wins — specific names ("이마트24", "이마트몰") before "이마트".
    private static let shops: [Shop] = [
        shop("쿠팡", ["쿠팡"], home: "https://www.coupang.com", search: { "https://www.coupang.com/np/search?q=\($0)" }),
        shop("컬리", ["컬리"], home: "https://www.kurly.com", search: { "https://www.kurly.com/search?sword=\($0)" }),
        shop("이마트24", ["이마트24"], home: "https://emart24.co.kr"),
        shop("이마트에브리데이", ["이마트에브리데이", "이마트 에브리데이"], home: "https://www.emarteveryday.co.kr"),
        shop("이마트몰", ["이마트몰"], home: "https://emart.ssg.com", search: { "https://emart.ssg.com/search.ssg?query=\($0)" }),
        shop("이마트", ["이마트"], home: "https://emart.ssg.com", search: { "https://emart.ssg.com/search.ssg?query=\($0)" }),
        shop("SSG", ["쓱", "SSG"], home: "https://m.ssg.com", search: { "https://m.ssg.com/search.ssg?query=\($0)" }),
        shop("홈플러스", ["홈플러스"], home: "https://mfront.homeplus.co.kr", search: { "https://mfront.homeplus.co.kr/search?keyword=\($0)" }),
        shop("롯데마트", ["롯데마트", "롯데슈퍼"], home: "https://www.lotteon.com/p/display/main/lottemart"),
        shop("롯데ON", ["롯데온", "롯데ON"], home: "https://www.lotteon.com", search: { "https://www.lotteon.com/search/search/search.ecn?q=\($0)" }),
        shop("다이소몰", ["다이소"], home: "https://www.daisomall.co.kr"),
        shop("오아시스", ["오아시스"], home: "https://www.oasis.co.kr"),
        shop("하나로마트", ["하나로", "농협"], home: "https://www.nonghyupmall.com"),
        shop("킴스클럽", ["킴스클럽"], home: "https://www.kimsclub.co.kr"),
        shop("우리동네GS", ["GS25", "GS더프레시", "지에스"], home: "https://www.gsretail.com"),
        shop("포켓CU", ["씨유"], exact: ["CU"], home: "https://cu.bgfretail.com"),
        shop("배민", ["배민", "배달의민족", "B마트"], home: "https://www.baemin.com"),
        shop("올리브영", ["올리브영"], home: "https://www.oliveyoung.co.kr", search: { "https://m.oliveyoung.co.kr/m/mtn/search/result?query=\($0)" }),
        shop("11번가", ["11번가"], home: "https://www.11st.co.kr", search: { "https://search.11st.co.kr/MW/search?searchKeyword=\($0)" }),
        shop("G마켓", ["G마켓", "지마켓"], home: "https://www.gmarket.co.kr", search: { "https://browse.gmarket.co.kr/search?keyword=\($0)" }),
        shop("옥션", ["옥션"], home: "https://www.auction.co.kr", search: { "https://m.auction.co.kr/n/search?keyword=\($0)" }),
        shop("무신사", ["무신사"], home: "https://www.musinsa.com", search: { "https://www.musinsa.com/search/goods?keyword=\($0)" }),
        shop("테무", ["테무", "temu"], home: "https://www.temu.com", search: { "https://www.temu.com/search_result.html?search_key=\($0)" }),
        shop("알리", ["알리", "aliexpress"], home: "https://www.aliexpress.com", search: { q in
            "https://www.aliexpress.com/w/wholesale-\(q.replacingOccurrences(of: "%20", with: "-")).html"
        }),
    ]

    static func shop(for martName: String) -> Shop? { shops.first { $0.matches(martName) } }

    @MainActor
    static func open(_ shop: Shop, item: String) {
        guard let url = shop.url(for: item) else { return }
        UIApplication.shared.open(url)
    }
}
