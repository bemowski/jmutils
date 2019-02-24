package net.jmatrix.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * A deep copy utility class based on jackson/json.
 */
public class DeepCopy {
  static ObjectMapper om = new ObjectMapper();

  public static final <T> T copy(Object o, Class<T> t) throws IOException {
   T inst = om.readValue(om.writeValueAsString(o), t);

    return inst;
  }
}