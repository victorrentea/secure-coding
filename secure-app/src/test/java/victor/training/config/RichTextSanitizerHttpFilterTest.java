package victor.training.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test", "richtext-filter"})
@WithMockUser
class RichTextSanitizerHttpFilterTest {
  @Autowired MockMvc mockMvc;
  @MockBean JwtDecoder jwtDecoder;

  // ====== Response-side: cover every JSON shape ======

  @Test
  void response_topLevelString() throws Exception {
    mockMvc.perform(get("/test-filter/dirty-response"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name", not(containsString("<script>"))));
  }

  @Test
  void response_nestedObject() throws Exception {
    mockMvc.perform(get("/test-filter/dirty-response"))
        .andExpect(jsonPath("$.nested.name", not(containsString("<script>"))));
  }

  @Test
  void response_listOfStrings() throws Exception {
    mockMvc.perform(get("/test-filter/dirty-response"))
        .andExpect(jsonPath("$.tags[0]", not(containsString("<script>"))));
  }

  @Test
  void response_listOfObjects() throws Exception {
    mockMvc.perform(get("/test-filter/dirty-response"))
        .andExpect(jsonPath("$.children[0].name", not(containsString("<script>"))));
  }

  @Test
  void response_recursiveChildren() throws Exception {
    mockMvc.perform(get("/test-filter/dirty-response"))
        .andExpect(jsonPath("$.children[0].children[0].name", not(containsString("<script>"))));
  }

  // ====== Request-side: controller asserts it received clean data ======

  @Test
  void request_isSanitizedBeforeControllerSeesIt() throws Exception {
    String dirty = """
        {
          "name": "Top<script>x</script>",
          "nested": {"name": "Nested<script>x</script>"},
          "tags": ["tag<script>x</script>"],
          "children": [{"name": "Child<script>x</script>",
                        "children": [{"name": "Grand<script>x</script>"}]}]
        }
        """;
    mockMvc.perform(post("/test-filter/assert-clean")
            .contentType(MediaType.APPLICATION_JSON)
            .content(dirty))
        .andExpect(status().isOk())
        .andExpect(content().string("ok"));
  }

  // ====== Safe formatting is preserved (allow-list, not block-list) ======

  @Test
  void safeFormatting_isPreserved() throws Exception {
    String body = """
        {"name": "<b>bold</b> and <i>italic</i> survive"}
        """;
    mockMvc.perform(post("/test-filter/echo")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(jsonPath("$.name", containsString("<b>bold</b>")))
        .andExpect(jsonPath("$.name", containsString("<i>italic</i>")));
  }
}
