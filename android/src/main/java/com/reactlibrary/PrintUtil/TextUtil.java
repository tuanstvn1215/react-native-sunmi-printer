package com.reactlibrary.PrintUtil;

import android.graphics.Paint;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextUtil {
    private static boolean isSpace(char ch) {
        return (ch == ' ' || ch == '\n' || ch == '\t');
    }

    public static String removeAccent(String src){
        String nfdNormalizedString = Normalizer.normalize(src, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public static List<String> getTextInLine(String text, int maxWidth, Paint paint) {
        if (text.contains("\n")){
            String[] list = text.split("\n");
            List<String> res = new ArrayList<>();
            for (String s : list) {
                if (!s.isEmpty()) {
                    res.addAll(getTextInLine(s, maxWidth, paint));
                }
            }
            return res;
        }

        float[] widths = new float[text.length()];
        paint.getTextWidths(text, widths);

        List<String> res = new ArrayList<>();

        int start = 0;
        int end;
        while (start < text.length()) {
            float w = 0f;
            end = start;
            while (w < maxWidth && end < text.length()) {
                w += widths[end];
                ++end;
            }

            if (w > maxWidth) {
                --end;
            }

            if (end == start) return res;

            if (end == text.length()) {
                res.add(text.substring(start, end).trim());
                start = end;
            } else if (isSpace(text.charAt(end))) {
                res.add(text.substring(start, end).trim());
                start = end + 1;
            } else {

                int _end = end - 1;
                while (_end > start && !isSpace(text.charAt(_end))) {
                    --_end;
                }
                if (_end != start) {
                    end = _end + 1;
                }

                res.add(text.substring(start, end).trim());
                start = end;
            }

        }

        return res;
    }

}
