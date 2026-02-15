package com.example.smartrecipe.ai.vision;

import android.content.Context;

import java.util.*;

public class IngredientMatcher {

    // 常见英文标签 → 中文（可扩充，但比原来少很多也没问题，因为有“词库匹配”兜底）
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
        EN_TO_CN.put("chicken", "鸡胸肉"); // 你食谱里如果有“鸡肉/鸡腿”，可再细分
    }

    // 泛化词过滤（ML Kit 经常给这些）
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "food", "dish", "cuisine", "meal", "recipe", "drink",
            "tableware", "plate", "bowl", "vegetable", "fruit", "produce",
            "ingredient", "cooking", "kitchen"
    ));

    /**
     * 输入：ML Kit labels（原始文本，英文或中文）
     * 输出：匹配到的中文食材（来自 recipes.json 自动词库）
     */
    public static Set<String> match(Context context, List<String> rawLabels) {
        Set<String> lexicon = IngredientLexicon.getAllIngredients(context);

        // 把词库预处理成可匹配形式：去空格等
        List<String> lexList = new ArrayList<>(lexicon);

        Set<String> result = new LinkedHashSet<>();
        if (rawLabels == null) return result;

        for (String raw : rawLabels) {
            if (raw == null) continue;
            String label = raw.trim();
            if (label.isEmpty()) continue;

            // 统一小写用于英文判断
            String lower = label.toLowerCase(Locale.ROOT);

            // 停用词过滤
            if (STOPWORDS.contains(lower)) continue;

            // 1) 英文先翻译到中文（若能翻译）
            String maybeCn = null;
            if (EN_TO_CN.containsKey(lower)) {
                maybeCn = EN_TO_CN.get(lower);
            } else {
                // 处理 "fried egg" 这种
                for (Map.Entry<String, String> e : EN_TO_CN.entrySet()) {
                    if (lower.contains(e.getKey())) {
                        maybeCn = e.getValue();
                        break;
                    }
                }
            }

            // 2) 如果能翻译成中文：优先拿它去匹配词库
            if (maybeCn != null) {
                String hit = findBestInLexicon(maybeCn, lexList);
                if (hit != null) result.add(hit);
                continue;
            }

            // 3) 如果 label 本身包含中文：直接拿去匹配词库（包含/相似）
            if (containsChinese(label)) {
                String hit = findBestInLexicon(label, lexList);
                if (hit != null) result.add(hit);
                continue;
            }

            // 4) 英文但没有命中翻译：尝试和词库做“轻度模糊”
            // 这里主要是为了容错，比如 label 可能是 "tomatoes"（复数）之类
            String singular = lower.endsWith("s") ? lower.substring(0, lower.length() - 1) : lower;
            if (EN_TO_CN.containsKey(singular)) {
                String hit = findBestInLexicon(EN_TO_CN.get(singular), lexList);
                if (hit != null) result.add(hit);
            }
        }

        return result;
    }

    /**
     * 在词库里找最合适的食材：优先“包含匹配”，其次“编辑距离模糊”
     */
    private static String findBestInLexicon(String queryCn, List<String> lexList) {
        String q = IngredientLexicon.normalizeCn(queryCn);
        if (q.isEmpty()) return null;

        // a) 直接相等
        for (String ing : lexList) {
            if (ing.equals(q)) return ing;
        }

        // b) 包含匹配：如 “小葱” vs “葱”
        for (String ing : lexList) {
            if (ing.contains(q) || q.contains(ing)) {
                // 优先返回更具体的那个（更长）
                return ing.length() >= q.length() ? ing : q;
            }
        }

        // c) 中文轻度模糊（编辑距离），避免太离谱：只在长度不大时启用
        //    比如 “西兰花” vs “西蓝花”
        String best = null;
        int bestDist = Integer.MAX_VALUE;

        for (String ing : lexList) {
            int d = levenshtein(q, ing);
            if (d < bestDist) {
                bestDist = d;
                best = ing;
            }
        }

        // 阈值：中文 1~2 个字差异允许
        if (best != null && bestDist <= 2) return best;
        return null;
    }

    private static boolean containsChinese(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) return true;
        }
        return false;
    }

    // Levenshtein 编辑距离（小规模用，性能足够）
    private static int levenshtein(String a, String b) {
        if (a.equals(b)) return 0;
        int n = a.length(), m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;

        int[] dp = new int[m + 1];
        for (int j = 0; j <= m; j++) dp[j] = j;

        for (int i = 1; i <= n; i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= m; j++) {
                int tmp = dp[j];
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[j] = Math.min(
                        Math.min(dp[j] + 1, dp[j - 1] + 1),
                        prev + cost
                );
                prev = tmp;
            }
        }
        return dp[m];
    }
}
