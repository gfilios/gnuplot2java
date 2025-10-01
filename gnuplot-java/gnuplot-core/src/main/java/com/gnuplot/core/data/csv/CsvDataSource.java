package com.gnuplot.core.data.csv;

import com.gnuplot.core.data.DataMetadata;
import com.gnuplot.core.data.DataRecord;
import com.gnuplot.core.data.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * DataSource implementation for CSV files.
 * Supports streaming for large files and configurable parsing options.
 *
 * <p>Example usage:
 * <pre>{@code
 * CsvConfig config = CsvConfig.builder()
 *     .delimiter(',')
 *     .hasHeader(true)
 *     .build();
 *
 * try (CsvDataSource csv = new CsvDataSource(path, config)) {
 *     for (DataRecord record : csv) {
 *         double x = record.getDouble(0);
 *         double y = record.getDouble(1);
 *         // process data...
 *     }
 * }
 * }</pre>
 *
 * @since 1.0
 */
public class CsvDataSource implements DataSource {

    private final BufferedReader reader;
    private final CsvConfig config;
    private final CsvMetadata metadata;
    private boolean closed = false;

    /**
     * Creates a CSV data source from a file path.
     *
     * @param path   path to the CSV file
     * @param config CSV parsing configuration
     * @throws IOException if file cannot be read
     */
    public CsvDataSource(Path path, CsvConfig config) throws IOException {
        this(Files.newBufferedReader(path), config, path.toString());
    }

    /**
     * Creates a CSV data source from a reader.
     *
     * @param reader           reader to read CSV data from
     * @param config           CSV parsing configuration
     * @param sourceIdentifier identifier for this source (filename, URL, etc.)
     * @throws IOException if reader cannot be initialized
     */
    public CsvDataSource(Reader reader, CsvConfig config, String sourceIdentifier) throws IOException {
        this.reader = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader);
        this.config = config;
        this.metadata = initializeMetadata(sourceIdentifier);
    }

    private CsvMetadata initializeMetadata(String sourceIdentifier) throws IOException {
        // Skip initial lines if configured
        for (int i = 0; i < config.getSkipLines(); i++) {
            reader.readLine();
        }

        // Read header if present
        String headerLine = null;
        if (config.hasHeader()) {
            headerLine = readNextValidLine();
        }

        return new CsvMetadata(sourceIdentifier, headerLine, config);
    }

    private String readNextValidLine() throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            // Skip empty lines if configured
            if (config.isSkipEmptyLines() && line.trim().isEmpty()) {
                continue;
            }
            // Skip comment lines
            if (config.getCommentPrefix() != null && line.trim().startsWith(config.getCommentPrefix())) {
                continue;
            }
            return line;
        }
        return null;
    }

    @Override
    public DataMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Iterator<DataRecord> iterator() {
        if (closed) {
            throw new IllegalStateException("DataSource has been closed");
        }
        return new CsvIterator();
    }

    @Override
    public void close() {
        if (!closed) {
            try {
                reader.close();
            } catch (IOException e) {
                // Log but don't throw during close
            }
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * Iterator implementation for streaming CSV records.
     */
    private class CsvIterator implements Iterator<DataRecord> {
        private String nextLine;
        private long recordNumber = 0;

        CsvIterator() {
            advance();
        }

        private void advance() {
            try {
                nextLine = readNextValidLine();
            } catch (IOException e) {
                throw new CsvParseException("Error reading CSV line", e);
            }
        }

        @Override
        public boolean hasNext() {
            return nextLine != null;
        }

        @Override
        public DataRecord next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more records");
            }

            try {
                String currentLine = nextLine;
                recordNumber++;
                advance();
                return parseLine(currentLine, recordNumber);
            } catch (CsvParseException e) {
                throw e;
            } catch (Exception e) {
                throw new CsvParseException(
                        "Error parsing CSV record at line " + recordNumber,
                        e
                );
            }
        }

        private DataRecord parseLine(String line, long lineNumber) {
            CsvParser parser = new CsvParser(config);
            return parser.parseLine(line, metadata, lineNumber);
        }
    }
}
