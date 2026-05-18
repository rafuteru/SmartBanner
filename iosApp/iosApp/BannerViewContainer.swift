//
//  BannerViewContainer.swift
//  iosApp
//
//  Created by AJ on 18/05/26.
//


import UIKit
import GoogleMobileAds

class BannerViewContainer: UIView {

    private let banner = BannerView()

    init(adUnitId: String) {
        super.init(frame: .zero)

        // 1. Configure banner
        banner.adUnitID = adUnitId
        banner.adSize = AdSizeBanner   // ✅ IMPORTANT FIX

        // 2. Root VC
        let rootVC = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first?
            .windows
            .first?
            .rootViewController

        banner.rootViewController = rootVC

        // 3. Load ad
        banner.load(Request())

        // 4. Add to view
        addSubview(banner)

        banner.translatesAutoresizingMaskIntoConstraints = false

        NSLayoutConstraint.activate([
            banner.centerXAnchor.constraint(equalTo: centerXAnchor),
            banner.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
