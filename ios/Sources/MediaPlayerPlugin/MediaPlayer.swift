import Foundation
import Capacitor
import UIKit
import AVKit

@objc public class MediaPlayer: NSObject {
    var bridge: CAPBridgeProtocol?

    var controllers: [String: MediaPlayerController] = [:]

    @objc func setBridge(bridge: CAPBridgeProtocol) {
        self.bridge = bridge
    }

    @objc func create(
        call: CAPPluginCall,
        playerId: String,
        url: URL,
        placement: MediaPlayerPlacementOptions,
        ios: MediaPlayerIosOptions,
        extra: MediaPlayerExtraOptions
    ) {
        DispatchQueue.main.sync {
            guard getMediaPlayerController(playerId: playerId) == nil else {
                call.resolve(["result": false, "method": "create", "message": "Player with playerId \(playerId) is already created"])
                return
            }

            let mediaPlayerController = MediaPlayerController(
                playerId: playerId, url: url, placement: placement, ios: ios, extra: extra
            )
            self.bridge?.viewController?.addChild(mediaPlayerController)
            self.bridge?.viewController?.view.addSubview(mediaPlayerController.view)
            mediaPlayerController.didMove(toParent: self.bridge?.viewController)

            self.addMediaPlayerController(playerId: playerId, controller: mediaPlayerController)
            call.resolve(["result": true, "method": "create", "value": playerId])
        }
    }

    @objc func play(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "play", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.play()
        call.resolve(["result": true, "method": "play", "value": playerId])
    }
    @objc func pause(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "pause", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.pause()
        call.resolve(["result": true, "method": "pause", "value": playerId])
    }
    @objc func getDuration(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "getDuration", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getDuration", "value": controller.duration])
    }
    @objc func getCurrentTime(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "getCurrentTime", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getCurrentTime", "value": CMTimeGetSeconds(controller.player.currentTime())])
    }

    @objc func setCurrentTime(call: CAPPluginCall, playerId: String, time: Double) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "setCurrentTime", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.seek(to: CMTimeMake(value: Int64(time*1000), timescale: 1000))
        call.resolve(["result": true, "method": "setCurrentTime", "value": playerId])
    }
    @objc func isPlaying(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "isPlaying", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "isPlaying", "value": controller.player.timeControlStatus == .playing])
    }
    @objc func isMuted(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "isMuted", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "isMuted", "value": controller.player.isMuted])
    }
    @objc func setVisibilityBackgroundForPiP(call: CAPPluginCall, playerId: String, isVisible: Bool) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "setVisibilityBackgroundForPiP", "message": "Player with playerId \(playerId) not found"])
            return
        }
        DispatchQueue.main.sync {
            if controller.isInPipMode == true {
                controller.view.isHidden = !isVisible
            }
            call.resolve(["result": true, "method": "setVisibilityBackgroundForPiP", "value": controller.view.isHidden])
        }
    }
    @objc func mute(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "mute", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.isMuted = true
        call.resolve(["result": true, "method": "mute", "value": true])
    }
    @objc func getVolume(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "getVolume", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getVolume", "value": controller.player.volume])
    }
    @objc func setVolume(call: CAPPluginCall, playerId: String, volume: Float) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "setVolume", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.volume = volume
        call.resolve(["result": true, "method": "setVolume", "value": volume])
    }
    @objc func getRate(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "getRate", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getRate", "value": controller.player.rate])
    }
    @objc func setRate(call: CAPPluginCall, playerId: String, rate: Float) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "setRate", "message": "Player with playerId \(playerId) not found"])
            return
        }
        controller.player.rate = rate
        call.resolve(["result": true, "method": "setRate", "value": rate])
    }
    @objc func remove(call: CAPPluginCall, playerId: String) {
        guard let controller = getMediaPlayerController(playerId: playerId) else {
            call.resolve(["result": false, "method": "remove", "message": "Player with playerId \(playerId) not found"])
            return
        }
        DispatchQueue.main.sync {
            controller.releasePlayer()
            removeMediaPlayerController(playerId: playerId)
            call.resolve(["result": true, "method": "remove", "value": playerId])
        }
    }
    @objc func removeAll(call: CAPPluginCall) {
        DispatchQueue.main.async {
            // First thoroughly clean up each player
            for (_, controller) in self.controllers {
                // Stop playback
                controller.player.pause()

                // Clear any items from the player
                controller.player.replaceCurrentItem(with: nil)

                // Reset player state if needed
                controller.player.seek(to: CMTime.zero)
                controller.player.rate = 0

                // Remove any observers the controller has
                controller.removeObservers()

                // Release the player through controller method
                controller.releasePlayer()
            }

            // Reset the audio session
            do {
                try AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
            } catch {
                print("Failed to deactivate audio session: \(error)")
            }

            // Now clear the controllers array
            self.removeAllMediaPlayerControllers()

            call.resolve(["result": true, "method": "removeAll", "value": true])
        }
    }

}
