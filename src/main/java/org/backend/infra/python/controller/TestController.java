package org.backend.infra.python.controller;

import org.backend.infra.python.PythonClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
	private final PythonClient pythonClient;

    public TestController(PythonClient pythonClient) {
        this.pythonClient = pythonClient;
    }

    @GetMapping("/api/test-python")
    public Mono<String> testPython(@RequestParam String name) {
        return pythonClient.test(name);
    }
}
