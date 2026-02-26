package com.shelflife.project.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
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
    void successfulUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "fake-image-content".getBytes());

        when(imageRepository.save(any(Image.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        imageService.uploadImage(file, "test.png");

        Path savedFile = Paths.get("test.png");

        assertTrue(Files.exists(savedFile));
        assertEquals("fake-image-content", Files.readString(savedFile));

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
