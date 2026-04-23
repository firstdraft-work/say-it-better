package com.example.communicationoptimizer.adapter.asr;

import com.example.communicationoptimizer.repository.MediaStore;
import com.example.communicationoptimizer.repository.StoredMedia;
import org.springframework.stereotype.Component;

@Component
public class MockAsrProvider implements AsrProvider {

    private final MediaStore mediaStore;

    public MockAsrProvider(MediaStore mediaStore) {
        this.mediaStore = mediaStore;
    }

    @Override
    public String getCode() {
        return "mock";
    }

    @Override
    public String transcribe(Long mediaId) {
        StoredMedia media = mediaStore.get(mediaId);
        String lowerFileName = media.getFileName().toLowerCase();

        if (lowerFileName.contains("borrow") || lowerFileName.contains("money")) {
            return "这次借钱的事情我可能帮不上你，但如果你愿意，我可以帮你一起想其他办法。";
        }

        if (lowerFileName.contains("family") || lowerFileName.contains("home")) {
            return "我想和你商量一下这件事，希望我们能找到一个都舒服的处理方式。";
        }

        return "你这周到底能不能把方案给我？别再拖了。";
    }
}
