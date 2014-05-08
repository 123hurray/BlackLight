package us.shandian.blacklight.support;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* This class is modified from qii/weiciyuan */
public class SpannableStringUtils
{
	private static final Pattern PATTERN_WEB = Pattern.compile("http://[a-zA-Z0-9+&@#/%?=~_\\-|!:,\\.;]*[a-zA-Z0-9+&@#/%=~_|]");
	private static final Pattern PATTERN_TOPIC = Pattern.compile("#[\\p{Print}\\p{InCJKUnifiedIdeographs}&&[^#]]+#");
	private static final Pattern PATTERN_MENTION = Pattern.compile("@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}");
	private static final Pattern PATTERN_EMOTICON = Pattern.compile("\\[(\\S+?)\\]");
	
	private static final String HTTP_SCHEME = "http://";
	private static final String TOPIC_SCHEME = "us.shandian.blacklight.topic://";
	private static final String MENTION_SCHEME = "us.shandian.blacklight.user://";
	
	public static SpannableString span(String text) {
		SpannableString ss = SpannableString.valueOf(text);
		Linkify.addLinks(ss, PATTERN_WEB, HTTP_SCHEME);
		Linkify.addLinks(ss, PATTERN_TOPIC, TOPIC_SCHEME);
		Linkify.addLinks(ss, PATTERN_MENTION, MENTION_SCHEME);
		
		// Convert to our own span
		URLSpan[] spans = ss.getSpans(0, ss.length(), URLSpan.class);
		for (URLSpan span : spans) {
			WeiboSpan s = new WeiboSpan(span.getURL());
			int start = ss.getSpanStart(span);
			int end = ss.getSpanEnd(span);
			ss.removeSpan(span);
			ss.setSpan(s, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		// Match Emoticons
		Matcher matcher = PATTERN_EMOTICON.matcher(ss);
		while (matcher.find()) {
			// Don't be too long
			if (matcher.end() - matcher.start() < 8) {
				String iconName = matcher.group(0);
				Bitmap bitmap = Emoticons.EMOTICON_BITMAPS.get(iconName);
				
				if (bitmap != null) {
					ImageSpan span = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
					ss.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		
		return ss;
	}
	
}
