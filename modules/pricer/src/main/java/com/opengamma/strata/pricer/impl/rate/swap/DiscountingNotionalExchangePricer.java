/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.rate.swap;

import java.time.LocalDate;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.finance.rate.swap.NotionalExchange;
import com.opengamma.strata.market.explain.ExplainKey;
import com.opengamma.strata.market.explain.ExplainMapBuilder;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.value.DiscountFactors;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.rate.swap.PaymentEventPricer;

/**
 * Pricer implementation for the exchange of notionals.
 * <p>
 * The notional exchange is priced by discounting the value of the exchange.
 */
public class DiscountingNotionalExchangePricer
    implements PaymentEventPricer<NotionalExchange> {

  /**
   * Default implementation.
   */
  public static final DiscountingNotionalExchangePricer DEFAULT = new DiscountingNotionalExchangePricer();

  /**
   * Creates an instance.
   */
  public DiscountingNotionalExchangePricer() {
  }

  //-------------------------------------------------------------------------
  @Override
  public double presentValue(NotionalExchange event, RatesProvider provider) {
    // futureValue * discountFactor
    double df = provider.discountFactor(event.getCurrency(), event.getPaymentDate());
    return futureValue(event, provider) * df;
  }

  @Override
  public PointSensitivityBuilder presentValueSensitivity(NotionalExchange event, RatesProvider provider) {
    DiscountFactors discountFactors = provider.discountFactors(event.getCurrency());
    return discountFactors.zeroRatePointSensitivity(event.getPaymentDate())
        .multipliedBy(event.getPaymentAmount().getAmount());
  }

  //-------------------------------------------------------------------------
  @Override
  public double futureValue(NotionalExchange event, RatesProvider provider) {
    // paymentAmount
    return event.getPaymentAmount().getAmount();
  }

  @Override
  public PointSensitivityBuilder futureValueSensitivity(NotionalExchange event, RatesProvider provider) {
    return PointSensitivityBuilder.none();
  }

  //-------------------------------------------------------------------------
  @Override
  public void explainPresentValue(NotionalExchange event, RatesProvider provider, ExplainMapBuilder builder) {
    Currency currency = event.getCurrency();
    LocalDate paymentDate = event.getPaymentDate();
    
    builder.put(ExplainKey.ENTRY_TYPE, "NotionalExchange");
    builder.put(ExplainKey.PAYMENT_DATE, paymentDate);
    builder.put(ExplainKey.PAYMENT_CURRENCY, currency);
    builder.put(ExplainKey.TRADE_NOTIONAL, event.getPaymentAmount());
    if (paymentDate.isBefore(provider.getValuationDate())) {
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.zero(currency));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.zero(currency));
    } else {
      builder.put(ExplainKey.DISCOUNT_FACTOR, provider.discountFactor(currency, paymentDate));
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.of(currency, futureValue(event, provider)));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.of(currency, presentValue(event, provider)));
    }
  }

}
