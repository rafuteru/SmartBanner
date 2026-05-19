import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

struct ComposeView: UIViewControllerRepresentable {

    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController(
            createAdView: { adUnitId in
                return BannerViewContainer(adUnitId: adUnitId)
            },
            adManager: IOSAdManager.shared
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
