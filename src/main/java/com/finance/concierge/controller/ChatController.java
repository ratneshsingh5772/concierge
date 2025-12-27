package com.finance.concierge.controller;

import com.finance.concierge.FinanceAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final InMemoryRunner runner;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public ChatController(org.springframework.core.env.Environment environment) {
        String apiKey = environment.getProperty("google.api.key");
        this.runner = new InMemoryRunner(FinanceAgent.createAgent(apiKey));
    }

    @PostMapping(value = "/message", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@RequestBody ChatRequest request) {
        log.info("Received message: {}", request.getMessage());

        String userId = request.getUserId() != null ? request.getUserId() : "default-user";

        // Get or create session
        Session session = sessions.computeIfAbsent(userId, id ->
            runner.sessionService()
                .createSession(runner.appName(), id)
                .blockingGet()
        );

        RunConfig runConfig = RunConfig.builder().build();
        Content userMsg = Content.fromParts(Part.fromText(request.getMessage()));

        Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

        return Flux.create(sink -> {
            events.subscribe(
                event -> {
                    if (event.finalResponse()) {
                        String response = event.stringifyContent();
                        log.info("Agent response: {}", response);
                        sink.next(response);
                    }
                },
                error -> {
                    log.error("Error processing message", error);
                    sink.error(error);
                },
                sink::complete
            );
        });
    }

    @PostMapping("/reset")
    public Map<String, String> resetSession(@RequestBody Map<String, String> request) {
        String userId = request.getOrDefault("userId", "default-user");
        sessions.remove(userId);
        log.info("Session reset for user: {}", userId);
        return Map.of("status", "success", "message", "Session reset successfully");
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "agent", "finance-concierge");
    }

    @Data
    public static class ChatRequest {
        private String message;
        private String userId;
    }
}

