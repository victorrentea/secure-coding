package victor.training.spring.web.controller.util;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DatabaseBrowserController {
  private static final int MAX_ROWS_PER_TABLE = 50;
  private final JdbcTemplate jdbc;

  @GetMapping(value = "/db", produces = "text/html")
  public String databaseBrowser() {
    List<Map<String, Object>> tables = jdbc.queryForList(
        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' ORDER BY TABLE_NAME");

    StringBuilder html = new StringBuilder();
    html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>DB Browser</title>");
    html.append("<style>");
    html.append("body{font-family:sans-serif;margin:20px;background:#f5f5f5}");
    html.append("h1{color:#333}");
    html.append("h2{color:#555;margin-top:30px;cursor:pointer}");
    html.append("h2:hover{color:#007bff}");
    html.append("table{border-collapse:collapse;width:100%;margin-bottom:10px;background:#fff}");
    html.append("th,td{border:1px solid #ddd;padding:6px 10px;text-align:left;font-size:13px}");
    html.append("th{background:#4a90d9;color:#fff}");
    html.append("tr:nth-child(even){background:#f9f9f9}");
    html.append("tr:hover{background:#e9e9e9}");
    html.append(".info{color:#888;font-style:italic;margin-bottom:20px}");
    html.append(".empty{color:#999}");
    html.append("</style></head><body>");
    html.append("<h1>Database Browser</h1>");
    html.append("<p class='info'>Auto-refreshes on page reload. Showing max ").append(MAX_ROWS_PER_TABLE).append(" rows per table.</p>");

    for (Map<String, Object> tableRow : tables) {
      String tableName = (String) tableRow.get("TABLE_NAME");
      Long totalCount = jdbc.queryForObject("SELECT COUNT(*) FROM \"" + tableName + "\"", Long.class);
      html.append("<h2>").append(escapeHtml(tableName))
          .append(" <span style='font-size:14px;color:#888'>(").append(totalCount).append(" rows)</span></h2>");

      if (totalCount == 0) {
        html.append("<p class='empty'>Empty table</p>");
        continue;
      }

      List<Map<String, Object>> rows = jdbc.queryForList(
          "SELECT * FROM \"" + tableName + "\" LIMIT " + MAX_ROWS_PER_TABLE);

      if (!rows.isEmpty()) {
        html.append("<table><tr>");
        for (String col : rows.get(0).keySet()) {
          html.append("<th>").append(escapeHtml(col)).append("</th>");
        }
        html.append("</tr>");
        for (Map<String, Object> row : rows) {
          html.append("<tr>");
          for (Object val : row.values()) {
            html.append("<td>").append(val == null ? "<em>null</em>" : escapeHtml(String.valueOf(val))).append("</td>");
          }
          html.append("</tr>");
        }
        html.append("</table>");
        if (totalCount > MAX_ROWS_PER_TABLE) {
          html.append("<p class='info'>... and ").append(totalCount - MAX_ROWS_PER_TABLE).append(" more rows</p>");
        }
      }
    }

    html.append("</body></html>");
    return html.toString();
  }

  private static String escapeHtml(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }
}
