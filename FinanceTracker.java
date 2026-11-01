import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;

public class FinanceTracker {
    private static final Path FILE = Paths.get("data", "transactions.csv");
    private static final Scanner INPUT = new Scanner(System.in);
    private static final List<Transaction> TRANSACTIONS = new ArrayList<>();

    public static void main(String[] args) {
        loadTransactions();
        while (true) {
            System.out.println("\n=== Personal Finance Tracker ===");
            System.out.println("1. Add transaction\n2. View all transactions\n3. Monthly summary\n4. Filter by category\n5. Export CSV\n0. Exit");
            String choice = ask("Choose an option: ");
            switch (choice) {
                case "1": addTransaction(); break;
                case "2": showTransactions(TRANSACTIONS); break;
                case "3": monthlySummary(); break;
                case "4": filterByCategory(); break;
                case "5": exportCsv(); break;
                case "0": saveTransactions(); System.out.println("Transactions saved. Bye!"); return;
                default: System.out.println("Please choose one of the listed options.");
            }
        }
    }

    private static void addTransaction() {
        String typeInput = ask("Type (I = income, E = expense): ").trim().toUpperCase(Locale.ROOT);
        if (!typeInput.equals("I") && !typeInput.equals("E")) { System.out.println("Enter I or E."); return; }
        String dateInput = ask("Date (YYYY-MM-DD): ").trim();
        LocalDate date;
        try { date = LocalDate.parse(dateInput); }
        catch (DateTimeParseException e) { System.out.println("Date format must be YYYY-MM-DD."); return; }
        String amountInput = ask("Amount (example: 1250.50): ").trim();
        BigDecimal amount;
        try { amount = new BigDecimal(amountInput); }
        catch (NumberFormatException e) { System.out.println("Enter a valid amount."); return; }
        if (amount.signum() <= 0) { System.out.println("Amount must be greater than zero."); return; }
        String category = ask("Category (example: Salary, Food, Travel): ").trim();
        if (category.isEmpty()) { System.out.println("Category cannot be empty."); return; }
        String note = ask("Note (optional): ").trim();
        TRANSACTIONS.add(new Transaction(typeInput.equals("I") ? "INCOME" : "EXPENSE", date, amount, category, note));
        saveTransactions();
        System.out.println("Transaction added.");
    }

    private static void monthlySummary() {
        String input = ask("Month (YYYY-MM): ").trim();
        YearMonth month;
        try { month = YearMonth.parse(input); }
        catch (DateTimeParseException e) { System.out.println("Enter month as YYYY-MM."); return; }
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        Map<String, BigDecimal> byCategory = new TreeMap<>();
        for (Transaction t : TRANSACTIONS) {
            if (!YearMonth.from(t.date).equals(month)) continue;
            if (t.type.equals("INCOME")) income = income.add(t.amount);
            else {
                expense = expense.add(t.amount);
                byCategory.put(t.category, byCategory.getOrDefault(t.category, BigDecimal.ZERO).add(t.amount));
            }
        }
        System.out.println("\nSummary for " + month);
        System.out.println("Income:       " + income.toPlainString());
        System.out.println("Expenses:     " + expense.toPlainString());
        System.out.println("Net cash flow:" + income.subtract(expense).toPlainString());
        if (!byCategory.isEmpty()) {
            System.out.println("Expense by category:");
            for (Map.Entry<String, BigDecimal> entry : byCategory.entrySet())
                System.out.println("  " + entry.getKey() + ": " + entry.getValue().toPlainString());
        }
    }

    private static void filterByCategory() {
        String query = ask("Category to find: ").trim().toLowerCase(Locale.ROOT);
        List<Transaction> matches = new ArrayList<>();
        for (Transaction t : TRANSACTIONS)
            if (t.category.toLowerCase(Locale.ROOT).contains(query)) matches.add(t);
        showTransactions(matches);
    }

    private static void showTransactions(List<Transaction> list) {
        if (list.isEmpty()) { System.out.println("No transactions found."); return; }
        System.out.printf("%-10s %-9s %12s %-18s %s%n", "DATE", "TYPE", "AMOUNT", "CATEGORY", "NOTE");
        System.out.println("--------------------------------------------------------------------------");
        for (Transaction t : list)
            System.out.printf("%-10s %-9s %12s %-18s %s%n", t.date, t.type, t.amount.toPlainString(), t.category, t.note);
    }

    private static void exportCsv() {
        Path output = Paths.get("data", "finance-summary.csv");
        try {
            Files.createDirectories(output.getParent());
            List<String> lines = new ArrayList<>();
            lines.add("date,type,amount,category,note");
            for (Transaction t : TRANSACTIONS)
                lines.add(csv(t.date.toString()) + "," + csv(t.type) + "," + csv(t.amount.toPlainString()) + "," + csv(t.category) + "," + csv(t.note));
            Files.write(output, lines, StandardCharsets.UTF_8);
            System.out.println("CSV exported to " + output);
        } catch (IOException e) { System.out.println("Could not export CSV: " + e.getMessage()); }
    }

    private static String csv(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }

    private static void loadTransactions() {
        try {
            Files.createDirectories(FILE.getParent());
            if (!Files.exists(FILE)) return;
            for (String line : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                List<String> f = parseCsv(line);
                if (f.size() == 5) {
                    try { TRANSACTIONS.add(new Transaction(f.get(1), LocalDate.parse(f.get(0)), new BigDecimal(f.get(2)), f.get(3), f.get(4))); }
                    catch (RuntimeException ignored) { System.out.println("Skipped an invalid transaction row in the data file."); }
                }
            }
        } catch (IOException e) { System.out.println("Could not read transactions: " + e.getMessage()); }
    }

    private static void saveTransactions() {
        try {
            Files.createDirectories(FILE.getParent());
            List<String> lines = new ArrayList<>();
            for (Transaction t : TRANSACTIONS)
                lines.add(csv(t.date.toString()) + "," + csv(t.type) + "," + csv(t.amount.toPlainString()) + "," + csv(t.category) + "," + csv(t.note));
            Files.write(FILE, lines, StandardCharsets.UTF_8);
        } catch (IOException e) { System.out.println("Could not save transactions: " + e.getMessage()); }
    }

    private static List<String> parseCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') { value.append('"'); i++; }
                else quoted = !quoted;
            } else if (c == ',' && !quoted) { fields.add(value.toString()); value.setLength(0); }
            else value.append(c);
        }
        fields.add(value.toString());
        return fields;
    }

    private static String ask(String prompt) { System.out.print(prompt); return INPUT.nextLine(); }

    private static class Transaction {
        private final String type;
        private final LocalDate date;
        private final BigDecimal amount;
        private final String category;
        private final String note;
        private Transaction(String type, LocalDate date, BigDecimal amount, String category, String note) {
            this.type = type; this.date = date; this.amount = amount; this.category = category; this.note = note;
        }
    }
}
