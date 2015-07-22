/*
 * Copyright 2015, RagingGoblin <http://raginggoblin.wordpress.com>
 * 
 * This file is part of SpeechLess.
 *
 *  SpeechLess is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  SpeechLess is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with SpeechLess.  If not, see <http://www.gnu.org/licenses/>.
 */

package raging.goblin.speechless.speech;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import raging.goblin.speechless.Messages;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Voice {

   @AllArgsConstructor
   public enum Gender {

      MALE(Messages.getInstance().get("male")), FEMALE(Messages.getInstance().get("female"));

      private String displayName;

      @Override
      public String toString() {
         return displayName;
      }
   };

   private static final Logger LOG = Logger.getLogger(Voice.class);
   private static final List<Voice> voices = new ArrayList<>();

   @Getter
   private String name;
   @Getter
   private String description;
   private Gender gender;
   private String language;
   private String country;

   public static List<Voice> getAllVoices() {
      if (voices.isEmpty()) {
         voices.addAll(readAllVoices());
      }
      return voices;
   }

   @Override
   public String toString() {
      return Character.toUpperCase(name.charAt(0)) + name.substring(1) + " (" + gender + ", " + language + " - "
            + country + ")";
   }

   private static List<Voice> readAllVoices() {
      List<Voice> voices = new ArrayList<>();
      String xmlDirectory = "/voices/";
      String[] files = new File(Voice.class.getResource(xmlDirectory).getFile()).list();
      for (String fileName : files) {
         if (fileName.endsWith("xml")) {
            Voice voice = readFromXml(new File(Voice.class.getResource(xmlDirectory).getFile() + fileName));
            if (voice != null) {
               voices.add(voice);
            }
         }
      }
      return voices;
   }

   private static Voice readFromXml(File file) {
      try {
         Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
         document.getDocumentElement().normalize();
         Node voice = document.getElementsByTagName("voice").item(0);
         Node description = document.getElementsByTagName("description").item(0);

         if (voice != null && voice.getNodeType() == Node.ELEMENT_NODE && description != null
               && description.getNodeType() == Node.ELEMENT_NODE) {

            String name = ((Element) voice).getAttribute("name");
            String descriptionText = ((Element) description).getTextContent();
            String genderText = ((Element) voice).getAttribute("gender");
            String localeText = ((Element) voice).getAttribute("locale");
            Locale locale = new Locale(localeText.split("-")[0], localeText.split("-")[1]);
            return new Voice(name, descriptionText, Gender.valueOf(genderText.toUpperCase()),
                  locale.getDisplayLanguage(), locale.getDisplayCountry());

         } else {
            LOG.error("No <voice> or <description> element in xml file: " + file.getAbsolutePath());
         }

      } catch (SAXException | IOException | ParserConfigurationException e) {
         LOG.error("Not able to parse xml to voice, file: " + file.getAbsolutePath(), e);
      }

      return null;
   }
}