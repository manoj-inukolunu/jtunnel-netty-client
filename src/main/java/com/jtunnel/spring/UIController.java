package com.jtunnel.spring;

import com.jtunnel.client.Tunnel;
import com.jtunnel.client.TunnelClientManager;
import com.jtunnel.data.SearchableDataStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;

@Controller
public class UIController {

  private final TunnelClientManager tunnelClientManager;
  private final SearchableDataStore dataStore;

  public UIController(TunnelClientManager tunnelClientManager, SearchableDataStore dataStore) {
    this.tunnelClientManager = tunnelClientManager;
//    initializeTemplateEngine();
    this.dataStore = dataStore;
  }

 /* private void initializeTemplateEngine() {

    ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
    // XHTML is the default mode, but we set it anyway for better understanding of code
    templateResolver.setTemplateMode("XHTML");
    // This will convert "home" to "/WEB-INF/templates/home.html"
    templateResolver.setPrefix("/resources/templates/");
    templateResolver.setSuffix(".html");
    // Template cache TTL=1h. If not set, entries would be cached until expelled by LRU
    templateResolver.setCacheTTLMs(3600000L);
    this.templateEngine = new TemplateEngine();
    this.templateEngine.setTemplateResolver(templateResolver);

  }*/

  @GetMapping("/stats")
  public String greeting(Model model) {
    return "statsj";
  }


  /*@SneakyThrows
  @GetMapping("/tunnel")
  public String tunnel(Model model, @RequestParam("page") Optional<Integer> page) {
    List<Tunnel> tunnels = tunnelClientManager.getTunnels();
    List<HttpRequest> response = new ArrayList<>();
    dataStore.allRequests().forEach((httpRequest, httpResponse) -> {
      String method = httpRequest.getMethod();
      String line = httpRequest.getInitialLine().substring(httpRequest.getInitialLine().indexOf(" "));
      response.add(
          HttpRequest.builder()
              .initialLine(httpRequest.getInitialLine())
              .requestId(httpRequest.getRequestId())
              .method(method)
              .requestTime(httpRequest.getRequestTime())
              .build());
    });
    model.addAttribute("tunnels", tunnels);
    model.addAttribute("requests", response.subList(0, Math.min(response.size(), 10)));
    model.addAttribute("total", response.size());
    return "tunnel";
  }*/

  @GetMapping("/main")
  public String main(Model model) {
    return "main";
  }

  @GetMapping("/statsj")
  public String statsj(Model model) {
    return "statsj";
  }
}
