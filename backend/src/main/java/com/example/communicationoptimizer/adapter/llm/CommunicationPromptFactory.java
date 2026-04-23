package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.dto.OptimizeRequest;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommunicationPromptFactory {

    public String buildSystemPrompt() {
        return """
                你是“人际沟通优化助手”。
                目标：保留原意，把话改得更得体、更符合人物关系。

                规则：
                - 不编造事实，不改变立场
                - 不一味变软，不讨好
                - 输出像真人说话，避免公文腔
                - reject / boundary 要保留清晰立场
                - relation=leader 时，recommended 要尊重但清晰，不能过度请示
                - relation=colleague 时，recommended 尽量不用“您”
                - relation=partner 或 friend 时，必须口语化，避免职场词

                输出 JSON：
                - analysis: scene, relation, goal, toneTags, riskPoints, emotionLevel
                - variants: very_tactful, recommended, direct_polite
                """;
    }

    public String buildUserPrompt(OptimizeRequest request) {
        String original = request.getText() == null || request.getText().isBlank()
                ? "这件事我想再沟通一下。"
                : request.getText().trim();

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("originalText", original);
        context.put("scene", request.getScene());
        context.put("relation", request.getRelation());
        context.put("goal", request.getGoal());
        context.put("sourceType", request.getSourceType());
        context.put("variantRules", List.of(
                "very_tactful: 更委婉、更给台阶",
                "recommended: 符合人物关系和场景的最佳版本",
                "direct_polite: 保留边界，减少攻击性"
        ));

        return """
                请基于下面输入完成沟通优化。

                输入上下文：
                %s

                这次特别注意：
                - recommended 必须最符合人物关系，不要默认最软
                - relation=leader 且 goal=reject 时，recommended 要专业、尊重、但有判断
                - goal=reject 时，不允许把拒绝弱化成只剩模糊顾虑
                - 三版之间要有明显梯度
                - 每个版本 1 到 2 句
                - 尽量补一个简短理由或建议

                输出 JSON，结构必须是：
                {
                  "analysis": {
                    "scene": "workplace|family|social|unknown",
                    "relation": "leader|colleague|elder|partner|friend|other",
                    "goal": "request|reject|explain|apologize|remind|boundary|other",
                    "toneTags": ["direct"],
                    "riskPoints": ["表达边界时缺少缓冲"],
                    "emotionLevel": 2
                  },
                  "variants": [
                    { "type": "very_tactful", "title": "非常委婉版本", "text": "..." },
                    { "type": "recommended", "title": "平衡推荐版本", "text": "..." },
                    { "type": "direct_polite", "title": "直接但礼貌版本", "text": "..." }
                  ]
                }
                """.formatted(asPrettyContext(context));
    }

    public String outputJsonSchema() {
        return """
                {
                  "type": "object",
                  "additionalProperties": false,
                  "required": ["analysis", "variants"],
                  "properties": {
                    "analysis": {
                      "type": "object",
                      "additionalProperties": false,
                      "required": ["scene", "relation", "goal", "toneTags", "riskPoints", "emotionLevel"],
                      "properties": {
                        "scene": { "type": "string" },
                        "relation": { "type": "string" },
                        "goal": { "type": "string" },
                        "toneTags": {
                          "type": "array",
                          "items": { "type": "string" }
                        },
                        "riskPoints": {
                          "type": "array",
                          "items": { "type": "string" }
                        },
                        "emotionLevel": { "type": "integer" }
                      }
                    },
                    "variants": {
                      "type": "array",
                      "minItems": 3,
                      "items": {
                        "type": "object",
                        "additionalProperties": false,
                        "required": ["type", "title", "text"],
                        "properties": {
                          "type": { "type": "string" },
                          "title": { "type": "string" },
                          "text": { "type": "string" }
                        }
                      }
                    }
                  }
                }
                """;
    }

    private String asPrettyContext(Map<String, Object> context) {
        StringBuilder builder = new StringBuilder();
        context.forEach((key, value) -> builder.append("- ").append(key).append(": ").append(value).append("\n"));
        return builder.toString().trim();
    }
}
