package com.example.smartrecipe.ai.vision;

import java.util.*;

public class IngredientMapper {

    // 英文标签 → 中文食材（你可以持续扩充）
    private static final Map<String, String> EN_TO_CN = new HashMap<>();
    static {
        EN_TO_CN.put("tomato", "番茄");
        EN_TO_CN.put("egg", "鸡蛋");
        EN_TO_CN.put("potato", "土豆");
        EN_TO_CN.put("beef", "牛肉");
        EN_TO_CN.put("broccoli", "西兰花");
        EN_TO_CN.put("garlic", "蒜");
        EN_TO_CN.put("onion", "洋葱");
        EN_TO_CN.put("carrot", "胡萝卜");
        EN_TO_CN.put("cucumber", "黄瓜");
        EN_TO_CN.put("lettuce", "生菜");
        EN_TO_CN.put("shrimp", "虾仁");
        EN_TO_CN.put("tofu", "豆腐");
        EN_TO_CN.put("noodle", "面条");
        EN_TO_CN.put("seaweed", "紫菜");
        EN_TO_CN.put("banana", "香蕉");
        EN_TO_CN.put("blueberry", "蓝莓");
        EN_TO_CN.put("yogurt", "酸奶");
        EN_TO_CN.put("pepper", "青椒");
        EN_TO_CN.put("chicken", "鸡胸肉"); // 粗映射：chicken → 鸡胸肉（你后续可更细）
    }

    // 一些泛化词，不当食材用
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "food", "dish", "cuisine", "meal", "recipe", "drink",
            "tableware", "plate", "bowl", "vegetable", "fruit", "produce"
    ));

    /**
     * 输入：ML Kit labels 文本（可能英文/可能其他）
     * 输出：你的系统食材词集合（中文）
     */
    public static Set<String> mapToIngredients(List<String> labelsLowercase) {
        Set<String> res = new LinkedHashSet<>();
        if (labelsLowercase == null) return res;

        for (String raw : labelsLowercase) {
            if (raw == null) continue;
            String s = raw.trim().toLowerCase(Locale.ROOT);

            if (s.isEmpty()) continue;
            if (STOPWORDS.contains(s)) continue;

            // 直接命中英文映射
            if (EN_TO_CN.containsKey(s)) {
                res.add(EN_TO_CN.get(s));
                continue;
            }

            // 简单包含匹配（处理如 "fried egg" 这类）
            for (Map.Entry<String, String> e : EN_TO_CN.entrySet()) {
                if (s.contains(e.getKey())) {
                    res.add(e.getValue());
                    break;
                }
            }

            // 如果 ML Kit 返回中文标签，你也可以直接放行（可选）
            // 这里做一个非常轻量的判断：包含中文就加入
            if (containsChinese(raw)) {
                res.add(raw.trim());
            }
        }
        return res;
    }

    private static boolean containsChinese(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) return true;
        }
        return false;
    }
}
