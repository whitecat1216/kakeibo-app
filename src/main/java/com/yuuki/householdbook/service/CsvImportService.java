package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.entity.PaymentSource;
import com.yuuki.householdbook.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvImportService {

    private final AccountRepository accountRepository;
    private final CategoryService categoryService;
    private final PaymentSourceService paymentSourceService;

    public CsvImportService(AccountRepository accountRepository,
                            CategoryService categoryService,
                            PaymentSourceService paymentSourceService) {
        this.accountRepository = accountRepository;
        this.categoryService = categoryService;
        this.paymentSourceService = paymentSourceService;
    }

    public CsvImportResult importCsv(AppUser user, MultipartFile file, boolean skipDuplicates) throws IOException {
        CsvImportResult result = new CsvImportResult();
        if (file == null || file.isEmpty()) {
            result.getErrors().add("ファイルが選択されていません。");
            return result;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.getErrors().add("CSVが空です。");
                return result;
            }
            headerLine = removeBom(headerLine);
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> index = buildIndex(headers);

            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                result.total++;
                try {
                    List<String> cols = parseCsvLine(line);
                    LocalDate date = LocalDate.parse(get(cols, index, "date"));
                    String type = normalizeType(get(cols, index, "type"));
                    Integer amount = parseAmount(get(cols, index, "amount"));
                    String categoryName = trimToNull(get(cols, index, "category"));
                    String sourceName = trimToNull(get(cols, index, "source"));
                    String item = trimToNull(get(cols, index, "item"));
                    String memo = trimToNull(get(cols, index, "memo"));

                    Category category = categoryService.findOrCreateByName(user, categoryName, type);
                    PaymentSource source = paymentSourceService.findOrCreateByName(user, sourceName);

                    boolean duplicated = accountRepository.existsDuplicate(
                            user, date, type, amount, item, memo,
                            category != null ? category.getId() : null,
                            source != null ? source.getId() : null
                    );
                    if (skipDuplicates && duplicated) {
                        result.skipped++;
                        continue;
                    }

                    Account account = new Account();
                    account.setUser(user);
                    account.setDate(date);
                    account.setType(type);
                    account.setCategory(category);
                    account.setSource(source);
                    account.setItem(item);
                    account.setAmount(amount);
                    account.setMemo(memo);
                    accountRepository.save(account);
                    result.imported++;
                } catch (Exception e) {
                    result.errors.add(lineNo + "行目: " + e.getMessage());
                }
            }
        }
        return result;
    }

    private static String removeBom(String line) {
        if (line != null && !line.isEmpty() && line.charAt(0) == '\uFEFF') {
            return line.substring(1);
        }
        return line;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        return v.isEmpty() ? null : v;
    }

    private static Integer parseAmount(String raw) {
        String v = raw.replace(",", "").replace("円", "").trim();
        if (v.isEmpty()) {
            throw new IllegalArgumentException("金額が空です。");
        }
        return Integer.parseInt(v);
    }

    private static String normalizeType(String raw) {
        String v = raw.trim().toLowerCase();
        if ("income".equals(v) || "収入".equals(v)) return "income";
        if ("expense".equals(v) || "支出".equals(v)) return "expense";
        throw new IllegalArgumentException("タイプが不正です: " + raw);
    }

    private static Map<String, Integer> buildIndex(List<String> headers) {
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).trim().toLowerCase();
            if ("日付".equals(h) || "date".equals(h)) idx.put("date", i);
            if ("タイプ".equals(h) || "type".equals(h) || "収支".equals(h)) idx.put("type", i);
            if ("カテゴリ".equals(h) || "category".equals(h)) idx.put("category", i);
            if ("支払方法".equals(h) || "source".equals(h) || "paymentsource".equals(h)) idx.put("source", i);
            if ("項目".equals(h) || "item".equals(h)) idx.put("item", i);
            if ("金額".equals(h) || "amount".equals(h)) idx.put("amount", i);
            if ("メモ".equals(h) || "memo".equals(h)) idx.put("memo", i);
        }
        if (!idx.containsKey("date") || !idx.containsKey("type") || !idx.containsKey("amount")) {
            throw new IllegalArgumentException("必須ヘッダー（日付/タイプ/金額）が不足しています。");
        }
        return idx;
    }

    private static String get(List<String> cols, Map<String, Integer> index, String key) {
        Integer i = index.get(key);
        if (i == null || i >= cols.size()) return "";
        return cols.get(i);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        values.add(current.toString());
        return values;
    }

    public static class CsvImportResult {
        private int total;
        private int imported;
        private int skipped;
        private final List<String> errors = new ArrayList<>();

        public int getTotal() { return total; }
        public int getImported() { return imported; }
        public int getSkipped() { return skipped; }
        public List<String> getErrors() { return errors; }
    }
}
