package com.finance.concierge;

import com.finance.concierge.service.FinanceAgentToolService;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.models.Gemini;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.Annotations.Schema;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class FinanceAgent {

    private static final String CSV_FILE = "expenses.csv";

    // Updated to include ALL 11 categories from database
    private static final Map<String, Double> BUDGETS = Map.ofEntries(
        Map.entry("Food", 200.0),
        Map.entry("Transport", 100.0),
        Map.entry("Entertainment", 150.0),
        Map.entry("Bills", 300.0),
        Map.entry("Shopping", 250.0),
        Map.entry("Health", 200.0),
        Map.entry("Education", 150.0),
        Map.entry("Grocery", 300.0),
        Map.entry("Investment", 500.0),
        Map.entry("Insurance", 200.0),
        Map.entry("Other", 100.0)
    );

    // Static holder for Spring-managed service (set via constructor)
    private static FinanceAgentToolService toolService;

    // Static holder for current user ID (set per request)
    private static ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static BaseAgent ROOT_AGENT = createAgent(System.getenv("GOOGLE_API_KEY"));

    // Constructor to inject the service
    public FinanceAgent(FinanceAgentToolService toolService) {
        FinanceAgent.toolService = toolService;
    }

    public static void setCurrentUserId(Long userId) {
        currentUserId.set(userId);
    }

    public static Long getCurrentUserId() {
        return currentUserId.get();
    }

    public static void clearCurrentUserId() {
        currentUserId.remove();
    }

    public static BaseAgent createAgent(String apiKey) {
        var builder = LlmAgent.builder()
            .name("finance-agent")
            .instruction("You are a helpful personal finance concierge. You help users track expenses and monitor their budget. " +
                         "Available categories: Food, Transport, Entertainment, Bills (for utilities/rent), Shopping, Health, Education, Grocery, Investment, Insurance, Other. " +
                         "Always use the provided tools to log expenses or check status. " +
                         "Map user expenses to the correct category: " +
                         "- Bills: electricity, water, internet, rent, phone, utilities " +
                         "- Food: coffee, lunch, dinner, restaurants " +
                         "- Grocery: supermarket, household items, vegetables, daily needs " +
                         "- Transport: uber, taxi, bus, gas, parking " +
                         "- Entertainment: movies, games, concerts " +
                         "- Shopping: clothes, electronics, general shopping " +
                         "- Health: doctor, medicine, gym, fitness " +
                         "- Education: books, courses, tuition " +
                         "- Investment: stocks, mutual funds, sip, savings " +
                         "- Insurance: life insurance, health insurance, car insurance premiums " +
                         "- Other: miscellaneous expenses. " +
                         "Today's date is " + LocalDate.now());

        if (apiKey != null && !apiKey.isEmpty()) {
            builder.model(Gemini.builder()
                .modelName("gemini-2.5-flash")
                .apiKey(apiKey)
                .build());
        } else {
            builder.model("gemini-2.5-flash");
        }

        return builder
            .tools(
                FunctionTool.create(FinanceAgent.class, "logExpense"),
                FunctionTool.create(FinanceAgent.class, "getBudgetStatus"),
                FunctionTool.create(FinanceAgent.class, "createMonthlyReport")
            )
            .build();
    }

    @Schema(description = "Logs a new expense to the tracker")
    public static Map<String, String> logExpense(
        @Schema(name = "amount", description = "The amount spent") double amount,
        @Schema(name = "category", description = "The category: Food, Transport, Entertainment, Bills, Shopping, Health, Education, Grocery, Investment, Insurance, or Other") String category,
        @Schema(name = "description", description = "A brief description of the expense") String description
    ) {
        // Use the Spring service to save to database
        if (toolService != null && getCurrentUserId() != null) {
            return toolService.logExpense(amount, category, description, getCurrentUserId());
        }

        // Fallback to CSV only if service not available (shouldn't happen in production)
        try {
            File file = new File(CSV_FILE);
            boolean fileExists = file.exists();
            
            try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))) {
                if (!fileExists) {
                    writer.writeNext(new String[]{"Date", "Category", "Amount", "Description"});
                }
                writer.writeNext(new String[]{
                    LocalDate.now().toString(),
                    category,
                    String.valueOf(amount),
                    description
                });
            }
            return Map.of("result", String.format("Logged $%.2f to %s", amount, category));
        } catch (IOException e) {
            return Map.of("error", "Error logging expense: " + e.getMessage());
        }
    }

    @Schema(description = "Checks the budget status for a specific category")
    public static Map<String, String> getBudgetStatus(
        @Schema(name = "category", description = "Category: Food, Transport, Entertainment, Bills, Shopping, Health, Education, Grocery, Investment, Insurance, or Other") String category
    ) {
        // Use dynamic budgets from database if available
        if (toolService != null && getCurrentUserId() != null) {
            return toolService.getBudgetStatus(category, getCurrentUserId());
        }

        // Fallback to static budgets
        String normalizedCategory = BUDGETS.keySet().stream()
            .filter(k -> k.equalsIgnoreCase(category))
            .findFirst()
            .orElse(category);

        if (!BUDGETS.containsKey(normalizedCategory)) {
            return Map.of("error", "No budget defined for category: " + category + ". Available categories: Food, Transport, Entertainment, Bills, Shopping, Health, Education, Grocery, Investment, Insurance, Other");
        }

        double limit = BUDGETS.get(normalizedCategory);
        double spent = calculateTotalSpent(normalizedCategory);
        double remaining = limit - spent;

        return Map.of("result", String.format("You have spent $%.2f out of $%.2f on %s. Remaining: $%.2f.",
            spent, limit, normalizedCategory, remaining));
    }

    @Schema(description = "Creates a summary report of all spending")
    public static Map<String, String> createMonthlyReport() {
        Map<String, Double> totals = new HashMap<>();
        
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) return Map.of("result", "No expenses logged yet.");

            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                String[] line;
                reader.readNext(); // Skip header
                while ((line = reader.readNext()) != null) {
                    if (line.length >= 3) {
                        String cat = line[1];
                        try {
                            double amount = Double.parseDouble(line[2]);
                            totals.merge(cat, amount, Double::sum);
                        } catch (NumberFormatException e) {
                            // Skip invalid lines
                        }
                    }
                }
            }
        } catch (Exception e) {
            return Map.of("error", "Error generating report: " + e.getMessage());
        }

        if (totals.isEmpty()) {
            return Map.of("result", "No expenses found.");
        }

        StringBuilder report = new StringBuilder("Monthly Spending Report:\n");
        totals.forEach((cat, amount) -> 
            report.append(String.format("- %s: $%.2f\n", cat, amount)));
            
        return Map.of("result", report.toString());
    }

    private static double calculateTotalSpent(String category) {
        double total = 0;
        try {
            File file = new File(CSV_FILE);
            if (!file.exists()) return 0;

            try (CSVReader reader = new CSVReader(new FileReader(file))) {
                String[] line;
                reader.readNext(); // Skip header
                while ((line = reader.readNext()) != null) {
                    if (line.length >= 3 && line[1].equalsIgnoreCase(category)) {
                        try {
                            total += Double.parseDouble(line[2]);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }
}
