package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.InvalidMimeTypeException;

import com.shelflife.project.model.Image;
import com.shelflife.project.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
public class ImageServiceUploadImageTests {
    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Test
    void successfulUpload(@TempDir Path tempDir) throws Exception {
        // Generate a real 10x10 PNG in memory so ImageIO.read() can parse it
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                baos.toByteArray());

        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        when(imageRepository.findByFilename("test.png")).thenReturn(Optional.empty());
        when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        imageService.uploadImage(file, "test.png");

        assertTrue(Files.exists(tempDir.resolve("test.png")));
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void svgUploadPassesThrough(@TempDir Path tempDir) throws Exception {
        String svgContent = "<svg xmlns='http://www.w3.org/2000/svg'><rect width='10' height='10'/></svg>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "icon.svg",
                "image/svg+xml",
                svgContent.getBytes());

        ReflectionTestUtils.setField(imageService, "uploadPath", tempDir.toString());

        when(imageRepository.findByFilename("icon.svg")).thenReturn(Optional.empty());
        when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        imageService.uploadImage(file, "icon.svg");

        Path saved = tempDir.resolve("icon.svg");
        assertTrue(Files.exists(saved));
        // SVG bytes are stored as-is
        assertTrue(Files.readString(saved).contains("<svg"));
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void invalidMimeType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "audio/aac",
                "fake-image-content".getBytes());

        assertThrows(InvalidMimeTypeException.class, () -> imageService.uploadImage(file, "testInvalidMime.png"));

        Path savedFile = Paths.get("testInvalidMime.png");

        assertFalse(Files.exists(savedFile));
        verify(imageRepository, never()).save(any(Image.class));
    }
}
