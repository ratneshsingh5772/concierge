package com.finance.concierge.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for category mapping and normalization
 * Follows Open/Closed Principle (OCP) - Easy to extend
 */
@UtilityClass
public class CategoryMappingUtil {

    // Keyword to Category mapping
    private static final Map<String, String> KEYWORD_CATEGORY_MAP = new HashMap<>();

    static {
        // Food keywords
        KEYWORD_CATEGORY_MAP.put("coffee", "Food");
        KEYWORD_CATEGORY_MAP.put("tea", "Food");
        KEYWORD_CATEGORY_MAP.put("lunch", "Food");
        KEYWORD_CATEGORY_MAP.put("dinner", "Food");
        KEYWORD_CATEGORY_MAP.put("breakfast", "Food");
        KEYWORD_CATEGORY_MAP.put("food", "Food");
        KEYWORD_CATEGORY_MAP.put("restaurant", "Food");
        KEYWORD_CATEGORY_MAP.put("cafe", "Food");
        KEYWORD_CATEGORY_MAP.put("pizza", "Food");
        KEYWORD_CATEGORY_MAP.put("burger", "Food");
        KEYWORD_CATEGORY_MAP.put("meal", "Food");

        // Transport keywords
        KEYWORD_CATEGORY_MAP.put("uber", "Transport");
        KEYWORD_CATEGORY_MAP.put("taxi", "Transport");
        KEYWORD_CATEGORY_MAP.put("cab", "Transport");
        KEYWORD_CATEGORY_MAP.put("bus", "Transport");
        KEYWORD_CATEGORY_MAP.put("train", "Transport");
        KEYWORD_CATEGORY_MAP.put("metro", "Transport");
        KEYWORD_CATEGORY_MAP.put("flight", "Transport");
        KEYWORD_CATEGORY_MAP.put("gas", "Transport");
        KEYWORD_CATEGORY_MAP.put("fuel", "Transport");
        KEYWORD_CATEGORY_MAP.put("parking", "Transport");
        KEYWORD_CATEGORY_MAP.put("toll", "Transport");

        // Entertainment keywords
        KEYWORD_CATEGORY_MAP.put("movie", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("cinema", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("concert", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("game", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("gaming", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("show", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("theatre", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("theater", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("netflix", "Entertainment");
        KEYWORD_CATEGORY_MAP.put("spotify", "Entertainment");

        // Bills/Utilities keywords
        KEYWORD_CATEGORY_MAP.put("electricity", "Bills");
        KEYWORD_CATEGORY_MAP.put("electric", "Bills");
        KEYWORD_CATEGORY_MAP.put("water", "Bills");
        KEYWORD_CATEGORY_MAP.put("internet", "Bills");
        KEYWORD_CATEGORY_MAP.put("wifi", "Bills");
        KEYWORD_CATEGORY_MAP.put("phone", "Bills");
        KEYWORD_CATEGORY_MAP.put("mobile", "Bills");
        KEYWORD_CATEGORY_MAP.put("rent", "Bills");
        KEYWORD_CATEGORY_MAP.put("bill", "Bills");
        KEYWORD_CATEGORY_MAP.put("utility", "Bills");
        KEYWORD_CATEGORY_MAP.put("utilities", "Bills");

        // Shopping keywords
        KEYWORD_CATEGORY_MAP.put("shopping", "Shopping");
        KEYWORD_CATEGORY_MAP.put("shop", "Shopping");
        KEYWORD_CATEGORY_MAP.put("clothes", "Shopping");
        KEYWORD_CATEGORY_MAP.put("clothing", "Shopping");
        KEYWORD_CATEGORY_MAP.put("shoes", "Shopping");
        KEYWORD_CATEGORY_MAP.put("amazon", "Shopping");
        KEYWORD_CATEGORY_MAP.put("flipkart", "Shopping");
        KEYWORD_CATEGORY_MAP.put("mall", "Shopping");

        // Health keywords
        KEYWORD_CATEGORY_MAP.put("doctor", "Health");
        KEYWORD_CATEGORY_MAP.put("hospital", "Health");
        KEYWORD_CATEGORY_MAP.put("medicine", "Health");
        KEYWORD_CATEGORY_MAP.put("pharmacy", "Health");
        KEYWORD_CATEGORY_MAP.put("medical", "Health");
        KEYWORD_CATEGORY_MAP.put("gym", "Health");
        KEYWORD_CATEGORY_MAP.put("fitness", "Health");
        KEYWORD_CATEGORY_MAP.put("health", "Health");

        // Grocery keywords
        KEYWORD_CATEGORY_MAP.put("grocery", "Food");
        KEYWORD_CATEGORY_MAP.put("groceries", "Food");
        KEYWORD_CATEGORY_MAP.put("supermarket", "Food");
        KEYWORD_CATEGORY_MAP.put("walmart", "Food");
        KEYWORD_CATEGORY_MAP.put("target", "Food");

        // Education keywords
        KEYWORD_CATEGORY_MAP.put("education", "Education");
        KEYWORD_CATEGORY_MAP.put("book", "Education");
        KEYWORD_CATEGORY_MAP.put("books", "Education");
        KEYWORD_CATEGORY_MAP.put("course", "Education");
        KEYWORD_CATEGORY_MAP.put("tuition", "Education");
        KEYWORD_CATEGORY_MAP.put("school", "Education");
    }

    /**
     * Get category from keyword
     */
    public static String getCategoryFromKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return KEYWORD_CATEGORY_MAP.get(keyword.toLowerCase().trim());
    }

    /**
     * Find best matching category from message
     */
    public static String findCategoryInMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Food"; // Default fallback
        }

        String lowerMessage = message.toLowerCase();

        // Try to find the first matching keyword
        for (Map.Entry<String, String> entry : KEYWORD_CATEGORY_MAP.entrySet()) {
            if (lowerMessage.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Food"; // Default fallback
    }

    /**
     * Validate if category is supported
     */
    public static boolean isSupportedCategory(String category) {
        if (category == null || category.isBlank()) {
            return false;
        }

        String normalizedCategory = category.trim();
        return KEYWORD_CATEGORY_MAP.containsValue(normalizedCategory);
    }

    /**
     * Get default category
     */
    public static String getDefaultCategory() {
        return "Food";
    }
}

