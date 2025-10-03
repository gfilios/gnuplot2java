package com.gnuplot.cli.demo;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compares SVG files at the pixel level by rasterizing them.
 */
public class PixelComparator {

    private final double threshold;

    public PixelComparator(double threshold) {
        this.threshold = threshold;
    }

    public PixelComparator() {
        this(0.95); // 95% similarity threshold by default
    }

    /**
     * Result of pixel comparison.
     */
    public static class PixelComparisonResult {
        private final double similarity;
        private final int totalPixels;
        private final int differentPixels;
        private final BufferedImage diffImage;

        public PixelComparisonResult(double similarity, int totalPixels, int differentPixels,
                                    BufferedImage diffImage) {
            this.similarity = similarity;
            this.totalPixels = totalPixels;
            this.differentPixels = differentPixels;
            this.diffImage = diffImage;
        }

        public double getSimilarity() { return similarity; }
        public int getTotalPixels() { return totalPixels; }
        public int getDifferentPixels() { return differentPixels; }
        public BufferedImage getDiffImage() { return diffImage; }

        public boolean isAcceptable(double threshold) {
            return similarity >= threshold;
        }

        public String getSummary() {
            return String.format("%.2f%% similar (%d/%d pixels differ)",
                similarity * 100, differentPixels, totalPixels);
        }
    }

    /**
     * Compare two SVG files by rasterizing and comparing pixels.
     */
    public PixelComparisonResult compare(Path svg1, Path svg2) throws IOException {
        if (!Files.exists(svg1) || !Files.exists(svg2)) {
            return new PixelComparisonResult(0.0, 0, 0, null);
        }

        try {
            // Rasterize both SVGs
            BufferedImage img1 = rasterizeSvg(svg1);
            BufferedImage img2 = rasterizeSvg(svg2);

            // Ensure same dimensions (resize if needed)
            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                img2 = resizeImage(img2, img1.getWidth(), img1.getHeight());
            }

            return comparePixels(img1, img2);

        } catch (TranscoderException e) {
            throw new IOException("Error rasterizing SVG: " + e.getMessage(), e);
        }
    }

    /**
     * Rasterize SVG to BufferedImage using Apache Batik.
     * Note: Batik dependency needs to be added to pom.xml
     */
    private BufferedImage rasterizeSvg(Path svgFile) throws IOException, TranscoderException {
        // Read SVG content
        String svgContent = Files.readString(svgFile);

        // Use Batik to transcode SVG to PNG
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 800f);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 600f);

        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgContent.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);

        transcoder.transcode(input, output);

        // Convert PNG bytes to BufferedImage
        byte[] pngBytes = outputStream.toByteArray();
        return ImageIO.read(new ByteArrayInputStream(pngBytes));
    }

    /**
     * Simple fallback: render SVG as white rectangle (when Batik not available).
     * TODO: Remove this once Batik is added to dependencies
     */
    private BufferedImage renderSvgFallback(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return img;
    }

    /**
     * Resize image to target dimensions.
     */
    private BufferedImage resizeImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    /**
     * Compare two images pixel by pixel.
     */
    private PixelComparisonResult comparePixels(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int totalPixels = width * height;

        // Create difference image (red pixels where different)
        BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int differentPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int rgb2 = img2.getRGB(x, y);

                if (rgb1 == rgb2) {
                    // Same pixel - copy from original
                    diffImage.setRGB(x, y, rgb1);
                } else {
                    // Different pixel - mark as red
                    diffImage.setRGB(x, y, Color.RED.getRGB());
                    differentPixels++;
                }
            }
        }

        double similarity = 1.0 - ((double) differentPixels / totalPixels);

        return new PixelComparisonResult(similarity, totalPixels, differentPixels, diffImage);
    }

    /**
     * Save difference image to file.
     */
    public void saveDiffImage(PixelComparisonResult result, Path outputPath) throws IOException {
        if (result.getDiffImage() != null) {
            ImageIO.write(result.getDiffImage(), "PNG", outputPath.toFile());
        }
    }

    /**
     * Calculate perceptual difference using weighted RGB distance.
     */
    private double calculateColorDistance(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >> 8) & 0xFF;
        int b1 = rgb1 & 0xFF;

        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >> 8) & 0xFF;
        int b2 = rgb2 & 0xFF;

        // Weighted Euclidean distance (human eye is more sensitive to green)
        double dr = (r1 - r2) * 0.30;
        double dg = (g1 - g2) * 0.59;
        double db = (b1 - b2) * 0.11;

        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /**
     * Compare with tolerance for anti-aliasing differences.
     */
    public PixelComparisonResult compareWithTolerance(Path svg1, Path svg2, double colorTolerance)
            throws IOException {
        if (!Files.exists(svg1) || !Files.exists(svg2)) {
            return new PixelComparisonResult(0.0, 0, 0, null);
        }

        try {
            BufferedImage img1 = rasterizeSvg(svg1);
            BufferedImage img2 = rasterizeSvg(svg2);

            if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
                img2 = resizeImage(img2, img1.getWidth(), img1.getHeight());
            }

            int width = img1.getWidth();
            int height = img1.getHeight();
            int totalPixels = width * height;

            BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int differentPixels = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb1 = img1.getRGB(x, y);
                    int rgb2 = img2.getRGB(x, y);

                    double distance = calculateColorDistance(rgb1, rgb2);

                    if (distance > colorTolerance) {
                        diffImage.setRGB(x, y, Color.RED.getRGB());
                        differentPixels++;
                    } else {
                        diffImage.setRGB(x, y, rgb1);
                    }
                }
            }

            double similarity = 1.0 - ((double) differentPixels / totalPixels);

            return new PixelComparisonResult(similarity, totalPixels, differentPixels, diffImage);

        } catch (TranscoderException e) {
            throw new IOException("Error rasterizing SVG: " + e.getMessage(), e);
        }
    }
}
