package acadia.lwcardano.internalization.bybit;

import me.hysong.files.ConfigurationFile;

public class Market {

    public static double getCurrentPrice(ConfigurationFile cfg, String symbol) {
        return 1.0; // TODO
    }

    public static double calculateExchange(ConfigurationFile cfg, String symbol, boolean fromTether, double amount) {
        double exchangeRate = getCurrentPrice(cfg, symbol);

        if (fromTether) {
            return amount / exchangeRate; // Tether to Crypto (1k usd / 100k btcusd = 0.01 btc)
        } else {
            return exchangeRate / amount; // Crypto to Tether (100k btcusd / 0.01 btc = 1k usd)
        }
    }

}
