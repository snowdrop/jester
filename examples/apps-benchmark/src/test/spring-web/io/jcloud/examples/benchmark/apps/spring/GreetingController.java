package io.jcloud.examples.benchmark.apps.spring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @GetMapping("/greeting")
    public String sayHello() {
        return "Hello!";
    }
}
