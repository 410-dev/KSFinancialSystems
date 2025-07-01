package upbit;

import lombok.Getter;
import org.kynesys.kstraderapi.v1.misc.KSCurrencyUnitConverter;

@Getter
public class USD2KRW implements KSCurrencyUnitConverter {
    private final String from = "USD";
    private final String to = "KRW";

    @Override
    public Double apply(Double price) {
        return price * 1400.0; // TODO Sync with exchange rate center, or use price of KRWUSDT
    }
}
