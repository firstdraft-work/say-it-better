package com.example.communicationoptimizer.adapter.llm;

import com.example.communicationoptimizer.dto.AnalysisDto;
import com.example.communicationoptimizer.dto.OptimizeRequest;
import com.example.communicationoptimizer.dto.OptimizeResponse;
import com.example.communicationoptimizer.dto.VariantDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class MockLlmProvider implements LlmProvider {

    @Override
    public String getCode() {
        return "mock";
    }

    @Override
    public OptimizeResponse generate(OptimizeRequest request) {
        OptimizeResponse response = new OptimizeResponse();
        response.setRecordId(1001L);

        String originalText = normalizeOriginalText(request.getText());
        String scene = request.getScene() != null ? request.getScene() : "workplace";
        String relation = request.getRelation() != null ? request.getRelation() : defaultRelation(scene);
        String goal = request.getGoal() != null ? request.getGoal() : "remind";

        AnalysisDto analysis = new AnalysisDto();
        analysis.setScene(scene);
        analysis.setRelation(relation);
        analysis.setGoal(goal);
        analysis.setToneTags(inferToneTags(originalText));
        analysis.setRiskPoints(inferRiskPoints(originalText));
        analysis.setEmotionLevel(inferEmotionLevel(originalText));
        response.setAnalysis(analysis);

        VariantDto tactful = new VariantDto();
        tactful.setType("very_tactful");
        tactful.setTitle("非常委婉版本");
        tactful.setText(buildVariantText(originalText, relation, goal, "very_tactful"));

        VariantDto recommended = new VariantDto();
        recommended.setType("recommended");
        recommended.setTitle("平衡推荐版本");
        recommended.setText(buildVariantText(originalText, relation, goal, "recommended"));

        VariantDto direct = new VariantDto();
        direct.setType("direct_polite");
        direct.setTitle("直接但礼貌版本");
        direct.setText(buildVariantText(originalText, relation, goal, "direct_polite"));

        response.setVariants(List.of(tactful, recommended, direct));
        return response;
    }

    private String defaultRelation(String scene) {
        if ("family".equals(scene)) {
            return "partner";
        }
        if ("social".equals(scene)) {
            return "friend";
        }
        return "colleague";
    }

    private String normalizeOriginalText(String text) {
        if (text == null || text.isBlank()) {
            return "这件事我想再沟通一下。";
        }
        return text.trim().replaceAll("\\s+", " ");
    }

    private List<String> inferToneTags(String originalText) {
        List<String> tags = new ArrayList<>();
        String lower = originalText.toLowerCase(Locale.ROOT);

        if (originalText.contains("！") || originalText.contains("!")) {
            tags.add("emotional");
        }
        if (originalText.contains("到底") || originalText.contains("必须") || originalText.contains("立刻")
                || originalText.contains("马上") || originalText.contains("别再") || lower.contains("must")) {
            tags.add("strong");
        }
        if (originalText.length() > 0) {
            tags.add("direct");
        }

        if (tags.isEmpty()) {
            tags.add("neutral");
        }
        return tags;
    }

    private List<String> inferRiskPoints(String originalText) {
        List<String> riskPoints = new ArrayList<>();
        if (originalText.contains("到底") || originalText.contains("别再") || originalText.contains("赶紧")) {
            riskPoints.add("措辞容易让对方感到被施压");
        }
        if (originalText.contains("不行") || originalText.contains("不同意") || originalText.contains("不接受")) {
            riskPoints.add("表达边界时缺少缓冲");
        }
        if (originalText.contains("？") || originalText.contains("?")) {
            riskPoints.add("问句可能带有质问感");
        }
        if (riskPoints.isEmpty()) {
            riskPoints.add("表达可以更柔和一些");
        }
        return riskPoints;
    }

    private int inferEmotionLevel(String originalText) {
        int level = 1;
        if (originalText.contains("！") || originalText.contains("!")) {
            level += 1;
        }
        if (originalText.contains("到底") || originalText.contains("别再") || originalText.contains("马上")) {
            level += 2;
        }
        if (originalText.contains("不行") || originalText.contains("不同意")) {
            level += 1;
        }
        return Math.min(level, 5);
    }

    private String buildVariantText(String originalText, String relation, String goal, String variantType) {
        String softened = soften(originalText, variantType);
        String address = relationPrefix(relation, variantType);
        String closing = goalClosing(goal, variantType);

        if ("very_tactful".equals(variantType)) {
            return address + "我想更温和地表达一下我的意思：" + softened + closing;
        }
        if ("recommended".equals(variantType)) {
            return address + softened + closing;
        }
        return address + softened;
    }

    private String soften(String text, String variantType) {
        String softened = text;
        softened = softened.replace("到底", "");
        softened = softened.replace("别再", "辛苦尽量避免再");
        softened = softened.replace("赶紧", "麻烦尽快");
        softened = softened.replace("立刻", "尽快");
        softened = softened.replace("马上", "尽快");
        softened = softened.replace("我不同意", "我这边暂时不太方便接受");
        softened = softened.replace("不同意", "暂时不太方便接受");
        softened = softened.replace("不接受", "可能暂时不太方便接受");
        softened = softened.replace("不太合适", "可能还需要再斟酌一下");
        softened = softened.replace("能不能", "是否方便");

        if ("very_tactful".equals(variantType)) {
            softened = softened.replace("你", "这边");
            softened = appendPoliteEnding(softened, "如果你方便的话，也想听听你的想法。");
        } else if ("recommended".equals(variantType)) {
            softened = appendPoliteEnding(softened, "如果有更合适的方式，我们也可以一起调整。");
        } else {
            softened = appendPoliteEnding(softened, "也请同步你的考虑。");
        }

        return softened;
    }

    private String appendPoliteEnding(String text, String ending) {
        String trimmed = text.trim();
        if (trimmed.endsWith("。") || trimmed.endsWith("！") || trimmed.endsWith("!")) {
            return trimmed + ending;
        }
        return trimmed + "。" + ending;
    }

    private String relationPrefix(String relation, String variantType) {
        if ("leader".equals(relation)) {
            return "想和您沟通一下，";
        }
        if ("elder".equals(relation)) {
            return "想和您商量一下，";
        }
        if ("partner".equals(relation) || "friend".equals(relation)) {
            return "我想和你说一下，";
        }
        if ("very_tactful".equals(variantType)) {
            return "想和你确认一下，";
        }
        return "";
    }

    private String goalClosing(String goal, String variantType) {
        if ("reject".equals(goal)) {
            if ("very_tactful".equals(variantType)) {
                return " 如果可以的话，我们看看有没有双方都更舒服的处理方式。";
            }
            if ("recommended".equals(variantType)) {
                return " 如果需要，我们可以一起讨论替代方案。";
            }
            return "";
        }
        if ("request".equals(goal)) {
            return " 麻烦你方便时回复我一下。";
        }
        if ("remind".equals(goal)) {
            return " 也辛苦你看一下时间安排。";
        }
        return "";
    }
}
