This app has three components:

1. SlamThePhoneActivity
  - this is the user's entry point to the app, for orienting them and letting them learn how to use it
2. CallStartedReceiver
  - this is how we hook into phone calls
3. WaitForSlamService
  - this service runs in the background and when it's supposed to it does the sound playing & hanging up
