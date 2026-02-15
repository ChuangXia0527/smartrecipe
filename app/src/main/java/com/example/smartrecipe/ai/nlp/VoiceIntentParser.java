package com.example.smartrecipe.ai.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VoiceIntentParser {

    // 你食谱里常见的食材词（后面你可以逐步扩充）
    private static final Set<String> INGREDIENT_DICT = new HashSet<>(Arrays.asList(
            "番茄","鸡蛋","葱","盐","土豆","牛肉","胡萝卜","洋葱","西兰花","蒜",
            "鸡胸肉","生菜","黄瓜","青椒","虾仁","紫菜","面条","豆腐","花椒","豆瓣酱",
            "酸奶","香蕉","蓝莓","坚果"
    ));

    // 标签词典（和你 recipes.json tags 对齐就好）
    private static final Set<String> TAG_DICT = new HashSet<>(Arrays.asList(
            "低脂","高蛋白","清淡","素食","减脂","增肌","快手","微辣","下饭","甜品","主食"
    ));

    public static VoiceIntent parse(String textRaw) {
        VoiceIntent intent = new VoiceIntent();
        if (textRaw == null) return intent;

        String text = normalize(textRaw);

        // 1) 标签解析（偏好/避免）
        for (String tag : TAG_DICT) {
            if (text.contains(tag)) {
                if (isNegated(text, tag)) intent.avoidTags.add(tag);
                else intent.needTags.add(tag);
            }
        }
        // “不要辣/不吃辣” 这种，统一映射到“微辣”避免（你食谱标签里是微辣）
        if (containsAny(text, "不要辣","不吃辣","别辣","不辣","不要麻辣","不吃麻辣")) {
            intent.avoidTags.add("微辣");
        }

        // 2) 食材解析（想要/避免）
        for (String ing : INGREDIENT_DICT) {
            if (text.contains(ing)) {
                if (isNegated(text, ing)) intent.avoidIngredients.add(ing);
                else intent.needIngredients.add(ing);
            }
        }

        return intent;
    }

    private static String normalize(String s) {
        // 简单归一化：去空格、统一符号
        return s.replace(" ", "")
                .replace("，", ",")
                .replace("。", ".")
                .replace("；", ";")
                .toLowerCase();
    }

    // 判断某个词是否在“不要/不吃/忌口/过敏/不想”这类否定语境里
    private static boolean isNegated(String text, String token) {
        int idx = text.indexOf(token);
        if (idx <= 0) return false;
        int start = Math.max(0, idx - 4);
        String left = text.substring(start, idx);
        return containsAny(left, "不吃", "不要", "不想", "忌口", "过敏", "别");
    }

    private static boolean containsAny(String text, String... kws) {
        for (String k : kws) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
