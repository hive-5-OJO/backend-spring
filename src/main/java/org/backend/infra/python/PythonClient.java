package org.backend.infra.python;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;

@Component
public class PythonClient {
	private final WebClient webClient;

	public PythonClient(@Value("${ANALYTICS_URL:http://localhost:8000}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

	public Mono<String> test(String name) {
	    return webClient.post()
	        .uri("/test")
	        .bodyValue(Map.of("name", name))
	        .retrieve()
	        .bodyToMono(String.class)
	        .doOnNext(response -> System.out.println("========== 파이썬 응답: " + response)) // 콘솔에 출력
	        .doOnError(error -> System.err.println("---------- 연동 에러: " + error.getMessage()));
	}
}
