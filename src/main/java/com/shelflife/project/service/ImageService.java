package com.shelflife.project.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import javax.imageio.ImageIO;

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
    private String uploadPath = "";

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

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/"))
            throw new InvalidMimeTypeException(file.getContentType(), "Invalid type");

        if (!Files.exists(dirPath))
            Files.createDirectories(dirPath);

        try {
            image = getImage(filename);
        } catch (ItemNotFoundException e) {
            image = new Image();
        }

        image.setFilename(filename);
        image.setMimetype(contentType);
        imageRepository.save(image);

        Files.write(path, file.getBytes());
    }

    public OptimizedImage loadOptimizedImage(String filename, String placeholderPath, int maxWidth, int maxHeight)
            throws IOException {
        Resource resource = loadImage(filename, placeholderPath);
        String mimeType = "image/svg+xml";

        if (imageExists(filename)) {
            try {
                mimeType = getImage(filename).getMimetype();
            } catch (ItemNotFoundException e) {
                Path path = Paths.get(uploadPath, filename);
                String detected = Files.probeContentType(path);
                if (detected != null)
                    mimeType = detected;
            }
        }

        byte[] bytes = { 0 };
        try (InputStream is = resource.getInputStream()) {
            bytes = is.readAllBytes();
        }

        if (isSvg(mimeType))
            return new OptimizedImage(bytes, mimeType);

        BufferedImage sourceImage = ImageIO.read(new ByteArrayInputStream(bytes));
        if (sourceImage == null)
            return new OptimizedImage(bytes, mimeType);

        BufferedImage resized = resizeToFit(sourceImage, maxWidth, maxHeight);
        byte[] optimizedBytes = writeImage(resized, mimeType);

        return new OptimizedImage(optimizedBytes, mimeType);
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

        Image image = getImage(filename);
        imageRepository.delete(image);
    }

    public record OptimizedImage(byte[] bytes, String mimeType) {
    }

    private boolean isSvg(String mimeType) {
        return mimeType != null && mimeType.toLowerCase().contains("svg");
    }

    private BufferedImage resizeToFit(BufferedImage source, int maxWidth, int maxHeight) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight)
            return source;

        double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        int targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));
        int targetHeight = Math.max(1, (int) Math.round(originalHeight * scale));

        int imageType = source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return resized;
    }

    private byte[] writeImage(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        format = format.split("/")[1];

        ImageIO.write(image, format, out);
        return out.toByteArray();
    }
}