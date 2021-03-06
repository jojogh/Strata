/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.rate.swap;

import java.time.LocalDate;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.finance.rate.swap.FxResetNotionalExchange;
import com.opengamma.strata.market.explain.ExplainKey;
import com.opengamma.strata.market.explain.ExplainMapBuilder;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.value.DiscountFactors;
import com.opengamma.strata.market.value.FxIndexRates;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.rate.swap.PaymentEventPricer;

/**
 * Pricer implementation for the exchange of FX reset notionals.
 * <p>
 * The FX reset notional exchange is priced by discounting the value of the exchange.
 * The value of the exchange is calculated by performing an FX conversion on the amount.
 */
public class DiscountingFxResetNotionalExchangePricer
    implements PaymentEventPricer<FxResetNotionalExchange> {

  /**
   * Default implementation.
   */
  public static final DiscountingFxResetNotionalExchangePricer DEFAULT =
      new DiscountingFxResetNotionalExchangePricer();

  /**
   * Creates an instance.
   */
  public DiscountingFxResetNotionalExchangePricer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public double presentValue(FxResetNotionalExchange event, RatesProvider provider) {
    // futureValue * discountFactor
    double df = provider.discountFactor(event.getCurrency(), event.getPaymentDate());
    return futureValue(event, provider) * df;
  }

  @Override
  public PointSensitivityBuilder presentValueSensitivity(FxResetNotionalExchange event, RatesProvider provider) {
    DiscountFactors discountFactors = provider.discountFactors(event.getCurrency());
    PointSensitivityBuilder sensiDsc = discountFactors.zeroRatePointSensitivity(event.getPaymentDate());
    sensiDsc = sensiDsc.multipliedBy(futureValue(event, provider));
    PointSensitivityBuilder sensiFx = futureValueSensitivity(event, provider);
    sensiFx = sensiFx.multipliedBy(discountFactors.discountFactor(event.getPaymentDate()));
    return sensiDsc.combinedWith(sensiFx);
  }

  //-------------------------------------------------------------------------
  @Override
  public double futureValue(FxResetNotionalExchange event, RatesProvider provider) {
    // notional * fxRate
    return event.getNotional() * fxRate(event, provider);
  }

  // obtains the FX rate
  private double fxRate(FxResetNotionalExchange event, RatesProvider provider) {
    FxIndexRates rates = provider.fxIndexRates(event.getIndex());
    return rates.rate(event.getReferenceCurrency(), event.getFixingDate());
  }

  @Override
  public PointSensitivityBuilder futureValueSensitivity(FxResetNotionalExchange event, RatesProvider provider) {
    FxIndexRates rates = provider.fxIndexRates(event.getIndex());
    return rates.ratePointSensitivity(event.getReferenceCurrency(), event.getFixingDate())
        .multipliedBy(event.getNotional());
  }

  //-------------------------------------------------------------------------
  @Override
  public void explainPresentValue(FxResetNotionalExchange event, RatesProvider provider, ExplainMapBuilder builder) {
    Currency currency = event.getCurrency();
    LocalDate paymentDate = event.getPaymentDate();

    builder.put(ExplainKey.ENTRY_TYPE, "FxResetNotionalExchange");
    builder.put(ExplainKey.PAYMENT_DATE, paymentDate);
    builder.put(ExplainKey.PAYMENT_CURRENCY, currency);
    builder.put(ExplainKey.TRADE_NOTIONAL, event.getNotionalAmount());
    if (paymentDate.isBefore(provider.getValuationDate())) {
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.zero(currency));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.zero(currency));
    } else {
      builder.addListEntry(ExplainKey.OBSERVATIONS, child -> {
        child.put(ExplainKey.ENTRY_TYPE, "FxObservation");
        child.put(ExplainKey.INDEX, event.getIndex());
        child.put(ExplainKey.FIXING_DATE, event.getFixingDate());
        child.put(ExplainKey.INDEX_VALUE, fxRate(event, provider));
      });
      builder.put(ExplainKey.DISCOUNT_FACTOR, provider.discountFactor(currency, paymentDate));
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.of(currency, futureValue(event, provider)));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.of(currency, presentValue(event, provider)));
    }
  }

}
