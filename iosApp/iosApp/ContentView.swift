import UIKit
import SwiftUI
import ComposeApp
import GoogleMobileAds

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return MainViewControllerKt.MainViewController { adUnitId in
            // Using the designated initializer for GADBannerView
            let bannerView = GADBannerView(adSize: BannerView)
            bannerView.adUnitID = adUnitId
            
            // In SwiftUI, finding the root controller for AdMob
            if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
               let rootVC = windowScene.windows.first?.rootViewController {
                bannerView.rootViewController = rootVC
            }
            
            bannerView.load(Request())
            return bannerView
        }

    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
