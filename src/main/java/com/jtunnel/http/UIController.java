package com.jtunnel.http;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UIController {

  @GetMapping("/stats")
  public String greeting(Model model) {
    return "stats";
  }

}
