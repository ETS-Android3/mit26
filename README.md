# M.IT26

##### IMPORTANT: we do recommend to completely re-build the whole application, as all we do is adding new features and fixing related bugs, while the main flow of the application (see 'Development') is incorrect.

This application is inherited from BoF (Be On Fresh) and extended/maintained by M.IT26.

  - Fix bugs that causes the application to close unexpectedly on resuming an activity.
  - Add commnents on how to debug the unrepaired errors/hidden bugs.

# New Features!

  - Serial forking: set 'Priority' based on 'Q-value'
  - Call redirect

# How to enable/disable:
  - First, please read the 'Server configuration' to know how to integrate between Server and Client
  - Then, checkout the 'How to use' belows.

### Document

* [Liblinphone document] - Liblinphone JDK: https://linphone.org/snapshots/docs/liblinphone/4.5.0/multilang//index.html
* [Liblinphone API] - Liblinphone JDK: https://linphone.org/snapshots/docs/liblinphone/4.5.0/multilang//reference/java/index.html
* [Activities] - The backbone of the application: https://developer.android.com/guide/components/activities/intro-activities
* [SIP] - A quick reference: http://www.kamailio.org/docs/tutorials/sip-introduction/
* [SIP - IMS] - Details: https://realtimecommunication.wordpress.com/2017/02/04/sip-illustrated-1-basics/


### Development

* When started the application, the first [Activity] gets called is [Login]
* The basic flow: [Login] -> [MainActivity] -> [VoiceCall_Start] -> [MainActivity] -> [Login] -> application closed.
* On [Login] activity: you can login and exit the app
* On [MainActivity] activity: you can start a call [VoiceCall_Start], receive a call [VoiceCall_End], logout to go back to [Login] activity and enable redirect call in the top-right menu [Redirect].


## How to use

### Serial forking

On [Login] screen, you can set the 'Priority value' from 0.0 to 1.0, whereas the highest priority is the largest number.
* Step 1: Enable 'Serial forking' on S-CSCF
* Step 2: Change the 'Priority value' as you want, then register.
* Step 3: Simulate the call and see what happens.

### Call redirect
On [MainActivity] screen, you can see the 'Redirect call' in the top-right menu button
* Step 1: Press 'Redirect call' in the top-right menu button 
* Step 2: Enter the 'username' you want, toggle the button to 'On' and press 'Done'
* Step 3: When you see the 'Saved!' message, everything is done. Now press 'Back'.
* Step 4: Try to make a call to your user and see the magic.

### Server configuration: check out 'server_configuration.txt' and 'kamailio_scscf_sample.txt'