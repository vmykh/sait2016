package dev.vmykh.sait;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
	private static final String requestRegex = "request#(\\d+)";
	private static final Pattern requestPattern = Pattern.compile(requestRegex);

	public static String processRequest(String request) {
		Matcher matcher = requestPattern.matcher(request);
		if (matcher.matches()) {
			return "response#" + Integer.parseInt(matcher.group(1));
		} else {
			System.out.println("Error. Cannot parse request");
			throw new RuntimeException();
		}
	}
}
