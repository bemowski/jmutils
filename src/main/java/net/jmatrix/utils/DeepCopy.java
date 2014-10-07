package net.jmatrix.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * A deep copy utility class based on jackson/json.  
 */
public class DeepCopy {
   
   
   public static final <T> T copy(Object o, Class<T> t) throws JsonParseException, JsonMappingException, JsonGenerationException, IOException {
      ObjectMapper om = new ObjectMapper();
      T inst = om.readValue(om.writeValueAsString(o), t);
      
      return inst;
   }
}
