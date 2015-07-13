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

package raging.goblin.speechless;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides access to localized messages.
 */
public class Messages {

	private static final SpeechLessProperties PROPERTIES = SpeechLessProperties.getInstance();

	private static Messages instance;

	private ResourceBundle messages;

	private Messages() {
		Locale locale = new Locale(PROPERTIES.getLocaleLanguage(), PROPERTIES.getLocaleLanguage());
		messages = ResourceBundle.getBundle("messages", locale);
	}

	public static Messages getInstance() {
		if (instance == null) {
			instance = new Messages();
		}
		return instance;
	}

	public String get(String key) {
		return messages.getString(key);
	}

}
