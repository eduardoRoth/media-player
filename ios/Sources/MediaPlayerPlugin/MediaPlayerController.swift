import UIKit
import AVKit
import MediaPlayer

public class MediaPlayerController: UIViewController {
    var playerId: String
    var url: URL

    var placement: MediaPlayerPlacementOptions
    var ios: MediaPlayerIosOptions
    var extra: MediaPlayerExtraOptions

    var isPIPModeAvailable: Bool = false
    var isInBackgroundMode: Bool = false
    var isInPipMode: Bool = false
    var isFullscreen: Bool = false

    var currentTime: Double = 0
    var duration: Double = 0
    var rate: Float = 1.0

    var isLoaded: Bool = false
    var isVideoEnd: Bool = false
    var isRateZero: Bool = false

    var isPlayingObserver: NSKeyValueObservation?
    var statusObserver: NSKeyValueObservation?
    var isReadyObserver: NSKeyValueObservation?
    var rateObserver: NSKeyValueObservation?
    var timeObserver: NSKeyValueObservation?
    var seekObserver: NSKeyValueObservation?
    var isPlaybackBufferEmptyObserver: NSKeyValueObservation?
    var isPlaybackBufferFullObserver: NSKeyValueObservation?
    var isPlaybackLikelyToKeepUpObserver: NSKeyValueObservation?

    var screenRotationObserver: Any?
    var backgroundObserver: Any?
    var foregroundObserver: Any?

    var periodicTimeObserver: Any?
    var videoAsset: AVURLAsset
    var playerItem: AVPlayerItem

    var playerController: AVPlayerViewController
    var player: AVPlayer
    var loadingView: UIActivityIndicatorView

    init(playerId: String, url: URL, placement: MediaPlayerPlacementOptions, ios: MediaPlayerIosOptions, extra: MediaPlayerExtraOptions) {
        self.playerId = playerId
        self.url = url
        self.placement = placement
        self.ios = ios
        self.extra = extra

        if self.extra.headers != nil {
            videoAsset = AVURLAsset(url: url, options: ["AVURLAssetHTTPHeaderFieldsKey": self.extra.headers])
        } else {
            videoAsset = AVURLAsset(url: url)
        }

        self.playerController = AVPlayerViewController()
        self.playerItem = AVPlayerItem(asset: videoAsset)
        self.player = AVPlayer(playerItem: playerItem)
        self.loadingView = UIActivityIndicatorView(style: .large)

        super.init(nibName: nil, bundle: nil)

        self.playerController.delegate = self
        self.playerController.player = player
        self.addObservers()

        self.playerController.updatesNowPlayingInfoCenter = false

        if #available (iOS 16.0, *) {
            self.playerController.allowsVideoFrameAnalysis = ios.allowsVideoFrameAnalysis
        }
        if UIDevice.current.userInterfaceIdiom == UIUserInterfaceIdiom.pad,
           #available(iOS 13.0, *) {
            isPIPModeAvailable = true
        } else if #available(iOS 14.0, *) {
            isPIPModeAvailable = true
        }
        if #available(iOS 14.2, *) {
            if self.ios.automaticallyEnterPiP == true {
                self.playerController.canStartPictureInPictureAutomaticallyFromInline = true
            } else {
                self.playerController.canStartPictureInPictureAutomaticallyFromInline = false
            }
        }
        if #available(iOS 15, *) {
            self.player.audiovisualBackgroundPlaybackPolicy = self.ios.enableBackgroundPlay ? .continuesIfPossible : .pauses
        }
        self.playerController.allowsPictureInPicturePlayback = (isPIPModeAvailable && self.ios.enablePiP == true)
        if self.ios.openInFullscreen == true {
            self.enterFullScreen(animated: true)
        }
        self.playerController.videoGravity = .resizeAspectFill

        if self.extra.subtitles != nil {
            setSubtitles()
        }

        self.setLoading(isLoading: true)

        self.view.backgroundColor = .black
        self.view.translatesAutoresizingMaskIntoConstraints = false
        self.view.clipsToBounds = true

        self.playerController.view.translatesAutoresizingMaskIntoConstraints = false
        self.playerController.view.clipsToBounds = true
        self.playerController.view.contentMode = .scaleToFill

        self.loadingView.translatesAutoresizingMaskIntoConstraints = false
        self.loadingView.color = .white

        self.addChild(self.playerController)
        self.view.addSubview(self.playerController.view)
        self.playerController.didMove(toParent: self)

        self.playerController.showsPlaybackControls = self.extra.showControls

        self.view.addSubview(self.loadingView)

        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(.playback, mode: .moviePlayback)
            try audioSession.setActive(true)
        } catch {}
    }

    override public func viewDidLayoutSubviews() {
        setLayoutConstraints()
    }

    public func isAudioOnly() -> Bool {
        let tracks = videoAsset.tracks(withMediaType: .video)
        return tracks.isEmpty
    }

    public func setLoading(isLoading: Bool) {
        if isLoading {
            self.loadingView.startAnimating()
        } else {
            self.loadingView.stopAnimating()
        }
    }

    public func setAllowHidingPlaybackControls(allowHiding: Bool) {
        if self.extra.showControls {
            DispatchQueue.main.async {
                self.playerController.setValue(allowHiding, forKey: "canHidePlaybackControls")
            }
        }
    }

    public func setLayoutConstraints() {
        NSLayoutConstraint.activate([
            self.view.widthAnchor.constraint(equalToConstant: self.placement.width),
            self.view.heightAnchor.constraint(equalToConstant: self.placement.height),
            self.loadingView.centerXAnchor.constraint(equalTo: self.view.centerXAnchor),
            self.loadingView.centerYAnchor.constraint(equalTo: self.view.centerYAnchor)
        ])

        if self.placement.horizontalAlignment == "START" {
            NSLayoutConstraint.activate([
                self.view.leadingAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.leadingAnchor, constant: self.placement.horizontalMargin)
            ])
        } else if self.placement.horizontalAlignment == "CENTER" {
            NSLayoutConstraint.activate([
                self.view.centerXAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.centerXAnchor)
            ])
        } else {
            NSLayoutConstraint.activate([
                self.view.trailingAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.trailingAnchor, constant: self.placement.horizontalMargin)
            ])
        }

        if self.placement.verticalAlignment == "TOP" {
            NSLayoutConstraint.activate([
                self.view.topAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.topAnchor, constant: self.placement.verticalMargin)
            ])
        } else if self.placement.verticalAlignment == "CENTER" {
            NSLayoutConstraint.activate([
                self.view.centerYAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.centerYAnchor)
            ])
        } else {
            NSLayoutConstraint.activate([
                self.view.bottomAnchor.constraint(equalTo: self.view.superview!.safeAreaLayoutGuide.bottomAnchor, constant: self.placement.verticalMargin)
            ])
        }
    }

    public func releasePlayer() {
        self.clearNowPlaying()
        self.removeObservers()
        self.player.pause()
        self.playerController.player = nil
        self.playerController.willMove(toParent: nil)
        self.playerController.view.removeFromSuperview()
        self.playerController.removeFromParent()
        self.willMove(toParent: nil)
        self.view.removeFromSuperview()
        self.removeFromParent()
    }

    public required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
}
