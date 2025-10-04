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

        // JavaScript for collapsible sections
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
        html.append("    .script-content { background: #f5f5f5; padding: 1rem; border-radius: 4px; ");
        html.append("                      font-family: 'Courier New', monospace; font-size: 0.85rem; ");
        html.append("                      overflow-x: auto; max-height: 400px; overflow-y: auto; }\n");
        html.append("    .toggle-icon { transition: transform 0.3s; }\n");
        html.append("    .toggle-icon.expanded { transform: rotate(90deg); }\n");
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

        // Script content
        if (record.getOriginalScript() != null && Files.exists(record.getOriginalScript())) {
            html.append("        <div class=\"script-section\">\n");
            html.append("          <h4>📝 Original Script</h4>\n");
            html.append("          <div class=\"script-content\">");
            html.append(escapeHtml(Files.readString(record.getOriginalScript())));
            html.append("</div>\n");
            html.append("        </div>\n");
        }

        // SVG Comparison
        html.append("        <h4 style=\"margin-top: 1.5rem;\">🖼️ Output Comparison</h4>\n");
        html.append("        <div class=\"comparison\">\n");

        // C Gnuplot output
        html.append("          <div class=\"implementation\">\n");
        html.append("            <h4>C Gnuplot</h4>\n");
        if (record.getCSvgOutput() != null && Files.exists(record.getCSvgOutput())) {
            // Show main output
            Path relativePath = runDirectory.relativize(record.getCSvgOutput());
            html.append("            <div class=\"svg-output\">\n");
            html.append("              <img src=\"").append(relativePath).append("\" alt=\"C Gnuplot output 1\">\n");
            html.append("            </div>\n");
            html.append("            <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Plot 1 - Size: ")
                .append(Files.size(record.getCSvgOutput())).append(" bytes</p>\n");

            // Look for numbered files (_002.svg, _003.svg, etc.)
            String baseName = record.getCSvgOutput().getFileName().toString().replace(".svg", "");
            Path outputDir = record.getCSvgOutput().getParent();
            for (int i = 2; i <= 100; i++) {
                Path numberedFile = outputDir.resolve(String.format("%s_%03d.svg", baseName, i));
                if (Files.exists(numberedFile)) {
                    Path numberedRelativePath = runDirectory.relativize(numberedFile);
                    html.append("            <div class=\"svg-output\" style=\"margin-top: 1rem;\">\n");
                    html.append("              <img src=\"").append(numberedRelativePath)
                        .append("\" alt=\"C Gnuplot output ").append(i).append("\">\n");
                    html.append("            </div>\n");
                    html.append("            <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Plot ")
                        .append(i).append(" - Size: ").append(Files.size(numberedFile)).append(" bytes</p>\n");
                } else {
                    break; // Stop when we find a gap
                }
            }
        } else {
            html.append("            <p style=\"color: #dc3545;\">No output generated</p>\n");
        }

        // C stderr
        if (record.getCStderr() != null && Files.exists(record.getCStderr())) {
            String stderr = Files.readString(record.getCStderr());
            if (!stderr.trim().isEmpty()) {
                html.append("            <div class=\"log-section\">\n");
                html.append("              <h5>Errors/Warnings:</h5>\n");
                html.append("              <div class=\"log-content\">");
                html.append(formatLogContent(stderr));
                html.append("</div>\n");
                html.append("            </div>\n");
            }
        }

        html.append("          </div>\n");

        // Java Gnuplot output
        html.append("          <div class=\"implementation\">\n");
        html.append("            <h4>Java Gnuplot</h4>\n");
        if (record.getJavaSvgOutput() != null && Files.exists(record.getJavaSvgOutput())) {
            // Show main output
            Path relativePath = runDirectory.relativize(record.getJavaSvgOutput());
            html.append("            <div class=\"svg-output\">\n");
            html.append("              <img src=\"").append(relativePath).append("\" alt=\"Java Gnuplot output 1\">\n");
            html.append("            </div>\n");
            html.append("            <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Plot 1 - Size: ")
                .append(Files.size(record.getJavaSvgOutput())).append(" bytes</p>\n");

            // Look for numbered files (_002.svg, _003.svg, etc.)
            String baseName = record.getJavaSvgOutput().getFileName().toString().replace(".svg", "");
            Path outputDir = record.getJavaSvgOutput().getParent();
            for (int i = 2; i <= 100; i++) {
                Path numberedFile = outputDir.resolve(String.format("%s_%03d.svg", baseName, i));
                if (Files.exists(numberedFile)) {
                    Path numberedRelativePath = runDirectory.relativize(numberedFile);
                    html.append("            <div class=\"svg-output\" style=\"margin-top: 1rem;\">\n");
                    html.append("              <img src=\"").append(numberedRelativePath)
                        .append("\" alt=\"Java Gnuplot output ").append(i).append("\">\n");
                    html.append("            </div>\n");
                    html.append("            <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #666;\">Plot ")
                        .append(i).append(" - Size: ").append(Files.size(numberedFile)).append(" bytes</p>\n");
                } else {
                    break; // Stop when we find a gap
                }
            }
        } else {
            html.append("            <p style=\"color: #dc3545;\">No output generated</p>\n");
        }

        // Java stderr
        if (record.getJavaStderr() != null && Files.exists(record.getJavaStderr())) {
            String stderr = Files.readString(record.getJavaStderr());
            if (!stderr.trim().isEmpty()) {
                html.append("            <div class=\"log-section\">\n");
                html.append("              <h5>Errors/Warnings:</h5>\n");
                html.append("              <div class=\"log-content\">");
                html.append(formatLogContent(stderr));
                html.append("</div>\n");
                html.append("            </div>\n");
            }
        }

        html.append("          </div>\n");
        html.append("        </div>\n");

        html.append("      </div>\n");
        html.append("    </div>\n");
    }

    private static void appendJavaScript(StringBuilder html) {
        html.append("  <script>\n");
        html.append("    function toggleDetails(id) {\n");
        html.append("      const details = document.getElementById('details-' + id);\n");
        html.append("      const icon = document.getElementById('icon-' + id);\n");
        html.append("      details.classList.toggle('expanded');\n");
        html.append("      icon.classList.toggle('expanded');\n");
        html.append("    }\n");
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
