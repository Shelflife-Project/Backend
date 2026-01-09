package com.shelflife.project.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Image;
import com.shelflife.project.repository.ImageRepository;

@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;

    @Value("${images.path}")
    private String uploadPath;

    public Image getImage(String filename) throws ItemNotFoundException {
        Optional<Image> image = imageRepository.findByFilename(filename);

        if (!image.isPresent())
            throw new ItemNotFoundException("filename", "Image with this filename was not found");

        return image.get();
    }

    public void uploadImage(MultipartFile file, String filename)
            throws IOException, InvalidMimeTypeException {

        Path dirPath = Paths.get(uploadPath);
        Path path = Paths.get(uploadPath, filename);
        Image image;

        if(!file.getContentType().startsWith("image/"))
            throw new InvalidMimeTypeException(file.getContentType(), "Invalid type");

        if (!Files.exists(dirPath))
            Files.createDirectory(dirPath);

        try {
            image = getImage(filename);
        } catch (ItemNotFoundException e) {
            image = new Image();
        }

        image.setFilename(filename);
        image.setMimetype(file.getContentType());
        imageRepository.save(image);

        Files.write(path, file.getBytes());
    }

    public Resource loadImage(String filename, String placeholderPath) {
        ResourceLoader loader = new DefaultResourceLoader();

        Path path = Paths.get(uploadPath, filename);

        if (!imageExists(filename))
            return loader.getResource(placeholderPath);

        return loader.getResource("file:" + path.toString());
    }

    public boolean imageExists(String filename) {
        Path path = Paths.get(uploadPath, filename);
        return Files.exists(path);
    }

    public void deleteImage(String filename) throws ItemNotFoundException, IOException {
        if (!imageExists(filename))
            throw new ItemNotFoundException("filename", "Image with this name was not found");

        Path path = Paths.get(uploadPath, filename);
        Files.delete(path);
    }
}
