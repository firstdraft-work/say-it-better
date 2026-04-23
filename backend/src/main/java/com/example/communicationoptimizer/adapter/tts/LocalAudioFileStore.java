package com.example.communicationoptimizer.adapter.tts;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

@Component
public class LocalAudioFileStore {

    private static final Path AUDIO_DIR = Path.of("/tmp/communication-optimizer-generated-audio");

    public String save(byte[] audioBytes, String extension) {
        try {
            Files.createDirectories(AUDIO_DIR);
            String fileName = UUID.randomUUID() + "." + extension;
            Path target = AUDIO_DIR.resolve(fileName);
            Files.write(target, audioBytes);
            return fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save synthesized audio file", exception);
        }
    }

    public Path get(String fileName) {
        Path target = AUDIO_DIR.resolve(fileName);
        if (!Files.exists(target)) {
            throw new NoSuchElementException("audio file not found");
        }
        return target;
    }
}
