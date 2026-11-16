package com.hellohere.hello_here.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.web.multipart.MultipartFile;

public final class ImageFileValidator {

    private ImageFileValidator() {
    }

    public static ImageMetadata inspectHeader(MultipartFile file) {
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            if (input == null) {
                throw invalidImage();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw invalidImage();
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);
                return new ImageMetadata(reader.getWidth(0), reader.getHeight(0),
                        normalizeFormatName(reader.getFormatName()));
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }

            throw new IllegalArgumentException("正しい画像ファイルではありません", exception);
        }
    }

    public static void verifyDecodable(MultipartFile file) {
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());

            if (image == null) {
                throw invalidImage();
            }
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof IllegalArgumentException illegalArgumentException) {
                throw illegalArgumentException;
            }

            throw new IllegalArgumentException("正しい画像ファイルではありません", exception);
        }
    }

    private static IllegalArgumentException invalidImage() {
        return new IllegalArgumentException("正しい画像ファイルではありません");
    }

    private static String normalizeFormatName(String formatName) {
        String normalized = formatName.toLowerCase(Locale.ROOT);
        return "jpg".equals(normalized) ? "jpeg" : normalized;
    }

    public record ImageMetadata(int width, int height, String formatName) {
    }
}
