/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.rate.swaption;

import java.time.LocalDate;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.MultiCurrencyAmount;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.finance.rate.swaption.Swaption;
import com.opengamma.strata.finance.rate.swaption.SwaptionTrade;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.sensitivity.SwaptionSensitivity;
import com.opengamma.strata.pricer.DiscountingPaymentPricer;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Pricer for swaption trade with physical settlement in a normal model on the swap rate.
 * <p>
 * The swap underlying the swaption should have a fixed leg on which the forward rate is computed. The underlying swap
 * should be single currency.
 * <p>
 * The volatility parameters are not adjusted for the underlying swap conventions. The volatilities from the provider
 * are taken as such.
 * <p>
 * The present value and sensitivities of the premium, if in the future, are also taken into account.
 */
public class NormalSwaptionPhysicalTradePricerBeta {

  /**
   * Default implementation.
   */
  public static final NormalSwaptionPhysicalTradePricerBeta DEFAULT = new NormalSwaptionPhysicalTradePricerBeta();

  /** Pricer for {@link Swaption}. */
  private static final NormalSwaptionPhysicalProductPricerBeta PRICER_PRODUCT = NormalSwaptionPhysicalProductPricerBeta.DEFAULT;
  /** Pricer for {@link Payment} which is used to described the premium. **/
  private static final DiscountingPaymentPricer PRICER_PREMIUM = DiscountingPaymentPricer.DEFAULT;
  
  /**
   * Calculates the present value of the swaption trade.
   * <p>
   * The result is expressed using the currency of the swapion.
   * 
   * @param tradeSwaption  the swaption trade to price
   * @param rates  the rates provider
   * @param volatilities  the normal volatility parameters
   * @return the present value of the swap product
   */
  public CurrencyAmount presentValue(SwaptionTrade tradeSwaption, RatesProvider rates,
      NormalVolatilitySwaptionProvider volatilities) {
    Swaption product = tradeSwaption.getProduct();
    CurrencyAmount pvProduct = PRICER_PRODUCT.presentValue(product, rates, volatilities);
    Payment premium = tradeSwaption.getPremium();
    CurrencyAmount pvPremium = PRICER_PREMIUM.presentValue(premium, rates);
    return pvProduct.plus(pvPremium);
  }

  //-------------------------------------------------------------------------
  /**
   * Computes the currency exposure of the swaption trade
   * 
   * @param tradeSwaption  the swaption trade to price
   * @param rates  the rates provider
   * @param volatilities  the normal volatility parameters
   * @return the present value of the swaption product
   */
  public MultiCurrencyAmount currencyExposure(SwaptionTrade tradeSwaption, RatesProvider rates, 
      NormalVolatilitySwaptionProvider volatilities) {
    return MultiCurrencyAmount.of(presentValue(tradeSwaption, rates, volatilities));
  }
  
  /**
   * Calculates the current of the swaption trade.
   * <p>
   * Only the premium is contributing to the current cash for non-cash settle swaptions.
   * 
   * @param tradeSwaption  the swaption trade to price
   * @param valuationDate  the valuation date
   * @return the current cash amount
   */
  public CurrencyAmount currentCash(SwaptionTrade tradeSwaption, LocalDate valuationDate) {
    Payment premium = tradeSwaption.getPremium();
    if (premium.getDate().equals(valuationDate)) {
      return CurrencyAmount.of(premium.getCurrency(), premium.getAmount());
    }
    return CurrencyAmount.of(premium.getCurrency(), 0.0);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the present value sensitivity of the swaption product.
   * <p>
   * The present value sensitivity of the product is the sensitivity of the present value to
   * the underlying curves.
   * 
   * @param tradeSwaption  the swaption trade
   * @param rates  the rates provider
   * @param volatilities  the normal volatility provider
   * @return the present value curve sensitivity of the swap trade
   */
  public PointSensitivityBuilder presentValueSensitivityStickyStrike(SwaptionTrade tradeSwaption, RatesProvider rates, 
      NormalVolatilitySwaptionProvider volatilities) {
    Swaption product = tradeSwaption.getProduct();
    PointSensitivityBuilder pvcsProduct = PRICER_PRODUCT.presentValueSensitivityStickyStrike(product, rates, volatilities);
    Payment premium = tradeSwaption.getPremium();
    PointSensitivityBuilder pvcsPremium = PRICER_PREMIUM.presentValueSensitivity(premium, rates);
    return pvcsProduct.combinedWith(pvcsPremium);
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the present value sensitivity to the implied volatility of the swaption trade.
   * <p>
   * The sensitivity to the implied normal volatility is also called normal vega.
   * 
   * @param tradeSwaption  the swaption trade
   * @param rates  the rates provider
   * @param volatilities  the normal volatility provider
   * @return the point sensitivity to the normal volatility
   */
  public SwaptionSensitivity presentValueSensitivityNormalVolatility(SwaptionTrade tradeSwaption, RatesProvider rates, 
      NormalVolatilitySwaptionProvider volatilities) {
    Swaption product = tradeSwaption.getProduct();
    return PRICER_PRODUCT.presentValueSensitivityNormalVolatility(product, rates, volatilities);
  }

}
