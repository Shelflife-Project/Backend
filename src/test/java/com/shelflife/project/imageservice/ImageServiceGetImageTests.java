package com.shelflife.project.imageservice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.repository.ImageRepository;
import com.shelflife.project.service.ImageService;

@ExtendWith(MockitoExtension.class)
public class ImageServiceGetImageTests {
    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    void throwsItemNotFound() {
        when(imageRepository.findByFilename("test")).thenReturn(Optional.empty());
        assertThrows(ItemNotFoundException.class, () -> imageService.getImage("test"));
    }

    @Test
    void returnsImage() {
        Image image = new Image();

        when(imageRepository.findByFilename("test")).thenReturn(Optional.of(image));
        assertDoesNotThrow(() -> imageService.getImage("test"));
    }
}
