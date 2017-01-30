package com.faforever.client.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.nio.file.Files.size;

/**
 * Concrete implementation of {@link AbstractInputStreamContent} that generates repeatable input
 * streams based on the contents of a file, while counting the number of written bytes.
 */
public class CountingFileContent extends AbstractInputStreamContent {

  private final Path file;
  private ByteCountListener listener;

  public CountingFileContent(String type, Path file, ByteCountListener listener) {
    super(type);
    this.file = Optional.ofNullable(file)
        .orElseThrow(() -> new IllegalArgumentException("'file' must not be null"));
    this.listener = Optional.ofNullable(listener)
        .orElseThrow(() -> new IllegalArgumentException("'listener' must not be null"));
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    ByteCopier.from(getInputStream())
        .to(out)
        .listener(listener)
        .totalBytes(getLength())
        .copy();
  }

  public long getLength() throws IOException {
    return size(file);
  }

  public boolean retrySupported() {
    return true;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return Files.newInputStream(file);
  }

  @Override
  public CountingFileContent setType(String type) {
    return (CountingFileContent) super.setType(type);
  }

  @Override
  public CountingFileContent setCloseInputStream(boolean closeInputStream) {
    return (CountingFileContent) super.setCloseInputStream(closeInputStream);
  }
}
