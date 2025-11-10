import MediaPlayer

extension MediaPlayerController {

    @objc private func didEnterBackground() {
        if self.ios.enableBackgroundPlay == true {
            self.isInBackgroundMode = true
            if self.isInPipMode != true {
                self.playerController.player = nil
            }
            NotificationCenter.default.post(name: .mediaPlayerIsPlayingInBackground, object: nil, userInfo: ["playerId": self.playerId, "isPlayingInBackground": self.isInBackgroundMode])
        } else if self.isInPipMode != true {
            self.player.pause()
        }
    }

    @objc private func willEnterForeground() {
        if self.ios.enableBackgroundPlay == true {
            self.isInBackgroundMode = false
            NotificationCenter.default.post(name: .mediaPlayerIsPlayingInBackground, object: nil, userInfo: ["playerId": self.playerId, "isPlayingInBackground": self.isInBackgroundMode])
            if self.isInPipMode != true {
                self.playerController.player = self.player
            }
        }
    }

    @objc private func orientationDidChange() {
        if self.ios.fullscreenOnLandscape == true {
            if UIDevice.current.orientation.isLandscape {
                self.enterFullScreen(animated: true)
            } else {
                self.exitFullScreen(animated: true)
            }
        }
    }

    func addObservers() {
        NotificationCenter.default.addObserver(self, selector: #selector(didEnterBackground), name: UIApplication.didEnterBackgroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(willEnterForeground), name: UIApplication.willEnterForegroundNotification, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(orientationDidChange), name: UIDevice.orientationDidChangeNotification, object: nil)

        self.statusObserver = self.player
            .observe(\.status, options: [.new, .old], changeHandler: { (player, _) in
                switch player.status {
                case .readyToPlay:
                    self.isVideoEnd = false
                    self.currentTime = 0
                    self.duration = CMTimeGetSeconds(self.playerItem.duration)
                    self.setNowPlayingInfo()
                    self.setNowPlayingImage()
                    self.setRemoteCommandCenter()
                    // For audio-only files, hide loading immediately
                    if self.isAudioOnly() {
                        self.setLoading(isLoading: false)
                        self.isLoaded = true
                        NotificationCenter.default.post(name: .mediaPlayerReady, object: nil, userInfo: ["playerId": self.playerId, "currentTime": self.currentTime, "videoRate": self.rate])
                        if self.extra.autoPlayWhenReady == true {
                            self.player.play()
                        }
                        // Set up buffering observers for audio
                        self.isPlaybackBufferEmptyObserver = self.player.currentItem!
                            .observe(\.isPlaybackBufferEmpty, options: [.new, .old], changeHandler: { (item, _) in
                                if item.isPlaybackBufferEmpty {
                                    self.setLoading(isLoading: true)
                                }
                            })

                        self.isPlaybackLikelyToKeepUpObserver = self.player.currentItem!
                            .observe(\.isPlaybackLikelyToKeepUp, options: [.new, .old], changeHandler: { (item, _) in
                                if item.isPlaybackLikelyToKeepUp {
                                    self.setLoading(isLoading: false)
                                }
                            })

                        self.isPlaybackBufferFullObserver = self.player.currentItem!
                            .observe(\.isPlaybackBufferFull, options: [.new, .old], changeHandler: { (item, _) in
                                if item.isPlaybackBufferFull {
                                    self.setLoading(isLoading: false)
                                }
                            })
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

        self.isReadyObserver = self.playerController
            .observe(\.isReadyForDisplay, options: [.new, .old], changeHandler: { (player, _) in
                if player.isReadyForDisplay && !self.isAudioOnly() {
                    self.setLoading(isLoading: false)
                    self.isLoaded = true
                    NotificationCenter.default.post(name: .mediaPlayerReady, object: nil, userInfo: ["playerId": self.playerId, "currentTime": self.currentTime, "videoRate": self.rate])
                    if self.extra.autoPlayWhenReady == true {
                        self.player.play()
                    } else {
                        self.setAllowHidingPlaybackControls(allowHiding: false)
                    }
                    self.isReadyObserver!.invalidate()

                    self.isPlaybackBufferEmptyObserver = self.player.currentItem!
                        .observe(\.isPlaybackBufferEmpty, options: [.new, .old], changeHandler: { (item, _) in
                            if item.isPlaybackBufferEmpty {
                                self.setLoading(isLoading: true)
                            }
                        })

                    self.isPlaybackLikelyToKeepUpObserver = self.player.currentItem!
                        .observe(\.isPlaybackLikelyToKeepUp, options: [.new, .old], changeHandler: { (item, _) in
                            if item.isPlaybackLikelyToKeepUp {
                                self.setLoading(isLoading: false)
                            }
                        })

                    self.isPlaybackBufferFullObserver = self.player.currentItem!
                        .observe(\.isPlaybackBufferFull, options: [.new, .old], changeHandler: { (item, _) in
                            if item.isPlaybackBufferFull {
                                self.setLoading(isLoading: false)
                            }
                        })
                }
            })

        self.isPlayingObserver = self.player
            .observe(\.timeControlStatus, options: [.new, .old], changeHandler: {(player, _) in
                self.setAllowHidingPlaybackControls(allowHiding: player.timeControlStatus != .paused)
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
                }
            })

        self.rateObserver = self.player
            .observe(\.rate, options: [.new], changeHandler: {(player, observed) in
                self.currentTime = CMTimeGetSeconds(self.playerItem.currentTime())
                self.duration = CMTimeGetSeconds(self.playerItem.duration)
                let userInfo = [
                    "playerId": self.playerId,
                    "currentTime": self.currentTime,
                    "videoRate": self.rate
                ]

                if self.player.timeControlStatus == .playing {
                    if observed.newValue! > 0 {
                        if self.rate != observed.newValue! {
                            self.rate = observed.newValue!
                            player.rate = self.rate
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
                                player.play()
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
                            if self.isInBackgroundMode != true && self.isInPipMode != true {
                                NotificationCenter.default.post(name: .mediaPlayerPause, object: nil, userInfo: userInfo)
                            } else {
                                player.rate = self.rate
                            }
                        }
                    }
                }
            })
    }

    func removeObservers() {
        self.isPlayingObserver?.invalidate()
        self.rateObserver?.invalidate()
        self.statusObserver?.invalidate()

        self.isPlaybackBufferFullObserver?.invalidate()
        self.isPlaybackBufferEmptyObserver?.invalidate()
        self.isPlaybackLikelyToKeepUpObserver?.invalidate()
    }
}
