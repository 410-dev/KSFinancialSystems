package upbit;

import lombok.Getter;
import org.kynesys.kstraderapi.v1.misc.CurrencyUnitConverter;

@Getter
public class KRW2USD implements CurrencyUnitConverter {
    private final String from = "KRW";
    private final String to = "USD";

    @Override
    public Double apply(Double price) {
        return price / 1400.0; // TODO Sync with exchange rate center, or use price of KRWUSDT
    }
}
