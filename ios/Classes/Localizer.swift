//
//  Localizer.swift
//  ricoh_theta_plugin
//
//  Created by John Ha on 6/20/19.
//

import Foundation

public class Localizer: NSObject{
    
    @objc public static func getString(key: String) -> String{
        var res = key
        if let bundle : Bundle = Bundle(identifier: SwiftRicohThetaPlugin.BUNDLE_ID ){
            res = NSLocalizedString(key, tableName: nil, bundle: bundle, value: "", comment:"")
        }
        return res;
    }
    
    public static func getStrings(keys: [String]) -> [String]{
        var res = keys
        
        for (index, key) in keys.enumerated() {
            if let bundle : Bundle = Bundle(identifier: SwiftRicohThetaPlugin.BUNDLE_ID ){
                 res[index] = NSLocalizedString(key, tableName: nil, bundle: bundle, value: "", comment:"")
            }
        }
        return res;
    }
}
