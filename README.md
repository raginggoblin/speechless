# Speechless
MaryTTS frontend

#1. Build
Clone marytts from https://github.com/marytts/marytts and run mvn install

Using the gui download voices, and install voices to local maven repo:
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-slt-hsmm -Dversion=5.1.2 -Dpackaging=jar -Dfile=voices/voice-cmu-slt-hsmm-5.1.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-bdl-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=voices/voice-cmu-bdl-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-rms-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=voices/voice-cmu-rms-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-obadiah-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=voices/voice-dfki-obadiah-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-poppy-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=voices/voice-dfki-poppy-hsmm-5.1.jar

Now use maven to build application.

Lombok is used as well, follow instructions from https://projectlombok.org/ to setup IDE.
