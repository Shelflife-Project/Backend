package com.shelflife.project.imageservice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.repository.ImageRepository;
import com.shelflife.project.service.ImageService;

@ExtendWith(MockitoExtension.class)
public class DeleteImageTests {
    @Spy
    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageRepository imageRepository;

    @Test
    void successfulDelete() throws Exception {
        String filename = "test.png";
        Path filePath = Paths.get(filename);
        Files.write(filePath, "image-data".getBytes());

        Image image = new Image();
        image.setFilename(filename);

        doReturn(true).when(imageService).imageExists(filename);
        doReturn(image).when(imageService).getImage(filename);

        imageService.deleteImage(filename);

        assertFalse(Files.exists(filePath));
        verify(imageRepository).delete(image);
    }

    @Test
    void throwsItemNotFoundForFile() {
        String filename = "missing.png";

        doReturn(false).when(imageService).imageExists(filename);

        assertThrows(
                ItemNotFoundException.class,
                () -> imageService.deleteImage(filename));

        verifyNoInteractions(imageRepository);
    }
}
