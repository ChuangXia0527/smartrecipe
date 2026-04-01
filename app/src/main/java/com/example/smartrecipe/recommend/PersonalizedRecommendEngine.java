package com.example.smartrecipe.recommend;

import com.example.smartrecipe.data.entity.Recipe;
import com.example.smartrecipe.data.local.dao.UserBehaviorDao;
import com.example.smartrecipe.data.local.entity.UserPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PersonalizedRecommendEngine {

    private static final double FAVORITE_WEIGHT = 0.45;
    private static final double RATING_WEIGHT = 0.35;
    private static final double VIEW_WEIGHT = 0.20;

    private static final double CONTENT_WEIGHT = 0.75;
    private static final double COLLAB_WEIGHT = 0.25;

    public static List<Recipe> recommend(List<Recipe> all,
                                         long userId,
                                         UserPreference preference,
                                         List<Integer> favoriteIds,
                                         List<UserBehaviorDao.RecipeScoreRow> behaviorScores,
                                         List<UserBehaviorDao.UserRecipeScoreRow> allUsersBehaviorScores,
                                         int topN) {
        if (all == null || all.isEmpty()) return new ArrayList<>();

        Map<Integer, double[]> recipeVectors = buildRecipeVectors(all);
        Map<Integer, Double> targetBehavior = buildPreferenceScoreMap(behaviorScores);

        Set<Integer> interactedRecipeIds = new HashSet<>();
        if (favoriteIds != null) interactedRecipeIds.addAll(favoriteIds);
        interactedRecipeIds.addAll(targetBehavior.keySet());

        double[] userVector = buildUserPreferenceVector(recipeVectors, targetBehavior);
        boolean hasBehavior = !targetBehavior.isEmpty();

        Map<Long, Map<Integer, Double>> allUserBehaviorMap = buildAllUsersBehaviorMap(allUsersBehaviorScores);
        Map<Integer, Double> collaborativeScores = computeCollaborativeScores(userId, interactedRecipeIds, allUserBehaviorMap);

        List<ScoredRecipe> scoredRecipes = new ArrayList<>();
        for (Recipe recipe : all) {
            if (interactedRecipeIds.contains(recipe.getId())) continue;
            if (!matchesDietaryConstraints(recipe, preference)) continue;

            double contentScore = cosineSimilarity(userVector, recipeVectors.get(recipe.getId()));
            double collaborativeScore = collaborativeScores.getOrDefault(recipe.getId(), 0.0);

            double score;
            if (hasBehavior) {
                score = CONTENT_WEIGHT * contentScore + COLLAB_WEIGHT * collaborativeScore;
            } else {
                score = coldStartScore(recipe, preference);
            }

            if (favoriteIds != null && favoriteIds.contains(recipe.getId())) {
                score += 0.15;
            }
            scoredRecipes.add(new ScoredRecipe(recipe, score));
        }

        scoredRecipes.sort(Comparator.comparingDouble((ScoredRecipe v) -> v.score).reversed());
        List<Recipe> result = new ArrayList<>();
        for (int i = 0; i < scoredRecipes.size() && i < topN; i++) {
            result.add(scoredRecipes.get(i).recipe);
        }

        if (result.isEmpty()) {
            List<Recipe> fallback = new ArrayList<>();
            for (Recipe recipe : all) {
                if (matchesDietaryConstraints(recipe, preference)) fallback.add(recipe);
            }
            fallback.sort(Comparator.comparingInt(Recipe::getMinutes));
            return fallback.subList(0, Math.min(topN, fallback.size()));
        }
        return result;
    }

    private static Map<Integer, double[]> buildRecipeVectors(List<Recipe> all) {
        Set<String> featureSet = new HashSet<>();
        for (Recipe recipe : all) {
            if (recipe.getIngredients() != null) {
                for (String ingredient : recipe.getIngredients()) {
                    featureSet.add("ing:" + ingredient.trim().toLowerCase(Locale.ROOT));
                }
            }
            if (recipe.getTags() != null) {
                for (String tag : recipe.getTags()) {
                    featureSet.add("tag:" + tag.trim().toLowerCase(Locale.ROOT));
                }
            }
        }

        List<String> features = new ArrayList<>(featureSet);
        Collections.sort(features);
        Map<String, Integer> indexMap = new LinkedHashMap<>();
        for (int i = 0; i < features.size(); i++) indexMap.put(features.get(i), i);

        Map<Integer, double[]> vectors = new HashMap<>();
        for (Recipe recipe : all) {
            double[] vector = new double[features.size() + 2];
            if (recipe.getIngredients() != null) {
                for (String ingredient : recipe.getIngredients()) {
                    Integer idx = indexMap.get("ing:" + ingredient.trim().toLowerCase(Locale.ROOT));
                    if (idx != null) vector[idx] = 1.0;
                }
            }
            if (recipe.getTags() != null) {
                for (String tag : recipe.getTags()) {
                    Integer idx = indexMap.get("tag:" + tag.trim().toLowerCase(Locale.ROOT));
                    if (idx != null) vector[idx] = 1.0;
                }
            }

            vector[features.size()] = normalizeValue(recipe.getMinutes(), 5, 90);
            vector[features.size() + 1] = normalizeValue(recipe.getCalorie(), 80, 1200);
            vectors.put(recipe.getId(), vector);
        }
        return vectors;
    }

    private static Map<Integer, Double> buildPreferenceScoreMap(List<UserBehaviorDao.RecipeScoreRow> rows) {
        Map<Integer, Double> scores = new HashMap<>();
        if (rows == null || rows.isEmpty()) return scores;

        double maxAbsFavorite = 1;
        double maxAbsRating = 1;
        double maxDuration = 1;

        for (UserBehaviorDao.RecipeScoreRow row : rows) {
            maxAbsFavorite = Math.max(maxAbsFavorite, Math.abs(row.favoriteDelta));
            maxAbsRating = Math.max(maxAbsRating, Math.abs(row.ratingDelta));
            maxDuration = Math.max(maxDuration, row.totalViewDuration);
        }

        for (UserBehaviorDao.RecipeScoreRow row : rows) {
            double favoriteNorm = normalizeSigned(row.favoriteDelta, maxAbsFavorite);
            double ratingNorm = normalizeSigned(row.ratingDelta, maxAbsRating);
            double viewNorm = normalizePositive(row.totalViewDuration, maxDuration);

            double preferenceScore = FAVORITE_WEIGHT * favoriteNorm
                    + RATING_WEIGHT * ratingNorm
                    + VIEW_WEIGHT * viewNorm;
            scores.put(row.recipeId, preferenceScore);
        }
        return scores;
    }

    private static Map<Long, Map<Integer, Double>> buildAllUsersBehaviorMap(List<UserBehaviorDao.UserRecipeScoreRow> rows) {
        Map<Long, List<UserBehaviorDao.RecipeScoreRow>> grouped = new HashMap<>();
        if (rows == null) return new HashMap<>();

        for (UserBehaviorDao.UserRecipeScoreRow row : rows) {
            List<UserBehaviorDao.RecipeScoreRow> list = grouped.get(row.userId);
            if (list == null) {
                list = new ArrayList<>();
                grouped.put(row.userId, list);
            }
            list.add(row);
        }

        Map<Long, Map<Integer, Double>> userScores = new HashMap<>();
        for (Map.Entry<Long, List<UserBehaviorDao.RecipeScoreRow>> entry : grouped.entrySet()) {
            userScores.put(entry.getKey(), buildPreferenceScoreMap(entry.getValue()));
        }
        return userScores;
    }

    private static Map<Integer, Double> computeCollaborativeScores(long userId,
                                                                    Set<Integer> interactedRecipeIds,
                                                                    Map<Long, Map<Integer, Double>> allUserBehaviorMap) {
        Map<Integer, Double> score = new HashMap<>();
        Map<Integer, Double> weight = new HashMap<>();

        Map<Integer, Double> me = allUserBehaviorMap.getOrDefault(userId, new HashMap<>());

        for (Map.Entry<Long, Map<Integer, Double>> entry : allUserBehaviorMap.entrySet()) {
            if (entry.getKey() == userId) continue;
            Map<Integer, Double> other = entry.getValue();
            double sim = cosineSimilarity(me, other);
            if (sim <= 0.01) continue;

            for (Map.Entry<Integer, Double> pref : other.entrySet()) {
                int recipeId = pref.getKey();
                if (interactedRecipeIds.contains(recipeId)) continue;
                score.put(recipeId, score.getOrDefault(recipeId, 0.0) + sim * pref.getValue());
                weight.put(recipeId, weight.getOrDefault(recipeId, 0.0) + Math.abs(sim));
            }
        }

        Map<Integer, Double> normalized = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : score.entrySet()) {
            double denom = weight.getOrDefault(entry.getKey(), 0.0);
            if (denom > 0) normalized.put(entry.getKey(), entry.getValue() / denom);
        }
        return normalized;
    }

    private static double[] buildUserPreferenceVector(Map<Integer, double[]> recipeVectors,
                                                      Map<Integer, Double> behaviorScores) {
        int dim = recipeVectors.isEmpty() ? 2 : recipeVectors.values().iterator().next().length;
        double[] out = new double[dim];

        double totalWeight = 0;
        for (Map.Entry<Integer, Double> entry : behaviorScores.entrySet()) {
            double[] vector = recipeVectors.get(entry.getKey());
            if (vector == null) continue;
            double w = Math.max(0.0, entry.getValue());
            if (w <= 0) continue;
            totalWeight += w;
            for (int i = 0; i < dim; i++) {
                out[i] += vector[i] * w;
            }
        }

        if (totalWeight > 0) {
            for (int i = 0; i < dim; i++) out[i] /= totalWeight;
            return out;
        }

        // 冷启动：无行为时返回零向量，改由 coldStartScore 处理。
        return out;
    }

    private static double coldStartScore(Recipe recipe, UserPreference preference) {
        double score = 0.1;
        if (preference == null) return score;

        if (containsPreference(recipe, preference.taste)) score += 0.35;
        if (containsPreference(recipe, preference.dietType)) score += 0.35;
        if (containsAnyIngredient(recipe, preference.commonIngredients)) score += 0.25;
        if (containsAnyIngredient(recipe, preference.allergies)) score -= 1.0;

        score += (1.0 - normalizeValue(recipe.getMinutes(), 5, 90)) * 0.03;
        score += (1.0 - normalizeValue(recipe.getCalorie(), 80, 1200)) * 0.02;
        return score;
    }

    private static boolean matchesDietaryConstraints(Recipe recipe, UserPreference preference) {
        if (preference == null) return true;
        if (containsAnyIngredient(recipe, preference.allergies)) return false;
        if (preference.dietType != null && !preference.dietType.trim().isEmpty()) {
            String dt = preference.dietType.trim();
            if (!containsPreference(recipe, dt) && ("素食".equals(dt) || "清真".equals(dt))) {
                return false;
            }
        }
        return true;
    }

    private static boolean containsPreference(Recipe recipe, String pref) {
        if (pref == null || pref.trim().isEmpty()) return false;
        String p = pref.trim().toLowerCase(Locale.ROOT);
        if (recipe.getTags() != null) {
            for (String tag : recipe.getTags()) {
                if (tag != null && tag.toLowerCase(Locale.ROOT).contains(p)) return true;
            }
        }
        if (recipe.getName() != null && recipe.getName().toLowerCase(Locale.ROOT).contains(p)) return true;
        return false;
    }

    private static boolean containsAnyIngredient(Recipe recipe, String text) {
        if (text == null || text.trim().isEmpty() || recipe.getIngredients() == null) return false;
        String[] tokens = text.replace("，", ",").split(",");
        for (String token : tokens) {
            String t = token.trim();
            if (t.isEmpty()) continue;
            for (String ing : recipe.getIngredients()) {
                if (ing != null && (ing.contains(t) || t.contains(ing))) return true;
            }
        }
        return false;
    }

    private static double normalizeValue(double value, double min, double max) {
        if (max <= min) return 0;
        double clamped = Math.max(min, Math.min(max, value));
        return (clamped - min) / (max - min);
    }

    private static double normalizeSigned(double value, double maxAbs) {
        if (maxAbs <= 0) return 0.5;
        return (value + maxAbs) / (2.0 * maxAbs);
    }

    private static double normalizePositive(double value, double max) {
        if (max <= 0) return 0;
        return Math.max(0, Math.min(1, value / max));
    }

    private static double cosineSimilarity(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) return 0;
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA <= 0 || normB <= 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static double cosineSimilarity(Map<Integer, Double> a, Map<Integer, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0;
        Set<Integer> union = new HashSet<>();
        union.addAll(a.keySet());
        union.addAll(b.keySet());

        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (Integer key : union) {
            double av = a.getOrDefault(key, 0.0);
            double bv = b.getOrDefault(key, 0.0);
            dot += av * bv;
            normA += av * av;
            normB += bv * bv;
        }
        if (normA <= 0 || normB <= 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private static class ScoredRecipe {
        Recipe recipe;
        double score;

        ScoredRecipe(Recipe recipe, double score) {
            this.recipe = recipe;
            this.score = score;
        }
    }
}
