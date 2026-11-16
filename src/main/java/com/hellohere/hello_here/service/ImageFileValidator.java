package com.hellohere.hello_here.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.web.multipart.MultipartFile;

public final class ImageFileValidator {

    private ImageFileValidator() {
    }

    public static ImageDimensions inspectHeader(MultipartFile file) {
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
                return new ImageDimensions(reader.getWidth(0), reader.getHeight(0));
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
        } catch (IOException exception) {
            throw new IllegalArgumentException("正しい画像ファイルではありません", exception);
        }
    }

    private static IllegalArgumentException invalidImage() {
        return new IllegalArgumentException("正しい画像ファイルではありません");
    }

    public record ImageDimensions(int width, int height) {
    }
}
