package com.example.smartrecipe.recommend;

import com.example.smartrecipe.ai.nlp.VoiceIntent;
import com.example.smartrecipe.data.entity.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecommendEngine {

    public static List<Recipe> recommendByVoiceIntent(List<Recipe> all, VoiceIntent intent, int topN) {
        if (all == null) return new ArrayList<>();

        List<ScoredRecipe> scored = new ArrayList<>();

        for (Recipe r : all) {
            int score = 0;

            // 1) 强过滤：避免食材命中直接淘汰
            if (r.getIngredients() != null) {
                boolean hitAvoid = false;
                for (String ing : r.getIngredients()) {
                    if (intent.avoidIngredients.contains(ing)) {
                        hitAvoid = true;
                        break;
                    }
                }
                if (hitAvoid) continue;
            }

            // 2) 避免标签：如“不要辣”→ 避免“微辣”
            if (r.getTags() != null) {
                boolean hitAvoidTag = false;
                for (String t : r.getTags()) {
                    if (intent.avoidTags.contains(t)) {
                        hitAvoidTag = true;
                        break;
                    }
                }
                if (hitAvoidTag) continue;
            }

            // 3) 想要食材：命中越多分越高
            int hitNeedIng = 0;
            if (r.getIngredients() != null) {
                for (String ing : r.getIngredients()) {
                    if (intent.needIngredients.contains(ing)) hitNeedIng++;
                }
            }
            score += hitNeedIng * 5;

            // 4) 偏好标签：命中加分
            int hitNeedTag = 0;
            if (r.getTags() != null) {
                for (String t : r.getTags()) {
                    if (intent.needTags.contains(t)) hitNeedTag++;
                }
            }
            score += hitNeedTag * 3;

            // 5) 基础分：让“没有关键词时”也能排出结果（可理解为“默认热门”）
            score += 1;

            scored.add(new ScoredRecipe(r, score));
        }

        // 使用 List.sort 替换 Collections.sort
        scored.sort((a, b) -> Integer.compare(b.score, a.score));

        List<Recipe> res = new ArrayList<>();
        for (int i = 0; i < scored.size() && i < topN; i++) {
            res.add(scored.get(i).recipe);
        }
        return res;
    }

    private static class ScoredRecipe {
        Recipe recipe;
        int score;

        ScoredRecipe(Recipe r, int s) {
            recipe = r;
            score = s;
        }
    }
}
