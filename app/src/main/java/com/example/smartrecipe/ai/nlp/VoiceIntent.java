package com.example.smartrecipe.ai.nlp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VoiceIntent {
    public Set<String> needIngredients = new HashSet<>();
    public Set<String> avoidIngredients = new HashSet<>();
    public Set<String> needTags = new HashSet<>();   // 低脂/高蛋白/清淡/素食/减脂/增肌等
    public Set<String> avoidTags = new HashSet<>();  // 不要辣 等

    public String toReadable() {
        return "想要食材：" + needIngredients +
                "\n避免食材：" + avoidIngredients +
                "\n偏好标签：" + needTags +
                "\n避免标签：" + avoidTags;
    }
}
