package io.jcloud.examples.spring.greetings;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingResource {

    @GetMapping
    public String sayHello() {
        return "Hello World";
    }
}
