package murshedi.backend.ChatBot.util;

import java.util.regex.Pattern;

public class TextUtils {
    
    /**
     * Clean up the output text.
     * - Removes text within 【】 brackets
     * - Removes asterisks and hash symbols
     */
    public static String cleanText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String cleanedText = text;
        
        // Remove text within 【】 brackets
        cleanedText = Pattern.compile("【.*?】").matcher(cleanedText).replaceAll("");
        
        // Remove asterisks and hash symbols
        cleanedText = cleanedText.replace("*", "");
        cleanedText = cleanedText.replace("#", "");
        
        return cleanedText;
    }
}
