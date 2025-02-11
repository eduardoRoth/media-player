// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "EduardorothMediaPlayer",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "EduardorothMediaPlayer",
            targets: ["MediaPlayerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "MediaPlayerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/MediaPlayerPlugin"),
        .testTarget(
            name: "MediaPlayerPluginTests",
            dependencies: ["MediaPlayerPlugin"],
            path: "ios/Tests/MediaPlayerPluginTests")
    ]
)
