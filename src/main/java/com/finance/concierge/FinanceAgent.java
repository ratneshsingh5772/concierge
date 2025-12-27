package com.finance.concierge;

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
    private static final Map<String, Double> BUDGETS = Map.of(
        "Food", 200.0,
        "Transport", 100.0,
        "Entertainment", 150.0
    );

    public static BaseAgent ROOT_AGENT = createAgent(System.getenv("GOOGLE_API_KEY"));

    public static BaseAgent createAgent(String apiKey) {
        var builder = LlmAgent.builder()
            .name("finance-agent")
            .instruction("You are a helpful personal finance concierge. You help users track expenses and monitor their budget. " +
                         "Always use the provided tools to log expenses or check status. " +
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

    // Keep a static reference for backward compatibility if needed, but it might fail if env var is missing
    // public static BaseAgent ROOT_AGENT = createAgent(null);


    @Schema(description = "Logs a new expense to the tracker")
    public static Map<String, String> logExpense(
        @Schema(name = "amount", description = "The amount spent") double amount,
        @Schema(name = "category", description = "The category of the expense (e.g., Food, Transport)") String category,
        @Schema(name = "description", description = "A brief description of the expense") String description
    ) {
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
        @Schema(name = "category", description = "The category to check (Food, Transport, Entertainment)") String category
    ) {
        // Normalize category name for case-insensitive lookup
        String normalizedCategory = BUDGETS.keySet().stream()
            .filter(k -> k.equalsIgnoreCase(category))
            .findFirst()
            .orElse(category);

        if (!BUDGETS.containsKey(normalizedCategory)) {
            return Map.of("error", "No budget defined for category: " + category);
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
