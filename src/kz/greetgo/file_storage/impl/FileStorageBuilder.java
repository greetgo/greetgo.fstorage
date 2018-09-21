package kz.greetgo.file_storage.impl;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.sql.DataSource;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Builder of file storage
 */
public interface FileStorageBuilder {

  /**
   * Creates a new file store builder
   *
   * @return the file storage builder
   */
  static FileStorageBuilder newBuilder() {
    return new FileStorageBuilderImpl();
  }

  /**
   * Indicates whether the file name is mandatory
   *
   * @param mandatoryName <code>true</code> - file name is mandatory, otherwise - optional
   * @return reference to this builder
   */
  FileStorageBuilder mandatoryName(boolean mandatoryName);

  /**
   * Indicates whether the file MIME-type is mandatory
   *
   * @param mandatoryMimeType <code>true</code> - file MIME-type is mandatory, otherwise - optional
   * @return reference to this builder
   */
  FileStorageBuilder mandatoryMimeType(boolean mandatoryMimeType);

  /**
   * Sets MIME-type validator function
   *
   * @param validator MIME-type validator function.
   *                  The function gets a type and returns a correctness criterion of this type:
   *                  if function returns <code>true</code>, then validation OK, otherwise - exception would be thrown
   * @return reference to this builder
   */
  FileStorageBuilder mimeTypeValidator(Function<String, Boolean> validator);

  /**
   * Sets the MimeType calculator by file name. Handles when setting the file name.
   *
   * @param mimeTypeExtractor MimeType calculator by file name or <code>null</code>, if calculator is not needed
   * @return reference to this builder
   */
  @SuppressWarnings("UnusedReturnValue")
  FileStorageBuilder mimeTypeExtractor(Function<String, String> mimeTypeExtractor);

  /**
   * Allows you to configure the builder from a third-party object
   *
   * @param configurator the third-party object to configure builder
   * @return reference to this builder
   */
  FileStorageBuilder configureFrom(FileStorageBuilderConfigurator configurator);

  /**
   * Replaces the identifier generator with another one
   *
   * @param idGenerator another the identifier generator
   * @param idLength    the maximum length in characters, generated identifiers
   * @return reference to this builder
   */
  FileStorageBuilder setIdGenerator(int idLength, Supplier<String> idGenerator);

  /**
   * Switches to the builder, which stores files in one relational database
   *
   * @param dataSource {@link DataSource} where files are storing
   * @return reference to new builder
   */
  FileStorageBuilderMonoDb inDb(DataSource dataSource);

  /**
   * Switches to the builder, which will store files in several relational databases
   *
   * @param dataSourceList List of DataSources to relational databases. The order in the list
   *                       is very important, since the data is sharding
   * @return reference to new builder
   */
  FileStorageBuilderMultiDb inMultiDb(List<DataSource> dataSourceList);

  /**
   * Switches to the builder, which will store files in MongoDB
   *
   * @param collection a mongodb collection to storing files
   * @return reference to new builder
   */
  FileStorageBuilderInMongodb inMongodb(MongoCollection<Document> collection);
}
