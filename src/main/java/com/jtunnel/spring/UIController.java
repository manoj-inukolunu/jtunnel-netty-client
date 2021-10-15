package com.jtunnel.spring;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIController {

  @GetMapping("/stats")
  public String greeting(Model model) {
    return "stats";
  }

}
