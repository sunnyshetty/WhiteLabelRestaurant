import SwiftUI
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        // Brand schemes bundle iosApp/$(BRAND)/GoogleService-Info.plist; plain Debug/Release don't,
        // and FirebaseApp.configure() would crash without it.
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
