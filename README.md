这是一个智能食谱推荐系统APP，拥有图像识别功能和语音识别功能。

## 毕业设计写作参考

- 论文与答辩方案：`docs/GRADUATION_THESIS_PLAN.md`

## 讯飞语音识别配置

项目中的语音转文字已切换为科大讯飞 IAT WebAPI。请在项目根目录 `local.properties` 中配置以下参数：

```properties
iflytek.appId=你的讯飞APPID
iflytek.apiKey=你的讯飞APIKey
iflytek.apiSecret=你的讯飞APISecret
```

> 说明：以上配置仅用于本地构建，不会提交到 Git 仓库。
