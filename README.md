# Personal Finance Tracker

**Student:** Sanskar Thakur  
**Language:** Java (JDK 11 or newer)  
**Interface:** Console  
**Storage:** Local CSV file (`data/transactions.csv`)

## App kya karte ho ?

Income aur expense transactions record karke month ka total income, total expense aur net cash flow dikhati hai. Expense ko category se filter kar sakte hain, aur transactions ko spreadsheet-friendly CSV mein export kar sakte hain.

## Kaise chalayein?

Project folder ke andar terminal kholkar:

```text
javac -encoding UTF-8 -d out src/FinanceTracker.java
java -cp out FinanceTracker
```

Run karte waqt project folder ko current folder rakhein; isi jagah `data` folder banega.

## Monthly summary ka hisaab

`Net cash flow = total income - total expenses`

Amount ke liye `BigDecimal` use hota hai, jo currency calculations mein `double` ke muqable rounding issues se bachata hai. Month `YYYY-MM` format mein aur transaction date `YYYY-MM-DD` format mein enter karein.

## Features

- Income ya expense add karna
- Date, category aur optional note save karna
- Month-wise totals aur expense category totals dekhna
- Category ke naam se transactions filter karna
- CSV export: `data/finance-summary.csv`
- App restart ke baad transaction data file se load hota hai

## Concepts

`BigDecimal`, `LocalDate`, `YearMonth`, collections, file handling aur CSV escaping.