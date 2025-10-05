package com.gnuplot.cli.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates HTML reports for demo test results with side-by-side comparison.
 */
public class HtmlReportGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Generate an HTML report for a test run.
     */
    public static void generateReport(Path outputFile, List<TestResultRepository.DemoTestRecord> records,
                                     Path runDirectory) throws IOException {

        StringBuilder html = new StringBuilder();

        // HTML header
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>Gnuplot Demo Test Results</title>\n");
        html.append("  <style>\n");
        appendStyles(html);
        html.append("  </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Header section
        html.append("  <div class=\"header\">\n");
        html.append("    <h1>🧪 Gnuplot Demo Test Results</h1>\n");
        html.append("    <p class=\"timestamp\">Test Run: ").append(runDirectory.getFileName()).append("</p>\n");
        html.append("  </div>\n");

        // Summary section
        appendSummary(html, records);

        // Individual test results
        html.append("  <div class=\"container\">\n");
        html.append("    <h2>Test Results</h2>\n");

        for (TestResultRepository.DemoTestRecord record : records) {
            appendTestResult(html, record, runDirectory);
        }

        html.append("  </div>\n");

        // Lightbox overlay
        html.append("  <div class=\"lightbox\" id=\"lightbox\" onclick=\"closeLightbox()\">\n");
        html.append("    <img id=\"lightbox-img\" src=\"\" alt=\"\">\n");
        html.append("  </div>\n");

        // JavaScript for collapsible sections and lightbox
        appendJavaScript(html);

        html.append("</body>\n");
        html.append("</html>\n");

        Files.writeString(outputFile, html.toString());
    }

    private static void appendStyles(StringBuilder html) {
        html.append("    * { margin: 0; padding: 0; box-sizing: border-box; }\n");
        html.append("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; ");
        html.append("           background: #f5f5f5; color: #333; line-height: 1.6; }\n");
        html.append("    .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); ");
        html.append("              color: white; padding: 2rem; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("    .header h1 { font-size: 2.5rem; margin-bottom: 0.5rem; }\n");
        html.append("    .timestamp { opacity: 0.9; font-size: 0.9rem; }\n");
        html.append("    .summary { background: white; margin: 2rem auto; max-width: 1200px; ");
        html.append("               padding: 2rem; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        html.append("    .summary h2 { color: #667eea; margin-bottom: 1rem; }\n");
        html.append("    .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1rem; }\n");
        html.append("    .stat-card { background: #f8f9fa; padding: 1rem; border-radius: 6px; text-align: center; }\n");
        html.append("    .stat-card .number { font-size: 2rem; font-weight: bold; color: #667eea; }\n");
        html.append("    .stat-card .label { color: #666; font-size: 0.9rem; margin-top: 0.5rem; }\n");
        html.append("    .container { max-width: 1200px; margin: 2rem auto; padding: 0 1rem; }\n");
        html.append("    .test-result { background: white; margin-bottom: 1.5rem; border-radius: 8px; ");
        html.append("                   box-shadow: 0 2px 5px rgba(0,0,0,0.1); overflow: hidden; }\n");
        html.append("    .test-header { padding: 1rem 1.5rem; cursor: pointer; display: flex; ");
        html.append("                   justify-content: space-between; align-items: center; ");
        html.append("                   transition: background 0.2s; }\n");
        html.append("    .test-header:hover { background: #f8f9fa; }\n");
        html.append("    .test-header.passing { border-left: 4px solid #28a745; }\n");
        html.append("    .test-header.failing { border-left: 4px solid #dc3545; }\n");
        html.append("    .test-title { font-weight: bold; font-size: 1.1rem; }\n");
        html.append("    .status-badge { padding: 0.25rem 0.75rem; border-radius: 20px; ");
        html.append("                    font-size: 0.85rem; font-weight: bold; }\n");
        html.append("    .status-badge.pass { background: #d4edda; color: #155724; }\n");
        html.append("    .status-badge.fail { background: #f8d7da; color: #721c24; }\n");
        html.append("    .test-details { display: none; padding: 1.5rem; background: #fafafa; ");
        html.append("                    border-top: 1px solid #e0e0e0; }\n");
        html.append("    .test-details.expanded { display: block; }\n");
        html.append("    .comparison { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-top: 1rem; ");
        html.append("                  align-items: start; }\n");
        html.append("    .comparison-grid { display: grid; grid-template-columns: 1fr 1fr 1.5fr; gap: 1.5rem; ");
        html.append("                       align-items: start; }\n");
        html.append("    .comparison-text { background: #f8f9fa; padding: 1rem; border-radius: 6px; ");
        html.append("                       max-height: 600px; overflow-y: auto; }\n");
        html.append("    .comparison-text h4 { margin-bottom: 0.75rem; color: #667eea; }\n");
        html.append("    .comparison-content { background: #2d2d2d; color: #f8f8f2; padding: 1rem; ");
        html.append("                          border-radius: 4px; font-family: 'Courier New', monospace; ");
        html.append("                          font-size: 0.75rem; max-height: 500px; overflow-y: auto; }\n");
        html.append("    .comparison-content pre { margin: 0; white-space: pre-wrap; word-wrap: break-word; }\n");
        html.append("    .implementation { background: white; padding: 1rem; border-radius: 6px; ");
        html.append("                      min-width: 0; }\n");
        html.append("    .implementation h4 { margin-bottom: 0.75rem; color: #667eea; }\n");
        html.append("    .svg-output { border: 1px solid #ddd; border-radius: 4px; padding: 0.5rem; ");
        html.append("                  background: white; width: 100%; overflow: visible; ");
        html.append("                  display: flex; justify-content: center; align-items: center; }\n");
        html.append("    .svg-output img { max-width: 100%; width: 100%; height: auto; display: block; }\n");
        html.append("    .log-section { margin-top: 1rem; }\n");
        html.append("    .log-section h5 { font-size: 0.9rem; color: #666; margin-bottom: 0.5rem; }\n");
        html.append("    .log-content { background: #2d2d2d; color: #f8f8f2; padding: 1rem; ");
        html.append("                   border-radius: 4px; font-family: 'Courier New', monospace; ");
        html.append("                   font-size: 0.85rem; overflow-x: auto; max-height: 300px; overflow-y: auto; }\n");
        html.append("    .error-line { color: #ff6b6b; }\n");
        html.append("    .warning-line { color: #ffd93d; }\n");
        html.append("    .success-line { color: #6bcf7f; }\n");
        html.append("    .script-section { margin-top: 1rem; }\n");
        html.append("    .script-section h4 { transition: background 0.2s; padding: 0.5rem; ");
        html.append("                         border-radius: 4px; margin: 0; }\n");
        html.append("    .script-section h4:hover { background: #f0f0f0; }\n");
        html.append("    .script-content { background: #f5f5f5; padding: 0; border-radius: 4px; ");
        html.append("                      overflow-x: auto; max-height: 400px; overflow-y: auto; ");
        html.append("                      margin-top: 0.5rem; }\n");
        html.append("    .script-content pre { margin: 0; padding: 1rem; ");
        html.append("                          font-family: 'Courier New', monospace; font-size: 0.85rem; ");
        html.append("                          white-space: pre-wrap; word-wrap: break-word; }\n");
        html.append("    .toggle-icon { transition: transform 0.3s; }\n");
        html.append("    .toggle-icon.expanded { transform: rotate(90deg); }\n");
        // Lightbox styles
        html.append("    .lightbox { display: none; position: fixed; z-index: 9999; left: 0; top: 0; ");
        html.append("                width: 100%; height: 100%; background-color: rgba(0,0,0,0.9); ");
        html.append("                justify-content: center; align-items: center; cursor: pointer; }\n");
        html.append("    .lightbox.active { display: flex; }\n");
        html.append("    .lightbox img { max-width: 95%; max-height: 95%; object-fit: contain; ");
        html.append("                    box-shadow: 0 0 30px rgba(255,255,255,0.3); }\n");
        html.append("    .svg-output img { cursor: zoom-in; }\n");
    }

    private static void appendSummary(StringBuilder html, List<TestResultRepository.DemoTestRecord> records) {
        long passing = records.stream().filter(TestResultRepository.DemoTestRecord::isPassing).count();
        long cSucceeded = records.stream().filter(TestResultRepository.DemoTestRecord::isCSuccess).count();
        long javaSucceeded = records.stream().filter(TestResultRepository.DemoTestRecord::isJavaSuccess).count();
        int total = records.size();

        double passRate = total > 0 ? (100.0 * passing / total) : 0;

        html.append("  <div class=\"summary\">\n");
        html.append("    <h2>📊 Summary</h2>\n");
        html.append("    <div class=\"stats\">\n");

        html.append("      <div class=\"stat-card\">\n");
        html.append("        <div class=\"number\">").append(total).append("</div>\n");
        html.append("        <div class=\"label\">Total Tests</div>\n");
        html.append("      </div>\n");

        html.append("      <div class=\"stat-card\">\n");
        html.append("        <div class=\"number\" style=\"color: #28a745;\">").append(passing).append("</div>\n");
        html.append("        <div class=\"label\">Passing (")
            .append(String.format("%.1f%%", passRate)).append(")</div>\n");
        html.append("      </div>\n");

        html.append("      <div class=\"stat-card\">\n");
        html.append("        <div class=\"number\" style=\"color: #17a2b8;\">").append(cSucceeded).append("</div>\n");
        html.append("        <div class=\"label\">C Gnuplot Success</div>\n");
        html.append("      </div>\n");

        html.append("      <div class=\"stat-card\">\n");
        html.append("        <div class=\"number\" style=\"color: #fd7e14;\">").append(javaSucceeded).append("</div>\n");
        html.append("        <div class=\"label\">Java Gnuplot Success</div>\n");
        html.append("      </div>\n");

        html.append("    </div>\n");
        html.append("  </div>\n");
    }

    private static void appendTestResult(StringBuilder html, TestResultRepository.DemoTestRecord record,
                                         Path runDirectory) throws IOException {
        String statusClass = record.isPassing() ? "passing" : "failing";
        String statusBadge = record.isPassing() ? "pass" : "fail";
        String statusText = record.isPassing() ? "✅ PASS" : "❌ FAIL";

        html.append("    <div class=\"test-result\">\n");
        html.append("      <div class=\"test-header ").append(statusClass).append("\" ");
        html.append("onclick=\"toggleDetails('").append(record.getDemoName().replace(".dem", "")).append("')\">\n");
        html.append("        <div>\n");
        html.append("          <span class=\"toggle-icon\" id=\"icon-")
            .append(record.getDemoName().replace(".dem", "")).append("\">▶</span> ");
        html.append("          <span class=\"test-title\">").append(record.getDemoName()).append("</span>\n");
        html.append("          <small style=\"color: #666; margin-left: 1rem;\">C: ")
            .append(record.isCSuccess() ? "✓" : "✗")
            .append(" | Java: ")
            .append(record.isJavaSuccess() ? "✓" : "✗")
            .append("</small>\n");
        html.append("        </div>\n");
        html.append("        <span class=\"status-badge ").append(statusBadge).append("\">")
            .append(statusText).append("</span>\n");
        html.append("      </div>\n");

        // Details section
        html.append("      <div class=\"test-details\" id=\"details-")
            .append(record.getDemoName().replace(".dem", "")).append("\">\n");

        // Script content (collapsible)
        if (record.getOriginalScript() != null && Files.exists(record.getOriginalScript())) {
            String demoId = record.getDemoName().replace(".dem", "");
            html.append("        <div class=\"script-section\">\n");
            html.append("          <h4 onclick=\"toggleScript('").append(demoId).append("')\" ");
            html.append("style=\"cursor: pointer; user-select: none;\">");
            html.append("<span class=\"toggle-icon\" id=\"script-icon-").append(demoId).append("\">▶</span> ");
            html.append("📝 Original Script</h4>\n");
            html.append("          <div class=\"script-content\" id=\"script-").append(demoId).append("\" ");
            html.append("style=\"display: none;\">");
            html.append("<pre>").append(escapeHtml(Files.readString(record.getOriginalScript()))).append("</pre>");
            html.append("</div>\n");
            html.append("        </div>\n");
        }

        // SVG Comparison with side-by-side layout
        html.append("        <h4 style=\"margin-top: 1.5rem;\">🖼️ Output Comparison</h4>\n");

        // Plot 1 comparison
        String baseName = record.getDemoName().replace(".dem", "");
        Path comparison1 = runDirectory.resolve("comparison_" + record.getDemoName() + ".txt");
        appendPlotComparison(html, record, runDirectory, 1, comparison1);

        // Additional plot comparisons (plot2, plot3, etc.)
        if (record.getCSvgOutput() != null) {
            String csvBaseName = record.getCSvgOutput().getFileName().toString().replace(".svg", "");
            Path outputDir = record.getCSvgOutput().getParent();
            for (int i = 2; i <= 100; i++) {
                Path cNumberedFile = outputDir.resolve(String.format("%s_%03d.svg", csvBaseName, i));
                Path comparisonN = runDirectory.resolve(String.format("comparison_%s_plot%d.txt",
                                                                      record.getDemoName(), i));
                if (Files.exists(cNumberedFile)) {
                    appendPlotComparison(html, record, runDirectory, i, comparisonN);
                } else {
                    break;
                }
            }
        }

        html.append("      </div>\n");
        html.append("    </div>\n");
    }

    private static void appendPlotComparison(StringBuilder html, TestResultRepository.DemoTestRecord record,
                                             Path runDirectory, int plotNumber, Path comparisonFile) throws IOException {
        String baseName = record.getDemoName().replace(".dem", "");
        String comparisonId = baseName + "_comparison_plot" + plotNumber;

        html.append("        <div class=\"plot-comparison-row\" style=\"margin-top: 1.5rem;\">\n");
        html.append("          <h5 style=\"color: #667eea;\">Plot ").append(plotNumber).append("</h5>\n");

        // Images side-by-side (2 columns)
        html.append("          <div class=\"comparison\">\n");

        // C Gnuplot output for this plot
        html.append("            <div class=\"implementation\">\n");
        html.append("              <h4>C Gnuplot</h4>\n");
        Path cSvgFile;
        if (plotNumber == 1) {
            cSvgFile = record.getCSvgOutput();
        } else {
            String csvBaseName = record.getCSvgOutput().getFileName().toString().replace(".svg", "");
            Path outputDir = record.getCSvgOutput().getParent();
            cSvgFile = outputDir.resolve(String.format("%s_%03d.svg", csvBaseName, plotNumber));
        }

        if (cSvgFile != null && Files.exists(cSvgFile)) {
            Path relativePath = runDirectory.relativize(cSvgFile);
            html.append("              <div class=\"svg-output\">\n");
            html.append("                <img src=\"").append(relativePath).append("\" alt=\"C Gnuplot plot ").append(plotNumber).append("\">\n");
            html.append("              </div>\n");
            html.append("              <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Size: ")
                .append(Files.size(cSvgFile)).append(" bytes</p>\n");
        } else {
            html.append("              <p style=\"color: #dc3545;\">No output generated</p>\n");
        }
        html.append("            </div>\n");

        // Java Gnuplot output for this plot
        html.append("            <div class=\"implementation\">\n");
        html.append("              <h4>Java Gnuplot</h4>\n");
        Path javaSvgFile = null;

        // Check if Java output exists before accessing its properties
        if (record.getJavaSvgOutput() != null) {
            if (plotNumber == 1) {
                javaSvgFile = record.getJavaSvgOutput();
            } else {
                String javaBaseName = record.getJavaSvgOutput().getFileName().toString().replace(".svg", "");
                Path outputDir = record.getJavaSvgOutput().getParent();
                javaSvgFile = outputDir.resolve(String.format("%s_%03d.svg", javaBaseName, plotNumber));
            }
        }

        if (javaSvgFile != null && Files.exists(javaSvgFile)) {
            Path relativePath = runDirectory.relativize(javaSvgFile);
            html.append("              <div class=\"svg-output\">\n");
            html.append("                <img src=\"").append(relativePath).append("\" alt=\"Java Gnuplot plot ").append(plotNumber).append("\">\n");
            html.append("              </div>\n");
            html.append("              <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Size: ")
                .append(Files.size(javaSvgFile)).append(" bytes</p>\n");
        } else {
            html.append("              <p style=\"color: #dc3545;\">No output generated</p>\n");
        }
        html.append("            </div>\n");

        html.append("          </div>\n");

        // Comparison text below images (collapsible)
        if (Files.exists(comparisonFile)) {
            html.append("          <div class=\"script-section\" style=\"margin-top: 1rem;\">\n");
            html.append("            <h4 onclick=\"toggleScript('").append(comparisonId).append("')\" ");
            html.append("style=\"cursor: pointer; user-select: none;\">");
            html.append("<span class=\"toggle-icon\" id=\"script-icon-").append(comparisonId).append("\">▶</span> ");
            html.append("🔍 Comparison Analysis</h4>\n");
            html.append("            <div class=\"script-content\" id=\"script-").append(comparisonId).append("\" ");
            html.append("style=\"display: none;\">");
            html.append("<pre>").append(escapeHtml(Files.readString(comparisonFile))).append("</pre>");
            html.append("</div>\n");
            html.append("          </div>\n");
        }

        html.append("        </div>\n");
    }

    private static void appendJavaScript(StringBuilder html) {
        html.append("  <script>\n");
        html.append("    function toggleDetails(id) {\n");
        html.append("      const details = document.getElementById('details-' + id);\n");
        html.append("      const icon = document.getElementById('icon-' + id);\n");
        html.append("      details.classList.toggle('expanded');\n");
        html.append("      icon.classList.toggle('expanded');\n");
        html.append("    }\n");
        html.append("    function toggleScript(id) {\n");
        html.append("      const script = document.getElementById('script-' + id);\n");
        html.append("      const icon = document.getElementById('script-icon-' + id);\n");
        html.append("      if (script.style.display === 'none') {\n");
        html.append("        script.style.display = 'block';\n");
        html.append("        icon.classList.add('expanded');\n");
        html.append("      } else {\n");
        html.append("        script.style.display = 'none';\n");
        html.append("        icon.classList.remove('expanded');\n");
        html.append("      }\n");
        html.append("    }\n");
        // Lightbox functions
        html.append("    function openLightbox(imgSrc) {\n");
        html.append("      const lightbox = document.getElementById('lightbox');\n");
        html.append("      const lightboxImg = document.getElementById('lightbox-img');\n");
        html.append("      lightboxImg.src = imgSrc;\n");
        html.append("      lightbox.classList.add('active');\n");
        html.append("    }\n");
        html.append("    function closeLightbox() {\n");
        html.append("      const lightbox = document.getElementById('lightbox');\n");
        html.append("      lightbox.classList.remove('active');\n");
        html.append("    }\n");
        // Add click handlers to all SVG images
        html.append("    document.addEventListener('DOMContentLoaded', function() {\n");
        html.append("      const images = document.querySelectorAll('.svg-output img');\n");
        html.append("      images.forEach(img => {\n");
        html.append("        img.addEventListener('click', function(e) {\n");
        html.append("          e.stopPropagation();\n");
        html.append("          openLightbox(this.src);\n");
        html.append("        });\n");
        html.append("      });\n");
        html.append("    });\n");
        html.append("  </script>\n");
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static String formatLogContent(String log) {
        StringBuilder formatted = new StringBuilder();
        String[] lines = log.split("\n");

        for (String line : lines) {
            String escapedLine = escapeHtml(line);
            String lowerLine = line.toLowerCase();

            if (lowerLine.contains("error") || lowerLine.contains("fail")) {
                formatted.append("<span class=\"error-line\">").append(escapedLine).append("</span>\n");
            } else if (lowerLine.contains("warning") || lowerLine.contains("warn")) {
                formatted.append("<span class=\"warning-line\">").append(escapedLine).append("</span>\n");
            } else if (lowerLine.contains("success") || lowerLine.contains("pass")) {
                formatted.append("<span class=\"success-line\">").append(escapedLine).append("</span>\n");
            } else {
                formatted.append(escapedLine).append("\n");
            }
        }

        return formatted.toString();
    }
}
