package com.faforever.client.api;

import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import lombok.SneakyThrows;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@Component
public class JsonApiMessageConverter extends AbstractHttpMessageConverter<Object> {
  private final ResourceConverter resourceConverter;

  @Inject
  public JsonApiMessageConverter(ResourceConverter resourceConverter) {
    super(MediaType.parseMediaType("application/vnd.api+json"));
    this.resourceConverter = resourceConverter;
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return true;
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage
      inputMessage) throws IOException, HttpMessageNotReadableException {
    try (InputStream inputStream = inputMessage.getBody()) {
      if (Iterable.class.isAssignableFrom(clazz)) {
        return resourceConverter.readDocumentCollection(inputStream, clazz);
      }

      return resourceConverter.readDocument(inputMessage.getBody(), clazz);
    }
  }

  @Override
  @SneakyThrows
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (o instanceof Iterable) {
      resourceConverter.writeDocumentCollection(new JSONAPIDocument<Iterable<?>>((Iterable<?>) o));
    } else {
      resourceConverter.writeDocument(new JSONAPIDocument<>(o));
    }
  }
}
