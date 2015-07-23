# Speechless
MaryTTS frontend

#1. Build
Clone marytts from https://github.com/marytts/marytts and run mvn install

Install voices to local maven repo:
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-slt-hsmm -Dversion=5.1.2 -Dpackaging=jar -Dfile=lib/voice-cmu-slt-hsmm-5.1.2.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-bdl-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-bdl-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-cmu-rms-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-cmu-rms-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-obadiah-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-obadiah-hsmm-5.1.jar
* mvn install:install-file -DgroupId=de.dfki.mary -DartifactId=voice-dfki-poppy-hsmm -Dversion=5.1 -Dpackaging=jar -Dfile=lib/voice-dfki-poppy-hsmm-5.1.jar

Install libs to local maven repo:
mvn install:install-file -DgroupId=org.jnativehook -DartifactId=jnativehook -Dversion=2.0.2 -Dpackaging=jar -Dfile=lib/jnativehook-2.0.2.jar

Install lombok to local maven repo:
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -Dartifact=org.projectlombok:lombok:1.16.4
Follow instructions from https://projectlombok.org/ to setup IDE.
