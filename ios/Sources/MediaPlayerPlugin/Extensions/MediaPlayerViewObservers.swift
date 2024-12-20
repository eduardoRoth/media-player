import MediaPlayer

extension MediaPlayerView {
    
    func addObservers() {

        self.screenRotationObserver = NotificationCenter.default.addObserver(
            forName: UIDevice.orientationDidChangeNotification,
            object: nil,
            queue: nil
        ){(_) in
            if self.ios.fullscreenOnLandscape == true {
                if UIDevice.current.orientation.isLandscape {
                    self.videoPlayer.enterFullScreen(animated: true)
                } else {
                    self.videoPlayer.exitFullScreen(animated: true)
                }
            }
        }
        
        self.backgroundObserver = NotificationCenter.default.addObserver(
            forName: UIApplication.didEnterBackgroundNotification,
            object: nil,
            queue: nil
        ){(_) in
            if self.ios.enableBackgroundPlay == true {
                self.isInBackgroundMode = true
                NotificationCenter.default.post(name: .mediaPlayerIsPlayingInBackground, object: nil, userInfo: ["playerId": self.playerId, "isPlayingInBackground": self.isInBackgroundMode])
                if self.isInPipMode != true {
                    self.videoPlayer.player = nil
                }
            } else {
                self.videoPlayer.player?.pause()
            }
        }
        
        self.foregroundObserver = NotificationCenter.default.addObserver(
            forName: UIApplication.willEnterForegroundNotification,
            object: nil,
            queue: OperationQueue.main
        ) { (_) in
            if self.ios.enableBackgroundPlay == true {
                self.isInBackgroundMode = false
                NotificationCenter.default.post(name: .mediaPlayerIsPlayingInBackground, object: nil, userInfo: ["playerId": self.playerId, "isPlayingInBackground": self.isInBackgroundMode])
                if self.isInPipMode != true {
                    self.videoPlayer.player = self.player
                }
            }
        }
        
        self.isReadyObserver = self.videoPlayer.player?
            .observe(\.status, options: [.new, .old], changeHandler: { (player, _) in
                switch player.status {
                    case .readyToPlay:
                        self.isLoaded = true
                        self.isVideoEnd = false
                        self.currentTime = 0
                        if let item = self.playerItem {
                            self.duration = CMTimeGetSeconds(item.duration)
                        }
                        self.setNowPlayingInfo()
                        self.setNowPlayingImage()
                        self.setRemoteCommandCenter()
                        NotificationCenter.default.post(name: .mediaPlayerReady, object: nil, userInfo: ["playerId": self.playerId, "currentTime": self.currentTime, "videoRate": self.rate])
                        if self.extra.autoPlayWhenReady == true {
                            player.play();
                        }
                    case .failed, .unknown:
                        self.isLoaded = false
                        self.isVideoEnd = false
                        self.clearNowPlaying()
                        self.clearRemoteCommandCenter()
                    @unknown default:
                        self.isLoaded = false
                        self.isVideoEnd = false
                        self.clearNowPlaying()
                        self.clearRemoteCommandCenter()
                }
            })
        
        self.isPlayingObserver = self.videoPlayer.player?
            .observe(\.timeControlStatus, options:[.new, .old], changeHandler: {(player, _) in
                switch player.timeControlStatus {
                case .playing:
                    NotificationCenter.default.post(
                        name: .mediaPlayerPlay,
                        object: nil,
                        userInfo: ["playerId": self.playerId]
                    )
                case .waitingToPlayAtSpecifiedRate:
                    break
                case .paused:
                    NotificationCenter.default.post(
                        name: .mediaPlayerPause,
                        object: nil,
                        userInfo: ["playerId": self.playerId]
                    )
                @unknown default:
                    NotificationCenter.default.post(
                        name: .mediaPlayerPause,
                        object: nil,
                        userInfo: ["playerId": self.playerId]
                    )
                }
            })
        
        self.rateObserver = self.videoPlayer.player?
            .observe(\.rate, options: [.new], changeHandler: {(player, observed) in
                if let item = self.playerItem {
                    self.currentTime = CMTimeGetSeconds(item.currentTime())
                    self.duration = CMTimeGetSeconds(item.duration)
                }
                let userInfo = [
                    "playerId": self.playerId,
                    "currentTime": self.currentTime,
                    "videoRate": self.rate
                ]

                if self.videoPlayer.player?.timeControlStatus == .playing {
                    if observed.newValue! > 0 {
                        if self.rate != observed.newValue! {
                            self.rate = observed.newValue!
                            player.rate = self.rate;
                        }
                        NotificationCenter.default.post(
                            name: .mediaPlayerPlay,
                            object: nil,
                            userInfo: userInfo
                        )
                    } else if observed.newValue! == 0 {
                        if !self.isVideoEnd && abs(self.currentTime - self.duration) < 0.2 {
                            player.seek(to: CMTime.zero)
                            self.currentTime = 0
                            if self.extra.loopOnEnd == true {
                                self.videoPlayer.player?.play()
                                NotificationCenter.default.post(
                                    name: .mediaPlayerPlay,
                                    object: nil,
                                    userInfo: userInfo
                                )
                            } else {
                                self.isVideoEnd = true
                                NotificationCenter.default.post(name: .mediaPlayerEnded, object: nil, userInfo: userInfo)
                            }
                        } else {
                            if(self.isInBackgroundMode != true && self.isInPipMode != true) {
                                NotificationCenter.default.post(name: .mediaPlayerPause, object: nil, userInfo: userInfo)
                            } else {
                                player.rate = self.rate
                            }
                        }
                    }
                }
            })
    }
    
    func removeObservers(){
        self.isPlayingObserver?.invalidate()
        self.isReadyObserver?.invalidate()
        self.rateObserver?.invalidate()
        
        NotificationCenter.default.removeObserver(self.backgroundObserver!)
        NotificationCenter.default.removeObserver(self.foregroundObserver!)
        NotificationCenter.default.removeObserver(self.screenRotationObserver!)
        
    }
}
