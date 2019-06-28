//
//  RedButton.swift
//  ThetaPlugin
//
//  Created by Ivan Luque on 2018/02/25.
//

import UIKit

class RedButton: UIButton {

    override var isHighlighted: Bool {
        get {
            return super.isHighlighted
        }
        set {
            if newValue {
                backgroundColor = #colorLiteral(red: 0.937254902, green: 0.137254902, blue: 0.337254902, alpha: 1)
            }
            else {
                backgroundColor = #colorLiteral(red: 1, green: 0.2, blue: 0.4, alpha: 1)
            }
            super.isHighlighted = newValue
        }
    }
    
    override var isEnabled: Bool {
        get {
            return super.isEnabled
        }
        set {
            if newValue {
                backgroundColor = #colorLiteral(red: 1, green: 0.2, blue: 0.4, alpha: 1)
            }
            else {
                backgroundColor = #colorLiteral(red: 0.937254902, green: 0.137254902, blue: 0.337254902, alpha: 1)
            }
            super.isEnabled = newValue
        }
    }

}
