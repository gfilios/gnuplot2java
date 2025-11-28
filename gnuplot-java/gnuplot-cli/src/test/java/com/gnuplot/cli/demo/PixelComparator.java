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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compares SVG files at the pixel level by rasterizing them using Apache Batik.
 *
 * This is a SECONDARY comparison method used for visual inspection.
 * The PRIMARY comparison is done by SvgStructuralComparator.
 *
 * Main methods:
 * - compareWithAmplifiedDiff(): Pixel comparison with amplified difference visualization
 * - saveDiffImage(): Save the difference image to file
 */
public class PixelComparator {

    public PixelComparator() {
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
     * Extract SVG dimensions from viewBox or width/height attributes.
     * Returns default 800x600 if not found.
     */
    private Dimension extractSvgDimensions(String svgContent) {
        // Try viewBox first: viewBox="0 0 800 600"
        Pattern viewBoxPattern = Pattern.compile("viewBox\\s*=\\s*[\"']([^\"']+)[\"']");
        Matcher viewBoxMatcher = viewBoxPattern.matcher(svgContent);
        if (viewBoxMatcher.find()) {
            String[] parts = viewBoxMatcher.group(1).trim().split("\\s+");
            if (parts.length >= 4) {
                try {
                    int width = (int) Double.parseDouble(parts[2]);
                    int height = (int) Double.parseDouble(parts[3]);
                    return new Dimension(width, height);
                } catch (NumberFormatException e) {
                    // Fall through to try width/height
                }
            }
        }

        // Try width/height attributes
        Pattern widthPattern = Pattern.compile("width\\s*=\\s*[\"'](\\d+)");
        Pattern heightPattern = Pattern.compile("height\\s*=\\s*[\"'](\\d+)");
        Matcher widthMatcher = widthPattern.matcher(svgContent);
        Matcher heightMatcher = heightPattern.matcher(svgContent);

        int width = 800;
        int height = 600;

        if (widthMatcher.find()) {
            width = Integer.parseInt(widthMatcher.group(1));
        }
        if (heightMatcher.find()) {
            height = Integer.parseInt(heightMatcher.group(1));
        }

        return new Dimension(width, height);
    }

    /**
     * Rasterize SVG to BufferedImage using Apache Batik.
     * Extracts actual dimensions from SVG viewBox/attributes.
     */
    private BufferedImage rasterizeSvg(Path svgFile) throws IOException, TranscoderException {
        // Read SVG content
        String svgContent = Files.readString(svgFile);

        // Extract actual dimensions from SVG
        Dimension dims = extractSvgDimensions(svgContent);

        // Use Batik to transcode SVG to PNG
        PNGTranscoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) dims.width);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) dims.height);
        transcoder.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);

        TranscoderInput input = new TranscoderInput(new ByteArrayInputStream(svgContent.getBytes()));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(outputStream);

        transcoder.transcode(input, output);

        // Convert PNG bytes to BufferedImage
        byte[] pngBytes = outputStream.toByteArray();
        return ImageIO.read(new ByteArrayInputStream(pngBytes));
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
     * Save difference image to file.
     */
    public void saveDiffImage(PixelComparisonResult result, Path outputPath) throws IOException {
        if (result.getDiffImage() != null) {
            ImageIO.write(result.getDiffImage(), "PNG", outputPath.toFile());
        }
    }

    /**
     * Compare with amplified differences for better visibility.
     * Differences are shown in bright colors with 10x amplification.
     */
    public PixelComparisonResult compareWithAmplifiedDiff(Path svg1, Path svg2) throws IOException {
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

            // Create amplified difference image
            BufferedImage diffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int differentPixels = 0;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int rgb1 = img1.getRGB(x, y);
                    int rgb2 = img2.getRGB(x, y);

                    if (rgb1 == rgb2) {
                        // Same pixel - show as grayscale version of original
                        int r = (rgb1 >> 16) & 0xFF;
                        int g = (rgb1 >> 8) & 0xFF;
                        int b = rgb1 & 0xFF;
                        int gray = (r + g + b) / 3;
                        // Dim the background to make differences stand out
                        gray = gray / 2 + 128;
                        diffImage.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
                    } else {
                        // Different pixel - amplify the difference in red/magenta
                        int r1 = (rgb1 >> 16) & 0xFF;
                        int g1 = (rgb1 >> 8) & 0xFF;
                        int b1 = rgb1 & 0xFF;
                        int r2 = (rgb2 >> 16) & 0xFF;
                        int g2 = (rgb2 >> 8) & 0xFF;
                        int b2 = rgb2 & 0xFF;

                        // Amplify differences by 10x for visibility
                        int amplifiedR = Math.min(255, 128 + Math.abs(r1 - r2) * 10);
                        int amplifiedG = Math.min(255, Math.abs(g1 - g2) * 5);
                        int amplifiedB = Math.min(255, Math.abs(b1 - b2) * 5);

                        // Show differences in red/magenta for visibility
                        int diffColor = (amplifiedR << 16) | (amplifiedG << 8) | amplifiedB;
                        diffImage.setRGB(x, y, diffColor);
                        differentPixels++;
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
