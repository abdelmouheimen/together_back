package com.together.util;

public final class AvatarUtils {

    private AvatarUtils() {}

    public static String generateInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] words = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty() && sb.length() < 2) {
                sb.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return sb.isEmpty() ? "?" : sb.toString();
    }

    public static String computeTextColor(String hexColor) {
        if (hexColor == null || hexColor.length() < 7) return "#1A1A1A";
        try {
            int r = Integer.parseInt(hexColor.substring(1, 3), 16);
            int g = Integer.parseInt(hexColor.substring(3, 5), 16);
            int b = Integer.parseInt(hexColor.substring(5, 7), 16);
            double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
            return luminance > 0.5 ? "#1A1A1A" : "#FFFFFF";
        } catch (NumberFormatException e) {
            return "#1A1A1A";
        }
    }
}
