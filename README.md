# AudioPlayer Add-on for Vaadin

This is a work in progress.

AudioPlayer is an add-on for Vaadin that, when complete, uses WebAudio to allow streaming playback of an arbitrary server-side PCM data buffer. Audio can be transported as OGG, MP3 or WAV. It also supports server-side control of the audio playback such as audio and stereo balance, as well as advanced control via pluggable effects, such as high- and lowpass filters.

## Building and running demo

~~~~
git clone https://github.com/drewharvey/VaadinAudioPlayer.git
mvn clean install
cd AudioPlayer-demo
mvn jetty:run
~~~~

To see the demo, navigate to http://localhost:8080/

## Development with Eclipse IDE

For further development of this add-on, the following tool-chain is recommended:
- Eclipse IDE
- m2e wtp plug-in (install it from Eclipse Marketplace)
- Vaadin Eclipse plug-in (install it from Eclipse Marketplace)
- JRebel Eclipse plug-in (install it from Eclipse Marketplace)
- Chrome browser

### Importing project

Choose File > Import... > Existing Maven Projects

Note that Eclipse may give "Plugin execution not covered by lifecycle configuration" errors for pom.xml. Use "Permanently mark goal resources in pom.xml as ignored in Eclipse build" quick-fix to mark these errors as permanently ignored in your project. Do not worry, the project still works fine. 

### Debugging server-side

If you have not already compiled the widgetset, do it now by running vaadin:install Maven target for AudioPlayer-root project.

If you have a JRebel license, it makes on the fly code changes faster. Just add JRebel nature to your AudioPlayer-demo project by clicking project with right mouse button and choosing JRebel > Add JRebel Nature

To debug project and make code modifications on the fly in the server-side, right-click the AudioPlayer-demo project and choose Debug As > Debug on Server. Navigate to http://localhost:8080/AudioPlayer-demo/ to see the application.

### Debugging client-side

Debugging client side code in the AudioPlayer-demo project:
  - run "mvn vaadin:run-codeserver" on a separate console while the application is running
  - activate Super Dev Mode in the debug window of the application or by adding ?superdevmode to the URL
  - You can access Java-sources and set breakpoints inside Chrome if you enable source maps from inspector settings.
 
## License & Authors

Add-on is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

AudioPlayer is written by Patrik Lindström and Drew Harvey of Vaadin Ltd.

