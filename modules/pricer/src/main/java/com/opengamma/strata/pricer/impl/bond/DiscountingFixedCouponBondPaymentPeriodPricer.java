/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.bond;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.finance.rate.bond.FixedCouponBondPaymentPeriod;
import com.opengamma.strata.market.explain.ExplainKey;
import com.opengamma.strata.market.explain.ExplainMapBuilder;
import com.opengamma.strata.market.sensitivity.IssuerCurveZeroRateSensitivity;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.sensitivity.ZeroRateSensitivity;
import com.opengamma.strata.market.value.CompoundedRateType;
import com.opengamma.strata.market.value.IssuerCurveDiscountFactors;

/**
 * Pricer implementation for bond payment periods based on a fixed coupon.
 * <p>
 * This pricer performs discounting of the fixed coupon payment.
 */
public class DiscountingFixedCouponBondPaymentPeriodPricer {

  /**
   * Default implementation.
   */
  public static final DiscountingFixedCouponBondPaymentPeriodPricer DEFAULT =
      new DiscountingFixedCouponBondPaymentPeriodPricer();

  /**
   * Creates an instance.
   */
  public DiscountingFixedCouponBondPaymentPeriodPricer() {
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the present value of a single fixed coupon payment period.
   * <p>
   * The amount is expressed in the currency of the period.
   * This returns the value of the period with discounting.
   * <p>
   * The payment date of the period should not be in the past.
   * The result of this method for payment dates in the past is undefined.
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @return the present value of the period
   */
  public double presentValue(FixedCouponBondPaymentPeriod period, IssuerCurveDiscountFactors discountFactors) {
    double df = discountFactors.discountFactor(period.getPaymentDate());
    return period.getFixedRate() * period.getNotional() * period.getYearFraction() * df;
  }

  /**
   * Calculates the present value of a single fixed coupon payment period with z-spread.
   * <p>
   * The z-spread is a parallel shift applied to continuously compounded rates or periodic compounded rates 
   * of the discounting curve. 
   * <p>
   * The amount is expressed in the currency of the period.
   * This returns the value of the period with discounting.
   * <p>
   * The payment date of the period should not be in the past.
   * The result of this method for payment dates in the past is undefined.
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @param zSpread  the z-spread
   * @param compoundedRateType  the spread is added to periodic compounded rates, or the spread is added to 
   * continuously compounded rates
   * @param periodPerYear  the number of periods per year
   * @return the present value of the period
   */
  public double presentValue(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors,
      double zSpread,
      CompoundedRateType compoundedRateType,
      int periodPerYear) {
    double df = discountFactors.getDiscountFactors()
        .discountFactorWithZSpread(period.getPaymentDate(), zSpread, compoundedRateType, periodPerYear);
    return period.getFixedRate() * period.getNotional() * period.getYearFraction() * df;
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the future value of a single fixed coupon payment period.
   * <p>
   * The amount is expressed in the currency of the period.
   * This returns the value of the period with discounting.
   * <p>
   * The payment date of the period should not be in the past.
   * The result of this method for payment dates in the past is undefined.
   * <p>
   * The future value is z-spread independent. 
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @return the present value of the period
   */
  public double futureValue(FixedCouponBondPaymentPeriod period, IssuerCurveDiscountFactors discountFactors) {
    return period.getFixedRate() * period.getNotional() * period.getYearFraction();
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the present value sensitivity of a single fixed coupon payment period.
   * <p>
   * The present value sensitivity of the period is the sensitivity of the present value to
   * the underlying curves.
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @return the present value curve sensitivity of the period
   */
  public IssuerCurveZeroRateSensitivity presentValueSensitivity(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors) {
    IssuerCurveZeroRateSensitivity dscSensi = discountFactors.zeroRatePointSensitivity(period.getPaymentDate());
    return dscSensi.multipliedBy(period.getFixedRate() * period.getNotional() * period.getYearFraction());
  }

  /**
   * Calculates the present value sensitivity of a single fixed coupon payment period with z-spread.
   * <p>
   * The z-spread is a parallel shift applied to continuously compounded rates or periodic compounded rates 
   * of the discounting curve. 
   * <p>
   * The present value sensitivity of the period is the sensitivity of the present value to
   * the underlying curves.
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @param zSpread  the z-spread
   * @param compoundedRateType  the spread is added to periodic compounded rates, or the spread is added to 
   * continuously compounded rates
   * @param periodPerYear  the number of periods per year
   * @return the present value curve sensitivity of the period
   */
  public IssuerCurveZeroRateSensitivity presentValueSensitivity(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors,
      double zSpread,
      CompoundedRateType compoundedRateType,
      int periodPerYear) {
    ZeroRateSensitivity zeroSensi = discountFactors.getDiscountFactors().zeroRatePointSensitivityWithZSpread(
        period.getPaymentDate(), zSpread, compoundedRateType, periodPerYear);
    IssuerCurveZeroRateSensitivity dscSensi =
        IssuerCurveZeroRateSensitivity.of(zeroSensi, discountFactors.getLegalEntityGroup());
    return dscSensi.multipliedBy(period.getFixedRate() * period.getNotional() * period.getYearFraction());
  }

  //-------------------------------------------------------------------------
  /**
   * Calculates the future value sensitivity of a single fixed coupon payment period.
   * <p>
   * The future value sensitivity of the period is the sensitivity of the future value to
   * the underlying curves.
   * <p>
   * The future value sensitivity is zero and z-spread independent for the fixed payment. 
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @return the future value curve sensitivity of the period
   */
  public PointSensitivityBuilder futureValueSensitivity(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors) {
    return PointSensitivityBuilder.none();
  }

  //-------------------------------------------------------------------------
  /**
   * Explains the present value of a single fixed coupon payment period.
   * <p>
   * This adds information to the {@link ExplainMapBuilder} to aid understanding of the calculation.
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @param builder  the builder to populate
   */
  public void explainPresentValue(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors,
      ExplainMapBuilder builder) {
    Currency currency = period.getCurrency();
    LocalDate paymentDate = period.getPaymentDate();

    builder.put(ExplainKey.ENTRY_TYPE, "FixedCouponBondPaymentPeriod");
    builder.put(ExplainKey.PAYMENT_DATE, paymentDate);
    builder.put(ExplainKey.PAYMENT_CURRENCY, currency);
    builder.put(ExplainKey.START_DATE, period.getStartDate());
    builder.put(ExplainKey.UNADJUSTED_START_DATE, period.getUnadjustedStartDate());
    builder.put(ExplainKey.END_DATE, period.getEndDate());
    builder.put(ExplainKey.ACCRUAL_DAYS, (int) DAYS.between(period.getStartDate(), period.getEndDate()));
    builder.put(ExplainKey.UNADJUSTED_END_DATE, period.getUnadjustedEndDate());
    if (paymentDate.isBefore(discountFactors.getValuationDate())) {
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.zero(currency));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.zero(currency));
    } else {
      builder.put(ExplainKey.DISCOUNT_FACTOR, discountFactors.discountFactor(paymentDate));
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.of(currency, futureValue(period, discountFactors)));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.of(currency, presentValue(period, discountFactors)));
    }
  }

  /**
   * Explains the present value of a single fixed coupon payment period with z-spread.
   * <p>
   * This adds information to the {@link ExplainMapBuilder} to aid understanding of the calculation.
   * <p>
   * The z-spread is a parallel shift applied to continuously compounded rates or periodic compounded rates 
   * of the discounting curve. 
   * 
   * @param period  the period to price
   * @param discountFactors  the discount factor provider
   * @param zSpread  the z-spread
   * @param compoundedRateType  the spread is added to periodic compounded rates, or the spread is added to 
   * continuously compounded rates
   * @param periodPerYear  the number of periods per year
   * @param builder  the builder to populate
   */
  public void explainPresentValue(
      FixedCouponBondPaymentPeriod period,
      IssuerCurveDiscountFactors discountFactors,
      ExplainMapBuilder builder,
      double zSpread,
      CompoundedRateType compoundedRateType,
      int periodPerYear) {
    Currency currency = period.getCurrency();
    LocalDate paymentDate = period.getPaymentDate();

    builder.put(ExplainKey.ENTRY_TYPE, "FixedCouponBondPaymentPeriod");
    builder.put(ExplainKey.PAYMENT_DATE, paymentDate);
    builder.put(ExplainKey.PAYMENT_CURRENCY, currency);
    builder.put(ExplainKey.START_DATE, period.getStartDate());
    builder.put(ExplainKey.UNADJUSTED_START_DATE, period.getUnadjustedStartDate());
    builder.put(ExplainKey.END_DATE, period.getEndDate());
    builder.put(ExplainKey.ACCRUAL_DAYS, (int) DAYS.between(period.getStartDate(), period.getEndDate()));
    builder.put(ExplainKey.UNADJUSTED_END_DATE, period.getUnadjustedEndDate());
    if (paymentDate.isBefore(discountFactors.getValuationDate())) {
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.zero(currency));
      builder.put(ExplainKey.PRESENT_VALUE, CurrencyAmount.zero(currency));
    } else {
      builder.put(ExplainKey.DISCOUNT_FACTOR, discountFactors.getDiscountFactors().discountFactorWithZSpread(
          paymentDate, zSpread, compoundedRateType, periodPerYear));
      builder.put(ExplainKey.FUTURE_VALUE, CurrencyAmount.of(currency, futureValue(period, discountFactors)));
      builder.put(ExplainKey.PRESENT_VALUE,
          CurrencyAmount
              .of(currency, presentValue(period, discountFactors, zSpread, compoundedRateType, periodPerYear)));
    }
  }
}
