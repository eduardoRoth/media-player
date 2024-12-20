import Foundation
import Capacitor
import UIKit
import AVKit

@objc public class MediaPlayer: NSObject {
    var bridge:CAPBridgeProtocol?

    var players: [String: MediaPlayerView] = [:]

    @objc func setBridge(bridge: CAPBridgeProtocol) {
        self.bridge = bridge
    }
    
    @objc func create(
        call: CAPPluginCall,
        playerId: String,
        url: URL,
        ios: MediaPlayerIosOptions,
        extra: MediaPlayerExtraOptions
    ) {
        DispatchQueue.main.sync {
            guard getPlayer(playerId: playerId) == nil else {
                call.resolve(["result": false, "method": "create", "message": "Player with playerId \(playerId) is already created"])
                return
            }
            let mediaPlayerView = MediaPlayerView(
                playerId: playerId, url: url, ios: ios, extra: extra
            )
            self.bridge?.webView?.superview?.addSubview(mediaPlayerView)

            self.addPlayers(playerId: playerId, player: mediaPlayerView)
            call.resolve(["result": true, "method": "create", "value": playerId]);
        }
    }

    @objc func play(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "play", "message": "Player with playerId \(playerId) not found"])
            return
        }
        player.player?.play()
        call.resolve(["result": true, "method": "play", "value": playerId])
    }
    @objc func pause(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "pause", "message": "Player with playerId \(playerId) not found"])
            return
        }
        player.player?.pause()
        call.resolve(["result": true, "method": "pause", "value": playerId])
    }
    @objc func getDuration(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "getDuration", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getDuration", "value": player.duration])
    }
    @objc func getCurrentTime(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "getCurrentTime", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getCurrentTime", "value": CMTimeGetSeconds(player.player!.currentTime())])
    }
    
    @objc func setCurrentTime(call: CAPPluginCall, playerId: String, time: Double) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "setCurrentTime", "message": "Player with playerId \(playerId) not found"])
            return
        }
          player.player?.seek(to: CMTimeMake(value: Int64(time*1000), timescale: 1000))
        call.resolve(["result": true, "method": "setCurrentTime", "value": playerId])
    }
    @objc func isPlaying(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "isPlaying", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "isPlaying", "value": player.player?.timeControlStatus == .playing])
    }
    @objc func isMuted(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "isMuted", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "isMuted", "value": player.player!.isMuted])
    }
    @objc func setVisibilityBackgroundForPiP(call: CAPPluginCall, playerId: String, isVisible: Bool){
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "setVisibilityBackgroundForPiP", "message": "Player with playerId \(playerId) not found"])
            return
        }
        DispatchQueue.main.sync {
            if player.isInPipMode == true {
                player.isHidden = !isVisible
            }
            call.resolve(["result": true, "method": "setVisibilityBackgroundForPiP", "value": player.isHidden])
        }
    }
    @objc func mute(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "mute", "message": "Player with playerId \(playerId) not found"])
            return
        }
        player.player?.isMuted = true
        call.resolve(["result": true, "method": "mute", "value": true])
    }
    @objc func getVolume(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "getVolume", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getVolume", "value": player.player!.volume])
    }
    @objc func setVolume(call: CAPPluginCall, playerId: String, volume: Float) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "setVolume", "message": "Player with playerId \(playerId) not found"])
            return
        }
        player.player?.volume = volume
        call.resolve(["result": true, "method": "setVolume", "value": volume])
    }
    @objc func getRate(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "getRate", "message": "Player with playerId \(playerId) not found"])
            return
        }
        call.resolve(["result": true, "method": "getRate", "value": player.player!.rate])
    }
    @objc func setRate(call: CAPPluginCall, playerId: String, rate: Float) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "setRate", "message": "Player with playerId \(playerId) not found"])
            return
        }
        player.player?.rate = rate
        call.resolve(["result": true, "method": "setRate", "value": rate])
    }
    @objc func remove(call: CAPPluginCall, playerId: String) {
        guard let player = getPlayer(playerId: playerId) else {
            call.resolve(["result": false, "method": "remove", "message": "Player with playerId \(playerId) not found"])
            return
        }
        DispatchQueue.main.sync {
            player.releasePlayer()
            removePlayer(playerId: playerId)
            call.resolve(["result": true, "method": "remove", "value": playerId])
        }
    }
    @objc func removeAll(call: CAPPluginCall) {
        DispatchQueue.main.sync {
            for(_, player) in players{
                player.releasePlayer()
            }
            removeAllPlayers()
            call.resolve(["result": true, "method": "removeAll", "value": true])
        }
    }

}
