//
//  LiveViewController.swift
//  ThetaPlugin
//
//  Created by Ivan Luque on 2018/02/25.
//

import UIKit
import Foundation

@objc protocol CameraCaptureProtocol {
    func cameraCaptureDidFinish(fileURI: String?)
}

@objcMembers class LiveViewController: UIViewController, UIPickerViewDelegate , UIPickerViewDataSource{
    
    @IBOutlet weak var shutterButton: UIButton!
    @IBOutlet weak var liveView: UIImageView!
    @IBOutlet weak var activityIndicator: UIActivityIndicatorView!
    
    @IBOutlet weak var optionsScrollView: UIScrollView!
    @IBOutlet weak var brightnessLabel: UILabel!
    
    public var fileURI: String?
    public var delegate: CameraCaptureProtocol?
    
    
    @IBOutlet weak var brightnessSlider: UISlider!
    
    private var thetaApi: ThetaApi!
    private var thumbnail: UIImage?
    private var imageInfo: HttpImageInfo?
    private var observer: NSObjectProtocol?
    private static var isPreviewShowing = false
    
    let options: [String] = Localizer.getStrings(keys: ["off", "hdr", "noise", "dr_comp"])
    let optionsValues: [String] = ["off", "hdr", "Noise Reduction", "DR Comp"]
    let evNumbers: [Double] = [-2.0, -1.7, -1.3, -1.0, -0.7, -0.3, 0.0, 0.3, 0.7, 1.0, 1.3, 1.7, 2.0]
    let timerOptions = Localizer.getStrings(keys: ["timer", "1sec", "2sec", "3sec", "4sec", "5sec", "6sec", "7sec", "8sec", "9sec", "10sec"])

    var timerPickerView: UIPickerView = UIPickerView()
    var loadingBoxView = UIView()
    
    @IBOutlet weak var cameraController: UIView!
    @IBOutlet weak var timerText: UITextField!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let navigationCloseButton = self.navigationItem.rightBarButtonItem
        navigationCloseButton?.title = Localizer.getString(key: "navigation_close_button")
        
        cameraController.isHidden = true
        setUpTimerView()
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        observer = NotificationCenter.default.addObserver(forName: UIApplication.willEnterForegroundNotification,
                                                          object: nil,
                                                          queue: OperationQueue.main)
        { [unowned self](_) in
            self.checkWifiAndConnectToTheta()
        }
        
        dispatchMain { [unowned self] in
            self.checkWifiAndConnectToTheta()
        }
    }
    
    func createAlert(_ message: String, buttons: [(String, () -> Void)]) -> UIAlertController {
        dismissLoading()
        let alert = UIAlertController(title: Localizer.getString(key:"dialog_confirm"), message: message, preferredStyle: .alert)
        buttons.forEach { button in
            let action = UIAlertAction(title: button.0, style: .default) { _ in button.1() }
            alert.addAction(action)
        }
        return alert
    }
    
    func showLoading() {
        loadingBoxView = UIView(frame: CGRect(x:((self.view.bounds.width-80)/2), y:300 , width: 80, height: 80))
        loadingBoxView.backgroundColor = UIColor.black
        loadingBoxView.alpha = 0.9
        loadingBoxView.layer.cornerRadius = 10
        // Spin config:
        let activityView = UIActivityIndicatorView(style: UIActivityIndicatorView.Style.whiteLarge)
        activityView.frame = CGRect(x: 20, y: 12, width: 40, height: 40)
        activityView.startAnimating()
        // Text config:
        let textLabel = UILabel(frame: CGRect(x: 0, y: 50, width: 80, height: 30))
        textLabel.textColor = UIColor.white
        textLabel.textAlignment = .center
        textLabel.font = UIFont(name: textLabel.font.fontName, size: 13)
        textLabel.text = "Loading..."
        // Activate:
        loadingBoxView.addSubview(activityView)
        loadingBoxView.addSubview(textLabel)
        view.addSubview(loadingBoxView)
    }
    
    func dismissLoading() {
        loadingBoxView.removeFromSuperview()
    }
    
    func setUpTimerView(){
        self.timerPickerView.delegate = self
        self.timerPickerView.dataSource = self
        self.timerPickerView.showsSelectionIndicator = true
        self.timerText.inputView = timerPickerView
        self.timerText.text = timerOptions[0]
        let toolbar = UIToolbar()
        toolbar.sizeToFit()
        let doneButton = UIBarButtonItem(barButtonSystemItem: .done, target: self, action: #selector(self.closePickerView))
        let flexibleSpace = UIBarButtonItem(barButtonSystemItem: .flexibleSpace, target: nil, action: nil)
        toolbar.setItems([flexibleSpace, doneButton], animated: false)
        toolbar.isUserInteractionEnabled = true
        timerText.inputAccessoryView = toolbar
    }
    
    @objc func closePickerView() {
        view.endEditing(true)
    }
    
    func numberOfComponents(in pickerView: UIPickerView) -> Int {
        return 1
    }
    
    func pickerView(_ pickerView: UIPickerView, numberOfRowsInComponent component: Int) -> Int {
        return timerOptions.count
    }
    
    func pickerView(_ pickerView: UIPickerView, titleForRow row: Int, forComponent component: Int) -> String? {
        return timerOptions[row]
    }
    
    func pickerView(_ pickerView: UIPickerView, didSelectRow row: Int, inComponent component: Int) {
        timerText.text = timerOptions[row]
        let options: NSDictionary = [
            "exposureDelay":row
        ]
        thetaApi.setOpitions(options: options)
    }
    
    @objc func brightnessValueChanged(sender: UISlider){
        let intValue = round(sender.value)
        sender.value = intValue
        print(sender.value)
        let realEv = evNumbers[Int(intValue)]
        self.brightnessLabel.text = Localizer.getString(key: "brightness") + " ：" + String(realEv)
        let options: NSDictionary = [
            "exposureCompensation":realEv
        ]
        thetaApi.setOpitions(options: options)
    }
    
    func initSlider(){
        let initValue: Int = 9
        self.brightnessSlider.minimumValue = 0
        self.brightnessSlider.maximumValue = 12
        self.brightnessSlider.setValue(Float(initValue), animated: false)
        let realEv = evNumbers[initValue]
        self.brightnessLabel.text = Localizer.getString(key: "brightness") + " ：" + String(realEv)
        self.brightnessSlider.addTarget(self, action:#selector(self.brightnessValueChanged(sender:)), for: .valueChanged)
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        if(thetaApi != nil){
            stopPreviewFromCamera()
        }
        NotificationCenter.default.removeObserver(observer!)
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destinationViewController.
        // Pass the selected object to the new view controller.
        
        if let viewerController = segue.destination as? ImageViewController {
//            viewerController.imageView.image = thumbnail
            DispatchQueue.global(qos: .userInitiated).async { [weak self] in
                guard let `self` = self else { return }
                let request = URLRequest(url: URL(string: self.imageInfo!.file_id)!) as? NSMutableURLRequest
                let session = HttpSession(request: request)
                viewerController.getObject(self.imageInfo, with: session)
            }
        }

    }
    
    func checkWifiAndConnectToTheta() {
        self.showLoading()
        if Wifi.connectedToThetaWifi() {
            thetaApi = ThetaApi()
            LiveViewController.isPreviewShowing = true
            thetaApi.connectToCamera { [unowned self] connected in
                if connected {
                    self.shutterButton.isEnabled = true
                    self.thetaApi.startLiveView { [unowned self] image in
                        if LiveViewController.isPreviewShowing == true {
                            self.dismissLoading()
                            self.liveView.image = image
                        }
                    }
                    self.optionsButtonCreation()
                    self.initSlider()
                    self.cameraController.isHidden = false
                } else {
                    self.showCouldNotConnectToCameraAlert()
                }
            }
        } else {
            showCouldNotConnectToCameraAlert();
        }
    }
    
    private func showCouldNotConnectToCameraAlert() {
        let alert = createAlert( Localizer.getString(key: "dialog_no_theta_wifi"), buttons:
            [
                (Localizer.getString(key:"dialog_settings"), {
                    if #available(iOS 10.0, *) {
                        UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                    } else {
                        UIApplication.shared.openURL(URL(string: UIApplication.openSettingsURLString)!)
                    }
                }),
                (Localizer.getString(key:"dialog_neutral_button"), {
                    self.delegate?.cameraCaptureDidFinish(fileURI: nil)
                    self.dismiss(animated: true, completion: nil)
                })
            ])
        present(alert, animated: true, completion: nil)
    }
 
    @IBAction func onShutterPressed(sender: UIButton) {
        // Show progress alert
        activityIndicator.startAnimating()
        shutterButton.isEnabled = false
        UIApplication.shared.beginIgnoringInteractionEvents()
        thetaApi.takePicture { [unowned self] thumbnail, info in
            self.activityIndicator.stopAnimating()
            self.shutterButton.isEnabled = true
            UIApplication.shared.endIgnoringInteractionEvents()
            self.thumbnail = thumbnail
            guard let info = info else {
                // TODO: Error dialog
                return
            }
            self.imageInfo = info
            self.performSegue(withIdentifier: "ImageViewController", sender: self)
        }
    }
    @IBAction func cancelLiveViewPressed(_ sender: Any) {
        LiveViewController.isPreviewShowing = false
        stopPreviewFromCamera()
        dismiss(animated: true, completion: nil)
    }
    
    private func stopPreviewFromCamera(){
        self.thetaApi.stopLiveView()
        self.delegate?.cameraCaptureDidFinish(fileURI: nil)
    }
    
    @IBAction func unwindImageViewer(segue: UIStoryboardSegue) {
        if let controller = segue.source as? ImageViewController {
            self.fileURI = controller.fileURI
        }
        self.delegate?.cameraCaptureDidFinish(fileURI: self.fileURI)
        dismiss(animated: true, completion: nil);
    }
    
    func optionsButtonCreation() {
        
        optionsScrollView.isScrollEnabled = true
        optionsScrollView.isUserInteractionEnabled = true
        
        let numberOfButtons = options.count
        
        var px = 0
        for i in 0...(numberOfButtons-1) {
            let optionButton = UIButton()
            optionButton.tag = i
            optionButton.frame = CGRect(x: px+10, y: 10, width: 110, height: 45)
            optionButton.backgroundColor = UIColor.black
            optionButton.setTitle(options[i], for: .normal)
            optionButton.titleLabel?.font = UIFont.systemFont(ofSize: 13)
            optionButton.addTarget(self, action: #selector(selectButtonAction), for: .touchUpInside)
            optionButton.setTitleColor(UIColor.gray, for: .normal)
            optionButton.setTitleColor(UIColor.white, for: .selected)
            optionsScrollView.addSubview(optionButton)
            if i == 1 {
                optionButton.isSelected = true
            }
            px = px + 110
        }
        
        optionsScrollView.contentSize = CGSize(width: px, height: 10)
    }
    
    @objc func selectButtonAction(sender: UIButton) {
        print("Hello \(sender.tag) is Selected")
        print(sender.isSelected)
        optionsScrollView.subviews.forEach({
            if let button = $0 as? UIButton{
                button.isSelected = false
            }
        })
        sender.isSelected = true
        //optionsValues
        let options: NSDictionary = [
            "_filter":optionsValues[sender.tag]
        ]
        thetaApi.setOpitions(options: options)
    }
}
