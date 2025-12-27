package com.finance.concierge;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

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
	public void run(String... args) throws Exception {
		// Load API key from application.properties if not in env
		String apiKey = environment.getProperty("google.api.key");
		if (apiKey != null && !apiKey.isEmpty() && System.getenv("GOOGLE_API_KEY") == null) {
			// Set it as a system property which some SDKs might check,
			// but for Google ADK/GenAI we might need to ensure it's available.
			// Since we can't easily set Env vars in Java, we'll rely on the Agent being initialized
			// AFTER we check this, or we might need to pass it explicitly.
			// However, FinanceAgent initializes statically.

			// WORKAROUND: The FinanceAgent class initializes ROOT_AGENT statically.
			// We need to ensure the key is available before that class is loaded if possible,
			// or we need to modify FinanceAgent to be non-static.
		}

		System.out.println("========================================");
		System.out.println("  Personal Finance Concierge Started!");
		System.out.println("========================================");
		System.out.println("Web UI: http://localhost:8081");
		System.out.println("API Endpoint: http://localhost:8081/api/chat");
		System.out.println("CLI: Type below or use 'exit' to quit");
		System.out.println("========================================\n");

		RunConfig runConfig = RunConfig.builder().build();
		InMemoryRunner runner = new InMemoryRunner(FinanceAgent.createAgent(apiKey));

		Session session = runner
				.sessionService()
				.createSession(runner.appName(), "cli-user")
				.blockingGet();

		try (Scanner scanner = new Scanner(System.in, UTF_8)) {
			while (true) {
				System.out.print("> ");
				String input = scanner.nextLine();

				if ("exit".equalsIgnoreCase(input.trim())) {
					System.out.println("Goodbye!");
					break;
				}

				try {
					Content userMsg = Content.fromParts(Part.fromText(input));
					Flowable<Event> events = runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

					events.blockingForEach(event -> {
						if (event.finalResponse()) {
							System.out.println("\n" + event.stringifyContent() + "\n");
						}
					});
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
				}
			}
		}
	}
}
