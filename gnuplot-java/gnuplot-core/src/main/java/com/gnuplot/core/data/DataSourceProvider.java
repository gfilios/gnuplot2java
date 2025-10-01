package com.gnuplot.core.data;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Service provider interface for creating DataSource instances.
 * Implementations can be registered with the DataSourceFactory to support
 * additional data formats.
 *
 * <p>Example implementation:
 * <pre>{@code
 * public class XmlDataSourceProvider implements DataSourceProvider {
 *     @Override
 *     public DataSource create(Path path) throws IOException {
 *         return new XmlDataSource(path, XmlConfig.defaults());
 *     }
 * }
 *
 * // Register with factory
 * DataSourceFactory.registerProvider("xml", new XmlDataSourceProvider());
 * }</pre>
 *
 * @since 1.0
 */
@FunctionalInterface
public interface DataSourceProvider {

    /**
     * Creates a DataSource from a file path using default configuration.
     *
     * @param path path to the data file
     * @return DataSource instance
     * @throws IOException if the file cannot be read
     */
    DataSource create(Path path) throws IOException;
}
