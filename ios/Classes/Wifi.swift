//
//  Wifi.swift
//  ThetaPlugin
//
//  Created by Ivan Luque on 2018/03/11.
//

import UIKit
import SystemConfiguration.CaptiveNetwork

@objc class Wifi: NSObject {

    @objc static func connectedToThetaWifi() -> Bool {
        guard let ssid = getSSID() else {
            return false
        }
        return ssid.lowercased().contains("theta")
    }
    
    private static func getSSID() -> String? {
        var ssid: String?
        if let interfaces = CNCopySupportedInterfaces() as NSArray? {
            for interface in interfaces {
                if let interfaceInfo = CNCopyCurrentNetworkInfo(interface as! CFString) as NSDictionary? {
                    ssid = interfaceInfo[kCNNetworkInfoKeySSID as String] as? String
                    break
                }
            }
        }
        return ssid
    }
    
}
