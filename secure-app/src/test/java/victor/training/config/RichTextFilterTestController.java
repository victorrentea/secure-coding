package victor.training.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Test-only controller exercised exclusively by RichTextSanitizerHttpFilterTest.
@RestController
@RequestMapping("/test-filter")
class RichTextFilterTestController {

  @PostMapping("/echo")
  TestNode echo(@RequestBody TestNode node) {
    return node;
  }

  // Hard-codes a dirty response — isolates response-side sanitization.
  @GetMapping("/dirty-response")
  TestNode dirtyResponse() {
    return new TestNode(
        "Top<script>alert(1)</script>",
        new TestNode("Nested<script>x</script>", null, null, null),
        List.of("tag<script>x</script>", "plain"),
        List.of(new TestNode(
            "Child<script>x</script>",
            null,
            null,
            List.of(new TestNode("Grandchild<script>x</script>", null, null, null)))));
  }

  // Throws if the controller sees unsanitized input — isolates request-side sanitization.
  @PostMapping("/assert-clean")
  String assertClean(@RequestBody TestNode node) {
    requireClean(node);
    return "ok";
  }

  private void requireClean(TestNode n) {
    if (n == null) return;
    if (n.name() != null && n.name().contains("<script>")) {
      throw new IllegalStateException("Unsanitized name: " + n.name());
    }
    if (n.tags() != null) {
      for (String t : n.tags()) {
        if (t.contains("<script>")) throw new IllegalStateException("Unsanitized tag: " + t);
      }
    }
    requireClean(n.nested());
    if (n.children() != null) n.children().forEach(this::requireClean);
  }
}

record TestNode(String name, TestNode nested, List<String> tags, List<TestNode> children) {}
