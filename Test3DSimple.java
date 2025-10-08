import com.gnuplot.cli.GnuplotScript;
import com.gnuplot.cli.executor.GnuplotScriptExecutor;
import com.gnuplot.cli.parser.GnuplotScriptParser;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Test3DSimple {
    public static void main(String[] args) {
        try {
            // Read test script
            String scriptContent = Files.readString(Paths.get("test_3d_simple.dem"));

            System.out.println("=== Parsing 3D test script ===");
            GnuplotScriptParser parser = new GnuplotScriptParser();
            GnuplotScript script = parser.parse(scriptContent);

            System.out.println("Parsed " + script.getCommands().size() + " commands");

            System.out.println("\n=== Executing script ===");
            GnuplotScriptExecutor executor = new GnuplotScriptExecutor();
            executor.execute(script);

            System.out.println("\n=== Checking output ===");
            if (Files.exists(Paths.get("test_3d_output.svg"))) {
                long size = Files.size(Paths.get("test_3d_output.svg"));
                System.out.println("SUCCESS: SVG file created (" + size + " bytes)");

                // Show first few lines
                String content = Files.readString(Paths.get("test_3d_output.svg"));
                String[] lines = content.split("\n");
                System.out.println("\nFirst 10 lines of output:");
                for (int i = 0; i < Math.min(10, lines.length); i++) {
                    System.out.println(lines[i]);
                }
            } else {
                System.out.println("FAIL: No output file created");
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
