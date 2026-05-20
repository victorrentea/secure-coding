package victor.training.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Sanitizes every textual value inside JSON request and response payloads
 * using the OWASP HTML sanitizer (allow-list of harmless formatting/block tags).
 *
 * Disabled by default — enable by activating the {@code richtext-filter} Spring profile.
 */
@Slf4j
@Component
@Profile("richtext-filter")
public class RichTextSanitizerHttpFilter extends OncePerRequestFilter {
  private static final PolicyFactory SANITIZER = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, jakarta.servlet.ServletException {
    HttpServletRequest req = isJson(request.getContentType()) ? new SanitizedJsonRequest(request) : request;
    ContentCachingResponseWrapper resp = new ContentCachingResponseWrapper(response);

    chain.doFilter(req, resp);

    if (isJson(resp.getContentType())) {
      byte[] body = resp.getContentAsByteArray();
      if (body.length > 0) {
        try {
          JsonNode tree = objectMapper.readTree(body);
          sanitizeTree(tree);
          byte[] cleaned = objectMapper.writeValueAsBytes(tree);
          resp.resetBuffer();
          resp.getOutputStream().write(cleaned);
        } catch (IOException parseFailed) {
          log.warn("Skipping response sanitization — could not parse JSON: {}", parseFailed.getMessage());
        }
      }
    }
    resp.copyBodyToResponse();
  }

  private static boolean isJson(String contentType) {
    return contentType != null && contentType.toLowerCase().contains("json");
  }

  private void sanitizeTree(JsonNode node) {
    if (node instanceof ObjectNode obj) {
      List<String> fieldNames = new ArrayList<>();
      Iterator<String> it = obj.fieldNames();
      while (it.hasNext()) fieldNames.add(it.next());
      for (String name : fieldNames) {
        JsonNode child = obj.get(name);
        if (child.isTextual()) {
          obj.put(name, SANITIZER.sanitize(child.asText()));
        } else {
          sanitizeTree(child);
        }
      }
    } else if (node instanceof ArrayNode arr) {
      for (int i = 0; i < arr.size(); i++) {
        JsonNode child = arr.get(i);
        if (child.isTextual()) {
          arr.set(i, TextNode.valueOf(SANITIZER.sanitize(child.asText())));
        } else {
          sanitizeTree(child);
        }
      }
    }
  }

  private class SanitizedJsonRequest extends HttpServletRequestWrapper {
    private final byte[] body;

    SanitizedJsonRequest(HttpServletRequest original) throws IOException {
      super(original);
      this.body = sanitizeBody(original.getInputStream().readAllBytes());
    }

    private byte[] sanitizeBody(byte[] raw) {
      if (raw.length == 0) return raw;
      try {
        JsonNode tree = objectMapper.readTree(raw);
        sanitizeTree(tree);
        return objectMapper.writeValueAsBytes(tree);
      } catch (IOException parseFailed) {
        log.warn("Skipping request sanitization — could not parse JSON: {}", parseFailed.getMessage());
        return raw;
      }
    }

    @Override
    public ServletInputStream getInputStream() {
      ByteArrayInputStream source = new ByteArrayInputStream(body);
      return new ServletInputStream() {
        @Override public int read() { return source.read(); }
        @Override public boolean isFinished() { return source.available() == 0; }
        @Override public boolean isReady() { return true; }
        @Override public void setReadListener(ReadListener l) { throw new UnsupportedOperationException(); }
      };
    }

    @Override
    public BufferedReader getReader() {
      return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    @Override
    public int getContentLength() {
      return body.length;
    }

    @Override
    public long getContentLengthLong() {
      return body.length;
    }
  }
}
