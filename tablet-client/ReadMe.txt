This directoy holds the tablet-client for Android.  
Below is a how-to for compiling Android-client

TodoList: 

* libs-dir contains backend.jar, ProtobufJava.jar and android-support-v4 
  These should be generated...

Step 1: Java JDK
-----------------
Install latest Java JDK for your platform, the free IcedTea JDK is not
enough. Also note that it has to be the full SDK and not JRE version.


Step 2: Android SDK
---------------------

Install the Eclipse/ADT dunble, either by downloading the official
adt/eclipse bundle [1] and unpack at the destination of your choice, or by
using your selectd platforms tools.

Step 3: Setup workspace
-------------------------

Start eclipse and set up worspace to "SDK-workspaces/eclipse-android-SDK"


Step 4: Update Android SDK and Eclipse plugin
---------------------------

Start the Android-SDK-manager (click on the small download-symbol) and
update. (You may choose not to install API Previews. )

In Eclipse-windows click Help->Check for updates

Step 5: Import Android projekt into eclipse workspace
-----------------------------------------------------

File->Import ,Android , Choose existing Android project Select a
reasonable name, chosse tablet-client as root, dont copy files.

Step 6: Set up virtual device
----------------------------------------------------- 

Use the Android Virtual Device manager and set up a device with
platform 4.4.2 or similar. Make sure you have donwloaded the correct
EABI version (Not android-wear or similar). I used API-level 19 and a
10" devie.

Start the devie, (this will take a while - eventually the home screen
will turn up)

Step 7: Compile and load
-----------------------------------------------------

From Run-Menu (Or Play-button) choose Run as -> Android Application
After a while you will be asked if you want to log the application
with Log-Cat. This is generally a good idea.

Now you can try out the application using the yamsLogd with for
example the Virtual Sensor and connect to the server.


Links 
---------------------------
[1] https://developer.android.com/sdk/index.html?hl=i
