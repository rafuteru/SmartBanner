import SwiftUI
import Firebase
import GoogleMobileAds
import ComposeApp

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        FirebaseApp.configure()
        MainViewControllerKt.initializeKoin()
        
        // Correct way to initialize Google Mobile Ads in Swift
        GADMobileAds.sharedInstance().start(completionHandler: nil)

        return true
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
