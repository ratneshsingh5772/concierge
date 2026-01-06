package com.finance.concierge;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@SpringBootApplication
public class ConciergeApplication implements CommandLineRunner {

	private final Environment environment;

	public ConciergeApplication(Environment environment) {
		this.environment = environment;
	}

	public static void main(String[] args) {
		SpringApplication.run(ConciergeApplication.class, args);
	}

	@Override
	@SuppressWarnings("java:S106") // Standard outputs should not be used directly to log anything
	public void run(String... args) {
		// Load API key from application.properties if not in env
		String apiKey = environment.getProperty("google.api.key");

		// Check if running in non-interactive mode.
		// If System.console() is null, we might be in background, test, or redirect.
		// We only proceed if we can confirm meaningful interactive capability or explicit override.
		// For safety in 'mvn spring-boot:run' or background, if System.in is not available, we abort.
		try {
			if (System.console() == null && System.in.available() <= 0 && !isIdeConsole()) {
				log.info("Non-interactive environment detected. CLI disabled.");
				return;
			}
		} catch (Exception e) {
			// System.in.available() might throw if stream closed
			log.info("Input stream not available. CLI disabled.");
			return;
		}

		log.info("========================================");
		log.info("  Personal Finance Concierge Started!");
		log.info("========================================");
		log.info("Web UI: http://localhost:8081");
		log.info("API Endpoint: http://localhost:8081/api/chat");
		log.info("CLI: Type below or use 'exit' to quit");
		log.info("========================================\n");


		RunConfig runConfig = RunConfig.builder().build();
		InMemoryRunner runner = new InMemoryRunner(FinanceAgent.createAgent(apiKey));

		Session session = runner
				.sessionService()
				.createSession(runner.appName(), "cli-user")
				.blockingGet();

		try (Scanner scanner = new Scanner(System.in, UTF_8)) {
			boolean running = true;
			while (running) {
				System.out.print("> ");
				// Check for input availability before blocking to avoid NoSuchElementException in some environments
				if (!scanner.hasNextLine()) {
					running = false;
				} else {
					String input = scanner.nextLine();

					if ("exit".equalsIgnoreCase(input.trim())) {
						log.info("Goodbye!");
						running = false;
					} else {
						try {
							Content userMsg = Content.fromParts(Part.fromText(input));
							Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

							events.blockingForEach(event -> {
								if (event.finalResponse()) {
									// CLI output requires System.out
									System.out.println("\n" + event.stringifyContent() + "\n");
								}
							});
						} catch (Exception e) {
							log.error("Error processing text input", e);
						}
					}
				}
			}
		}
	}

	private boolean isIdeConsole() {
		// Basic check if we might be in an IDE where System.console() is null but we still have input
		return System.getenv("jetbrains.client.id") != null || System.getProperty("java.class.path").contains("idea_rt.jar");
	}
}
