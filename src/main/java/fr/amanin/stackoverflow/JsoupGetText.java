package fr.amanin.stackoverflow;

import org.jsoup.Jsoup;

/**
 * Answer to
 * <a href="https://stackoverflow.com/q/73827213/2678097">SO question about text trimming with entangled HTML content</a>.
 */
public class JsoupGetText {

	public static void main(String[] args) {
		var txt = "\r\n HDFC Bank </a>\r\n </div>\r\n </td>\r\n";

		var extracted = Jsoup.parse(txt).text();
		System.out.println('\''+extracted+'\'');
	}
}
