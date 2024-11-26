package dev.thural.quietspace.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Slf4j
@Component
public class ImageCompressionUtil {
    private static final int BUFFER_SIZE = 4 * 1024;
    private static final int DEFAULT_TARGET_SIZE = 100 * 1024; // 100KB

    /**
     * Compress image using Deflater (ZIP-style compression)
     *
     * @param data Original image bytes
     * @return Compressed image bytes
     */
    public byte[] compressImageWithDeflater(byte[] data) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            deflater.setInput(data);
            deflater.finish();

            byte[] tmp = new byte[BUFFER_SIZE];
            while (!deflater.finished()) {
                int size = deflater.deflate(tmp);
                outputStream.write(tmp, 0, size);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error compressing image with Deflater", e);
            throw new RuntimeException("Error compressing image", e);
        } finally {
            deflater.end();
        }
    }

    /**
     * Decompress image using Inflater (ZIP-style decompression)
     *
     * @param data Compressed image bytes
     * @return Decompressed image bytes
     */
    public byte[] decompressImageWithDeflater(byte[] data) {
        Inflater inflater = new Inflater();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            inflater.setInput(data);

            byte[] tmp = new byte[BUFFER_SIZE];
            while (!inflater.finished()) {
                int count = inflater.inflate(tmp);
                outputStream.write(tmp, 0, count);
            }

            return outputStream.toByteArray();
        } catch (DataFormatException | IOException e) {
            log.error("Error decompressing image with Inflater", e);
            throw new RuntimeException("Error decompressing image", e);
        } finally {
            inflater.end();
        }
    }

    /**
     * Compress image using Thumbnails library
     * Dynamically reduces image quality to meet target size
     *
     * @param inputStream     Original image input stream
     * @param targetSizeBytes Target size in bytes
     * @return Compressed image bytes
     * @throws IOException If compression fails
     */
    public byte[] compressImage(InputStream inputStream, int targetSizeBytes) throws IOException {
        // Use default target size if not specified
        if (targetSizeBytes <= 0) {
            targetSizeBytes = DEFAULT_TARGET_SIZE;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] compressedImage;
        double quality = 1.0;

        do {
            outputStream.reset();
            Thumbnails.of(inputStream)
                    .scale(1.0)
                    .outputQuality(quality)
                    .toOutputStream(outputStream);

            compressedImage = outputStream.toByteArray();

            // Reset input stream for next iteration
            inputStream = new ByteArrayInputStream(compressedImage);

            // Reduce quality incrementally
            quality -= 0.1;
        } while (compressedImage.length > targetSizeBytes && quality > 0);

        log.info("Image compressed to {} bytes with quality {}",
                compressedImage.length, quality + 0.1);

        return compressedImage;
    }

    /**
     * Compress image from byte array using Thumbnails library
     *
     * @param imageBytes      Original image bytes
     * @param targetSizeBytes Target size in bytes
     * @return Compressed image bytes
     * @throws IOException If compression fails
     */
    public byte[] compressImage(byte[] imageBytes, int targetSizeBytes) throws IOException {
        return compressImage(new ByteArrayInputStream(imageBytes), targetSizeBytes);
    }

    /**
     * Decompress image (pass-through for Thumbnails compression)
     *
     * @param compressedImage Compressed image bytes
     * @return Decompressed image bytes
     */
    public byte[] decompressImage(byte[] compressedImage) {
        return compressedImage;
    }

    /**
     * Choose compression method based on image size and requirements
     *
     * @param imageBytes      Original image bytes
     * @param compressionType Compression type (DEFLATE or THUMBNAILS)
     * @return Compressed image bytes
     * @throws IOException If compression fails
     */
    public byte[] compressImage(byte[] imageBytes, CompressionType compressionType) throws IOException {
        switch (compressionType) {
            case DEFLATE:
                return compressImageWithDeflater(imageBytes);
            case THUMBNAILS:
            default:
                return compressImage(imageBytes, DEFAULT_TARGET_SIZE);
        }
    }

    /**
     * Compression type enum
     */
    public enum CompressionType {
        DEFLATE,  // ZIP-style compression
        THUMBNAILS  // Image quality reduction
    }
}