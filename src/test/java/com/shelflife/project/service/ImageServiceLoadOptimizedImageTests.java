package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.shelflife.project.model.Image;
import com.shelflife.project.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
public class ImageServiceLoadOptimizedImageTests {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @TempDir
    Path tempDir;

    // -----------------------------------------------------------------------
    //  SVG on disk → returned as-is (no resize)
    // -----------------------------------------------------------------------
    @Test
    void svgFileReturnedAsIs() throws Exception {
        String svgContent = "<svg xmlns='http://www.w3.org/2000/svg'><rect width='10' height='10'/></svg>";
        String filename = "icon.svg";

        Files.write(tempDir.resolve(filename), svgContent.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/svg+xml");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 64, 64);

        assertEquals("image/svg+xml", result.mimeType());
        assertArrayEquals(svgContent.getBytes(StandardCharsets.UTF_8), result.bytes());
    }

    // -----------------------------------------------------------------------
    //  Raster larger than max → resized down while preserving aspect ratio
    // -----------------------------------------------------------------------
    @Test
    void rasterLargerThanMaxGetsResized() throws Exception {
        BufferedImage src = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(src, "png", out);

        String filename = "big.png";
        Files.write(tempDir.resolve(filename), out.toByteArray());
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/png");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 50, 50);

        assertEquals("image/png", result.mimeType());

        BufferedImage resized = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertNotNull(resized);
        assertTrue(resized.getWidth() <= 50,
                "Width should be ≤ 50, but was " + resized.getWidth());
        assertTrue(resized.getHeight() <= 50,
                "Height should be ≤ 50, but was " + resized.getHeight());
    }

    // -----------------------------------------------------------------------
    //  Raster smaller than max → returned at original dimensions
    // -----------------------------------------------------------------------
    @Test
    void rasterSmallerThanMaxUnchanged() throws Exception {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(src, "png", out);

        String filename = "small.png";
        Files.write(tempDir.resolve(filename), out.toByteArray());
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/png");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 100, 100);

        assertEquals("image/png", result.mimeType());

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertNotNull(decoded);
        assertEquals(10, decoded.getWidth());
        assertEquals(10, decoded.getHeight());
    }

    // -----------------------------------------------------------------------
    //  Raster exactly at max size → returned unchanged
    // -----------------------------------------------------------------------
    @Test
    void rasterExactlyAtMaxUnchanged() throws Exception {
        BufferedImage src = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(src, "png", out);

        String filename = "exact.png";
        Files.write(tempDir.resolve(filename), out.toByteArray());
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/png");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 64, 64);

        assertEquals("image/png", result.mimeType());

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertNotNull(decoded);
        assertEquals(64, decoded.getWidth());
        assertEquals(64, decoded.getHeight());
    }

    // -----------------------------------------------------------------------
    //  JPEG mime type → output stays JPEG
    // -----------------------------------------------------------------------
    @Test
    void jpegMimeTypePreserved() throws Exception {
        BufferedImage src = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(src, "jpg", out);

        String filename = "photo.jpg";
        Files.write(tempDir.resolve(filename), out.toByteArray());
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/jpeg");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 64, 64);

        assertEquals("image/jpeg", result.mimeType());

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertNotNull(decoded);
        assertTrue(decoded.getWidth() <= 64);
        assertTrue(decoded.getHeight() <= 64);
    }

    // -----------------------------------------------------------------------
    //  Non-square raster → aspect ratio preserved
    // -----------------------------------------------------------------------
    @Test
    void aspectRatioPreservedOnResize() throws Exception {
        // 400x100 image, fitting into 50x50 should yield 50x12 (or 50x13)
        BufferedImage src = new BufferedImage(400, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(src, "png", out);

        String filename = "wide.png";
        Files.write(tempDir.resolve(filename), out.toByteArray());
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        Image img = new Image();
        img.setFilename(filename);
        img.setMimetype("image/png");
        when(imageRepository.findByFilename(filename)).thenReturn(Optional.of(img));

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                filename, "classpath:avatar-default.svg", 50, 50);

        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(result.bytes()));
        assertNotNull(decoded);
        // Wider image: width should be capped at 50, height proportionally reduced
        assertEquals(50, decoded.getWidth());
        assertTrue(decoded.getHeight() < 50,
                "Height of wide image should be < 50 after fit, was " + decoded.getHeight());
    }

    // -----------------------------------------------------------------------
    //  Missing file → placeholder SVG returned
    // -----------------------------------------------------------------------
    @Test
    void missingFileUsesPlaceholder() throws Exception {
        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        ImageService.OptimizedImage result = imageService.loadOptimizedImage(
                "nonexistent.png", "classpath:avatar-default.svg", 64, 64);

        // Placeholder is an SVG → mime type is SVG and bytes are non-empty
        assertEquals("image/svg+xml", result.mimeType());
        assertTrue(result.bytes().length > 0, "Placeholder bytes should not be empty");
        // Sanity-check it looks like SVG
        String body = new String(result.bytes(), StandardCharsets.UTF_8);
        assertTrue(body.contains("<svg") || body.contains("<?xml"),
                "Placeholder content should be SVG XML");
    }
}
