import java.util.HashMap;
import java.util.Map;

interface ExchangeRates {
    double getRate(String from, String to);
}

class RatesApiClient implements ExchangeRates {
    public double getRate(String from, String to) {
        System.out.println("Connecting to API...");

        if (from.equals("USD") && to.equals("EUR")) {
            System.out.println("API call: $0.01");
            return 0.92;
        }

        if (from.equals("USD") && to.equals("JPY")) {
            System.out.println("API call: $0.01");
            return 151.40;
        }

        throw new IllegalArgumentException("Unknown currency");
    }
}

class LazyProxy implements ExchangeRates {
    private ExchangeRates api;

    public double getRate(String from, String to) {
        if (api == null) {
            api = new RatesApiClient();
        }
        return api.getRate(from, to);
    }
}

class CacheProxy implements ExchangeRates {
    private ExchangeRates api;
    private Map<String, Double> cache = new HashMap<>();

    public CacheProxy(ExchangeRates api) {
        this.api = api;
    }

    public double getRate(String from, String to) {
        String key = from + to;

        if (!cache.containsKey(key)) {
            cache.put(key, api.getRate(from, to));
        }

        return cache.get(key);
    }
}

class PlanProxy implements ExchangeRates {
    private ExchangeRates api;
    private String plan;

    public PlanProxy(ExchangeRates api, String plan) {
        this.api = api;
        this.plan = plan;
    }

    public double getRate(String from, String to) {
        if (plan.equals("FREE") && (to.equals("JPY") || from.equals("JPY"))) {
            throw new IllegalStateException("Upgrade to PRO");
        }

        return api.getRate(from, to);
    }
}

class CurrencyConverter {
    private ExchangeRates rates;

    public CurrencyConverter(ExchangeRates rates) {
        this.rates = rates;
    }

    public void convert(double amount, String from, String to) {
        try {
            System.out.println(amount * rates.getRate(from, to) + " " + to);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}

public class Starter {
    public static void main(String[] args) {
        System.out.println("App started");

        ExchangeRates cache = new CacheProxy(new LazyProxy());

        CurrencyConverter pro = new CurrencyConverter(new PlanProxy(cache, "PRO"));

        CurrencyConverter free = new CurrencyConverter(new PlanProxy(cache, "FREE"));

        pro.convert(100, "USD", "EUR");
        free.convert(50, "USD", "EUR");

        pro.convert(100, "USD", "JPY");
        free.convert(100, "USD", "JPY");
    }
}
