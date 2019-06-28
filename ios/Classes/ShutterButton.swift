//
//  ShutterButton.swift
//  ThetaPlugin
//
//  Created by Ivan Luque on 2018/02/25.
//

import UIKit

class ShutterButton: UIButton {
    
    

    override func beginTracking(_ touch: UITouch, with event: UIEvent?) -> Bool {
        self.setNeedsDisplay()
        return super.beginTracking(touch, with: event)
    }
    
    override func endTracking(_ touch: UITouch?, with event: UIEvent?) {
        self.setNeedsDisplay()
        super.endTracking(touch, with: event)
    }

    override func draw(_ rect: CGRect) {
        guard let context = UIGraphicsGetCurrentContext() else {return}
        
        var rect = rect
        
        if isHighlighted || !isEnabled {
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.8, green: 0, blue: 0, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 4, dy: 4)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.6, green: 0.6, blue: 0.6, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 2, dy: 2)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.3490196078, green: 0.3490196078, blue: 0.3490196078, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 1, dy: 1)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.6, green: 0.6, blue: 0.6, alpha: 1))
            context.fillPath()
        } else {
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.937254902, green: 0.137254902, blue: 0.337254902, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 4, dy: 4)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.9529411765, green: 0.9529411765, blue: 0.9529411765, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 2, dy: 2)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.3490196078, green: 0.3490196078, blue: 0.3490196078, alpha: 1))
            context.fillPath()
            
            rect = rect.insetBy(dx: 1, dy: 1)
            context.addEllipse(in: rect)
            context.setFillColor(#colorLiteral(red: 0.9529411765, green: 0.9529411765, blue: 0.9529411765, alpha: 1))
            context.fillPath()
        }
    }
}
