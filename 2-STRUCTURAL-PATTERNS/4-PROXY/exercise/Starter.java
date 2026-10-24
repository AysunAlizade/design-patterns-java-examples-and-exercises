import java.util.HashMap;
import java.util.Map;

// Proxy Practice Exercise — starter code. ExchangeRateService connects to a slow,
// paid API, checks the user's plan, and caches rates, all in one class. Split it
// into one real API client and separate proxies for each concern.
// See README.md in this folder for the full task and requirements.

class ExchangeRateService {
    private static final Map<String, Double> API_RATES = Map.of(
            "USD->EUR", 0.92, "EUR->USD", 1.09,
            "USD->GBP", 0.79, "GBP->USD", 1.27,
            "USD->JPY", 151.40, "EUR->JPY", 164.20);

    private final Map<String, Double> cache = new HashMap<>();
    private int apiCalls = 0;

    public ExchangeRateService() {
        // expensive: runs at application startup, even if nobody ever converts anything
        System.out.println("Connecting and authenticating with RatesAPI (slow)...");
    }

    public double getRate(String plan, String from, String to) {
        // concern 1: plan restriction. FREE users may only convert between USD, EUR, GBP
        if (plan.equals("FREE") && !(isMajor(from) && isMajor(to))) {
            throw new IllegalStateException("Upgrade to PRO to convert " + from + " -> " + to);
        }

        // concern 2: caching
        String pair = from + "->" + to;
        if (cache.containsKey(pair)) {
            return cache.get(pair);
        }

        // concern 3: the real work, a slow API call that costs money
        apiCalls++;
        Double rate = API_RATES.get(pair);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown currency pair: " + pair);
        }
        System.out.println("RatesAPI call #" + apiCalls + ": " + pair + " = " + rate + " ($0.01 charged)");
        cache.put(pair, rate);
        return rate;
    }

    private boolean isMajor(String currency) {
        return currency.equals("USD") || currency.equals("EUR") || currency.equals("GBP");
    }
}

class CurrencyConverter {
    private final ExchangeRateService rates;
    private final String plan;

    public CurrencyConverter(ExchangeRateService rates, String plan) {
        this.rates = rates;
        this.plan = plan;
    }

    public void convert(double amount, String from, String to) {
        try {
            double result = amount * rates.getRate(plan, from, to);
            System.out.printf("%.2f %s = %.2f %s%n", amount, from, result, to);
        } catch (IllegalStateException e) {
            System.out.println("Refused: " + e.getMessage());
        }
    }
}

public class Starter {
    public static void main(String[] args) {
        System.out.println("App started");
        ExchangeRateService rates = new ExchangeRateService();

        CurrencyConverter pro = new CurrencyConverter(rates, "PRO");
        CurrencyConverter free = new CurrencyConverter(rates, "FREE");

        pro.convert(100, "USD", "EUR");
        free.convert(50, "USD", "EUR");
        pro.convert(100, "USD", "JPY");
        free.convert(100, "USD", "JPY");
    }
}
