//
//  ThetaApi.swift
//  ThetaPlugin
//
//  Created by Ivan Luque on 2018/02/25.
//


import UIKit

func dispatchAsync(execute: @escaping () -> Void) {
    DispatchQueue.global(qos: .userInitiated).async(execute: execute)
}

func dispatchMain(execute: @escaping () -> Void) {
    DispatchQueue.main.async(execute: execute)
}

class ThetaApi: NSObject {
    
    var httpConnection: HttpConnection!
    
    override init() {
        httpConnection = HttpConnection()
        httpConnection.setTargetIp("192.168.1.1")
    }

    func setOpitions(options:NSDictionary){
        self.httpConnection.setOptions(options as? [AnyHashable : Any])
    }
    
    func connectToCamera(callback: @escaping (Bool) -> Void) {
        self.httpConnection.startAPILevel2Session { (connected) in
            dispatchMain {
             callback(connected)
            }
        }
    }
    
    func startLiveView(callback: @escaping (UIImage?) -> Void) {
        self.httpConnection.startLiveView { (rawData) in
            dispatchAsync {
                var image: UIImage?
                if let rawData = rawData {
                    image = UIImage(data: rawData)
                }
                dispatchMain {
                    callback(image)
                }
            }
        }
    }
    
    func stopLiveView() {
        self.httpConnection.stopLiveView()
    }

    func takePicture(callback: @escaping (UIImage?, HttpImageInfo?) -> Void) {
        dispatchAsync { [weak self] in
            guard let `self` = self else { return }
            var thumbnail: UIImage?
            let info = self.httpConnection.takePicture()
            if let info = info,
                let thumbData = self.httpConnection.getThumb(info.file_id) {
                thumbnail = UIImage(data: thumbData)
            }
            dispatchMain {
                callback(thumbnail, info)
            }
        }
    }
}
