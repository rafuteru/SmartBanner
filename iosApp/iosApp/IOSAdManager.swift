import Foundation
import GoogleMobileAds
import UIKit
import ComposeApp

class IOSAdManager: NSObject, AdManager {

    static let shared = IOSAdManager()

    private var rewardedAd: RewardedAd?
    private var interstitialAd: InterstitialAd?
    private var appOpenAd: AppOpenAd?
    private var interstitialDelegate: AdDelegateBridge?

    func showRewardedAd(
        adUnitId: String,
        onRewardEarned: @escaping () -> Void
    ) {

        RewardedAd.load(
            with: adUnitId,
            request: Request()
        ) { [weak self] ad, error in

            if let error = error {
                print("Rewarded failed: \(error.localizedDescription)")
                return
            }

            self?.rewardedAd = ad

            guard let rootVC = self?.getRootViewController(),
                  let rewardedAd = ad
            else { return }

            rewardedAd.present(from: rootVC) {
                print("Reward earned")
                onRewardEarned()
            }
        }
    }

    func showInterstitialAd(
        adUnitId: String,
        onAdClosed: @escaping () -> Void
    ) {

        InterstitialAd.load(
            with: adUnitId,
            request: Request()
        ) { [weak self] ad, error in

            if let error = error {
                print("Interstitial failed: \(error.localizedDescription)")
                onAdClosed()
                return
            }

            self?.interstitialAd = ad

            guard let rootVC = self?.getRootViewController(),
                  let interstitialAd = ad
            else { return }

            let delegate = AdDelegateBridge(
                onAdClosed: onAdClosed
            )

            self?.interstitialDelegate = delegate
            interstitialAd.fullScreenContentDelegate = delegate

            interstitialAd.present(from: rootVC)
        }
    }

    func showAppOpenAd(adUnitId: String) {

        AppOpenAd.load(
            with: adUnitId,
            request: Request()
        ) { [weak self] ad, error in

            if let error = error {
                print("App open failed: \(error.localizedDescription)")
                return
            }

            self?.appOpenAd = ad

            guard let rootVC = self?.getRootViewController(),
                  let appOpenAd = ad
            else { return }

            appOpenAd.present(from: rootVC)
        }
    }

    private func getRootViewController() -> UIViewController? {
        UIApplication.shared.connectedScenes
        .compactMap { $0 as? UIWindowScene }
        .flatMap { $0.windows }
        .first { $0.isKeyWindow }?
        .rootViewController
    }
}

class AdDelegateBridge: NSObject, FullScreenContentDelegate {

    let onAdClosed: () -> Void

    init(onAdClosed: @escaping () -> Void) {
        self.onAdClosed = onAdClosed
    }

    func adDidDismissFullScreenContent(
        _ ad: FullScreenPresentingAd
    ) {
        onAdClosed()
    }
}