package gov.uspto.common.text;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

/**
 * Utility for normalizing people names.
 *
 * @author Brian G. Feldman <brian.feldman@uspto.gov>
 *
 */
public class NameUtil {

	private static final Set<String> COMMON_SUFFIXES = new HashSet<String>(
			Arrays.asList("JR", "SR", "II", "III", "IV", "V", "VI", "ESQ"));

	private static final Set<String> lowerCaseLeadSurname = new HashSet<String>(Arrays.asList("van", "de", "da", "do",
			"du", "den", "der", "bin", "la", "le", "na", "te", "ter", "ten", "von", "della", "van't"));

	private static final Pattern surnamePrefixes = Pattern.compile("^(Ma?c|[A-Z]['�])(.+)$", Pattern.CASE_INSENSITIVE);

	/**
	 * Normalize Name Case
	 *
	 * @param text
	 * @return
	 */
	public static String normalizeCase(String text) {
		if (text == null) {
			return null;
		} else if (text.isEmpty()) {
			return text;
		} else if (text.trim().isEmpty()) {
			return text.trim();
		}

		/*
		 * Split on whitespace (after after trimming leading and trailing whitespace.)
		 */
		String[] words = text.trim().split("\\s+");
		Matcher matcher = surnamePrefixes.matcher("");

		for (int i = 0; i < words.length; i++) {
			if (lowerCaseLeadSurname.contains(words[i].toLowerCase())) {
				if (i == 0) {
					words[i] = capitalizeFirstLetter(words[i]);
				} else {
					words[i] = words[i].toLowerCase();
				}
			} else if (matcher.reset(words[i]).matches()) {
				words[i] = capitalizeFirstLetter(matcher.group(1)) + capitalizeFirstLetter(matcher.group(2));
			} else {
				words[i] = capitalizeFirstLetter(words[i]);
				words[i] = normHyphenWord(words[i]);
			}
		}

		return (Joiner.on(" ").join(words));
	}

	public static String capitalizeFirstLetter(String word) {
		if (word.length() >= 2) {
			word = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
		} else {
			word = word.toUpperCase();
		}
		return word;
	}

	/**
	 * Fix Hyphen Word
	 * 
	 * @param word
	 * @return
	 */
	private static String normHyphenWord(String word) {
		String ret = word;
		String[] hypenwords = word.split("[-\\u2013\\u2014]");
		if (hypenwords.length > 1) {
			for (int j = 0; j < hypenwords.length; j++) {
				hypenwords[j] = capitalizeFirstLetter(hypenwords[j]);
			}
			ret = Joiner.on("-").join(hypenwords);
		}
		return ret;
	}

	/**
	 * Parse Common Suffixes from Last Name
	 * 
	 * @param surname
	 * @return String["lastname w/o suffix", "found suffix"]; if no suffix returns
	 *         String["input lastname"]
	 * 
	 */
	@SuppressWarnings("unused")
	public static String[] lastnameSuffix(String surname) {
		String[] parts = surname.split(",");
		if (parts.length == 2) {
			String suffixCheck = parts[1].trim().replaceFirst("\\.$", "").toUpperCase();
			if (suffixCheck.length() < 4 && COMMON_SUFFIXES.contains(suffixCheck)) {
				return new String[] { parts[0], suffixCheck };
			}
		}

		return new String[] { surname };
	}

}