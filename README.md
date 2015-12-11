# SpeechLess
MaryTTS frontend with batteries included.

aap


![SpeechLess main window](images/SpeechLessScreenshotMainWindow.png?raw=true)

#1. Build
Download marytts sources from [https://github.com/marytts/marytts/archive/v5.1.2.zip](https://github.com/marytts/marytts/archive/v5.1.2.zip) and run mvn install

Install voices to local maven repo:
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-slt-hsmm -Dversion=5.1.2 -Dpackaging=jar -Dfile=lib/voice-cmu-slt-hsmm-5.1.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-bdl-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-bdl-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-rms-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-rms-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-obadiah-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-obadiah-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-poppy-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-poppy-hsmm-5.1.jar

Follow instructions from https://projectlombok.org/ to setup IDE.

#2. Usage
Download the latest release from [https://github.com/raginggoblin/speechless/releases](https://github.com/raginggoblin/speechless/releases) and run it with: `java -jar SpeechLess.one-jar.jar`  
