package com.gnuplot.core.data;

import com.gnuplot.core.data.csv.CsvConfig;
import com.gnuplot.core.data.csv.CsvDataSource;
import com.gnuplot.core.data.json.JsonConfig;
import com.gnuplot.core.data.json.JsonDataSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating DataSource instances based on file type or format.
 * Provides automatic format detection and supports custom data source registration.
 *
 * <p>Example usage:
 * <pre>{@code
 * // Automatic format detection
 * try (DataSource ds = DataSourceFactory.create(Path.of("data.csv"))) {
 *     for (DataRecord record : ds) {
 *         // process data
 *     }
 * }
 *
 * // Explicit format with configuration
 * CsvConfig config = CsvConfig.builder()
 *     .delimiter('\t')
 *     .hasHeader(true)
 *     .build();
 * try (DataSource ds = DataSourceFactory.createCsv(path, config)) {
 *     // process data
 * }
 *
 * // Register custom data source
 * DataSourceFactory.registerProvider("xml", XmlDataSourceProvider.class);
 * }</pre>
 *
 * @since 1.0
 */
public final class DataSourceFactory {

    private static final Map<String, DataSourceProvider> providers = new HashMap<>();

    static {
        // Register built-in providers
        providers.put("csv", new CsvDataSourceProvider());
        providers.put("tsv", new TsvDataSourceProvider());
        providers.put("json", new JsonDataSourceProvider());
    }

    private DataSourceFactory() {
        // Utility class - prevent instantiation
    }

    /**
     * Creates a DataSource from a file path, automatically detecting the format.
     * Format is detected based on file extension.
     *
     * @param path path to the data file
     * @return DataSource for the file
     * @throws IOException                   if file cannot be read
     * @throws UnsupportedFormatException    if format is not supported
     */
    public static DataSource create(Path path) throws IOException {
        String extension = getFileExtension(path);
        DataSourceProvider provider = providers.get(extension.toLowerCase(Locale.ROOT));

        if (provider == null) {
            throw new UnsupportedFormatException("Unsupported file format: " + extension);
        }

        return provider.create(path);
    }

    /**
     * Creates a DataSource from a file path with explicit format specification.
     *
     * @param path   path to the data file
     * @param format format identifier (csv, json, etc.)
     * @return DataSource for the file
     * @throws IOException                   if file cannot be read
     * @throws UnsupportedFormatException    if format is not supported
     */
    public static DataSource create(Path path, String format) throws IOException {
        DataSourceProvider provider = providers.get(format.toLowerCase(Locale.ROOT));

        if (provider == null) {
            throw new UnsupportedFormatException("Unsupported format: " + format);
        }

        return provider.create(path);
    }

    /**
     * Creates a CSV DataSource from a file path.
     *
     * @param path path to the CSV file
     * @return CSV DataSource
     * @throws IOException if file cannot be read
     */
    public static DataSource createCsv(Path path) throws IOException {
        return new CsvDataSource(path, CsvConfig.defaults());
    }

    /**
     * Creates a CSV DataSource from a file path with custom configuration.
     *
     * @param path   path to the CSV file
     * @param config CSV configuration
     * @return CSV DataSource
     * @throws IOException if file cannot be read
     */
    public static DataSource createCsv(Path path, CsvConfig config) throws IOException {
        return new CsvDataSource(path, config);
    }

    /**
     * Creates a CSV DataSource from a reader.
     *
     * @param reader           reader to read CSV data from
     * @param config           CSV configuration
     * @param sourceIdentifier identifier for this source
     * @return CSV DataSource
     * @throws IOException if reader cannot be initialized
     */
    public static DataSource createCsv(Reader reader, CsvConfig config, String sourceIdentifier)
            throws IOException {
        return new CsvDataSource(reader, config, sourceIdentifier);
    }

    /**
     * Creates a JSON DataSource from a file path.
     *
     * @param path path to the JSON file
     * @return JSON DataSource
     * @throws IOException if file cannot be read
     */
    public static DataSource createJson(Path path) throws IOException {
        return new JsonDataSource(path, JsonConfig.defaults());
    }

    /**
     * Creates a JSON DataSource from a file path with custom configuration.
     *
     * @param path   path to the JSON file
     * @param config JSON configuration
     * @return JSON DataSource
     * @throws IOException if file cannot be read
     */
    public static DataSource createJson(Path path, JsonConfig config) throws IOException {
        return new JsonDataSource(path, config);
    }

    /**
     * Creates a JSON DataSource from a reader.
     *
     * @param reader           reader to read JSON data from
     * @param config           JSON configuration
     * @param sourceIdentifier identifier for this source
     * @return JSON DataSource
     * @throws IOException if reader cannot be parsed
     */
    public static DataSource createJson(Reader reader, JsonConfig config, String sourceIdentifier)
            throws IOException {
        return new JsonDataSource(reader, config, sourceIdentifier);
    }

    /**
     * Registers a custom data source provider for a specific format.
     * This allows extension of the factory with new data source types.
     *
     * @param format   format identifier (e.g., "xml", "parquet")
     * @param provider provider instance
     */
    public static void registerProvider(String format, DataSourceProvider provider) {
        providers.put(format.toLowerCase(Locale.ROOT), provider);
    }

    /**
     * Unregisters a data source provider for a specific format.
     *
     * @param format format identifier
     * @return the removed provider, or empty if not found
     */
    public static Optional<DataSourceProvider> unregisterProvider(String format) {
        return Optional.ofNullable(providers.remove(format.toLowerCase(Locale.ROOT)));
    }

    /**
     * Returns true if a provider is registered for the specified format.
     *
     * @param format format identifier
     * @return true if format is supported
     */
    public static boolean isFormatSupported(String format) {
        return providers.containsKey(format.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the set of supported formats.
     *
     * @return set of format identifiers
     */
    public static java.util.Set<String> getSupportedFormats() {
        return java.util.Collections.unmodifiableSet(providers.keySet());
    }

    private static String getFileExtension(Path path) {
        String filename = path.getFileName().toString();
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }

        return "";
    }

    /**
     * Built-in CSV provider.
     */
    private static class CsvDataSourceProvider implements DataSourceProvider {
        @Override
        public DataSource create(Path path) throws IOException {
            return new CsvDataSource(path, CsvConfig.defaults());
        }
    }

    /**
     * Built-in TSV (Tab-Separated Values) provider.
     */
    private static class TsvDataSourceProvider implements DataSourceProvider {
        @Override
        public DataSource create(Path path) throws IOException {
            CsvConfig config = CsvConfig.builder()
                    .delimiter('\t')
                    .hasHeader(true)
                    .build();
            return new CsvDataSource(path, config);
        }
    }

    /**
     * Built-in JSON provider.
     */
    private static class JsonDataSourceProvider implements DataSourceProvider {
        @Override
        public DataSource create(Path path) throws IOException {
            return new JsonDataSource(path, JsonConfig.defaults());
        }
    }
}
