package net.jmatrix.http.servlet;

import java.util.List;
import java.util.Map;

public interface HttpHeaderManager {
   public void setHttpHeaders(Map<String,List<String>> headerMap);
}
