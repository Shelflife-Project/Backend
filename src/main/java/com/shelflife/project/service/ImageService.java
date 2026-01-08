package com.shelflife.project.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shelflife.project.exception.ItemNotFoundException;

@Service
public class ImageService {
    @Value("${images.path}")
    private String uploadPath;

    public void uploadImage(MultipartFile file, String filename)
            throws IOException {

        Path dirPath = Paths.get(uploadPath);
        Path path = Paths.get(uploadPath, filename);
        
        if(!Files.exists(dirPath))
            Files.createDirectory(dirPath);
        
        Files.write(path, file.getBytes());
    }

    public Resource loadImage(String filename, String placeholderPath) {
        ResourceLoader loader = new DefaultResourceLoader();

        Path path = Paths.get(uploadPath, filename);
        System.out.println(path.toAbsolutePath().toString());

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
