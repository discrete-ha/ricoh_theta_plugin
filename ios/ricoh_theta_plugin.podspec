#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'ricoh_theta_plugin'
  s.version          = '0.0.1'
  s.summary          = 'ricoh theta plugin '
  s.description      = <<-DESC
plugin for Ricoh Theta camera
                       DESC
  s.homepage         = 'http://air-flare.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'air-flare' => 'discreteb@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  
  s.ios.deployment_target = '8.0'
end

