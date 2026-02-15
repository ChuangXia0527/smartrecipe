package com.example.smartrecipe.data.entity;

import java.util.List;

public class Recipe {
    private String name;
    private List<String> ingredients; // 食材列表
    private List<String> tags; // 标签列表
    private int id;
    private int minutes; // 烹饪时间
    private int calorie; // 热量
    private List<String> steps; // 步骤

    // 获取食材列表
    public List<String> getIngredients() {
        return ingredients;
    }

    // 获取标签列表
    public List<String> getTags() {
        return tags;
    }

    // 获取菜品名称
    public String getName() {
        return name;
    }

    // 获取菜品 ID
    public int getId() {
        return id;
    }

    // 获取烹饪时间
    public int getMinutes() {
        return minutes;
    }

    // 获取热量
    public int getCalorie() {
        return calorie;
    }

    // 获取步骤列表
    public List<String> getSteps() {
        return steps;
    }

    // 其他字段和方法
}
