/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.impl.rate.swap;

import static com.opengamma.strata.basics.currency.Currency.GBP;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.currency.Payment;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.date.DayCounts;
import com.opengamma.strata.finance.rate.swap.KnownAmountPaymentPeriod;
import com.opengamma.strata.market.curve.ConstantNodalCurve;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.Curves;
import com.opengamma.strata.market.explain.ExplainKey;
import com.opengamma.strata.market.explain.ExplainMap;
import com.opengamma.strata.market.explain.ExplainMapBuilder;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;
import com.opengamma.strata.market.sensitivity.ZeroRateSensitivity;
import com.opengamma.strata.market.value.DiscountFactors;
import com.opengamma.strata.market.value.SimpleDiscountFactors;
import com.opengamma.strata.pricer.rate.RatesProvider;
import com.opengamma.strata.pricer.rate.SimpleRatesProvider;

/**
 * Test {@link DiscountingKnownAmountPaymentPeriodPricer}
 */
@Test
public class DiscountingKnownAmountPaymentPeriodPricerTest {

  private static final DiscountingKnownAmountPaymentPeriodPricer PRICER = DiscountingKnownAmountPaymentPeriodPricer.DEFAULT;
  private static final LocalDate VAL_DATE = LocalDate.of(2014, 1, 22);
  private static final DayCount DAY_COUNT = DayCounts.ACT_360;
  private static final LocalDate DATE_1 = LocalDate.of(2014, 1, 24);
  private static final LocalDate DATE_2 = LocalDate.of(2014, 4, 25);
  private static final LocalDate DATE_2U = LocalDate.of(2014, 4, 24);
  private static final double AMOUNT_1000 = 1000d;
  private static final CurrencyAmount AMOUNT_GBP1000 = CurrencyAmount.of(GBP, 1000);
  private static final LocalDate PAYMENT_DATE = LocalDate.of(2014, 4, 26);
  private static final double DISCOUNT_FACTOR = 0.976d;
  private static final double TOLERANCE_PV = 1E-7;

  private static final Payment PAYMENT = Payment.of(AMOUNT_GBP1000, PAYMENT_DATE);
  private static final Payment PAYMENT_PAST = Payment.of(AMOUNT_GBP1000, VAL_DATE.minusDays(1));

  private static final KnownAmountPaymentPeriod PERIOD = KnownAmountPaymentPeriod.builder()
      .payment(PAYMENT)
      .startDate(DATE_1)
      .endDate(DATE_2)
      .unadjustedEndDate(DATE_2U)
      .build();
  private static final KnownAmountPaymentPeriod PERIOD_PAST = KnownAmountPaymentPeriod.builder()
      .payment(PAYMENT_PAST)
      .startDate(DATE_1)
      .endDate(DATE_2)
      .build();

  //-------------------------------------------------------------------------
  public void test_presentValue() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    double pvExpected = AMOUNT_1000 * DISCOUNT_FACTOR;
    double pvComputed = PRICER.presentValue(PERIOD, prov);
    assertEquals(pvComputed, pvExpected, TOLERANCE_PV);
  }

  public void test_presentValue_inPast() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    double pvComputed = PRICER.presentValue(PERIOD_PAST, prov);
    assertEquals(pvComputed, 0, TOLERANCE_PV);
  }

  //-------------------------------------------------------------------------
  public void test_futureValue() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    double fvExpected = AMOUNT_1000;
    double fvComputed = PRICER.futureValue(PERIOD, prov);
    assertEquals(fvComputed, fvExpected, TOLERANCE_PV);
  }

  public void test_futureValue_inPast() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    double fvComputed = PRICER.futureValue(PERIOD_PAST, prov);
    assertEquals(fvComputed, 0, TOLERANCE_PV);
  }

  //-------------------------------------------------------------------------
  public void test_presentValueSensitivity() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    PointSensitivities point = PRICER.presentValueSensitivity(PERIOD, prov).build();
    double relativeYearFraction = DAY_COUNT.relativeYearFraction(VAL_DATE, PAYMENT_DATE);
    double expected = -DISCOUNT_FACTOR * relativeYearFraction * AMOUNT_1000;
    ZeroRateSensitivity actual = (ZeroRateSensitivity) point.getSensitivities().get(0);
    assertEquals(actual.getCurrency(), GBP);
    assertEquals(actual.getCurveCurrency(), GBP);
    assertEquals(actual.getDate(), PAYMENT_DATE);
    assertEquals(actual.getSensitivity(), expected, AMOUNT_1000 * TOLERANCE_PV);
  }

  public void test_presentValueSensitivity_inPast() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    PointSensitivities computed = PRICER.presentValueSensitivity(PERIOD_PAST, prov)
        .build();
    assertEquals(computed, PointSensitivities.empty());
  }

  //-------------------------------------------------------------------------
  public void test_futureValueSensitivity() {
    SimpleRatesProvider prov = createProvider(VAL_DATE);

    assertEquals(PRICER.futureValueSensitivity(PERIOD, prov), PointSensitivityBuilder.none());
  }

  //-------------------------------------------------------------------------
  public void test_accruedInterest() {
    LocalDate valDate = PERIOD.getStartDate().plusDays(7);
    SimpleRatesProvider prov = createProvider(valDate);

    double expected = AMOUNT_1000 * (7d / (7 + 28 + 31 + 25));
    double computed = PRICER.accruedInterest(PERIOD, prov);
    assertEquals(computed, expected, TOLERANCE_PV);
  }

  public void test_accruedInterest_valDateBeforePeriod() {
    SimpleRatesProvider prov = createProvider(PERIOD.getStartDate());

    double computed = PRICER.accruedInterest(PERIOD, prov);
    assertEquals(computed, 0, TOLERANCE_PV);
  }

  public void test_accruedInterest_valDateAfterPeriod() {
    SimpleRatesProvider prov = createProvider(PERIOD.getEndDate().plusDays(1));

    double computed = PRICER.accruedInterest(PERIOD, prov);
    assertEquals(computed, 0, TOLERANCE_PV);
  }

  //-------------------------------------------------------------------------
  public void test_explainPresentValue() {
    RatesProvider prov = createProvider(VAL_DATE);

    ExplainMapBuilder builder = ExplainMap.builder();
    PRICER.explainPresentValue(PERIOD, prov, builder);
    ExplainMap explain = builder.build();

    assertEquals(explain.get(ExplainKey.ENTRY_TYPE).get(), "KnownAmountPaymentPeriod");
    assertEquals(explain.get(ExplainKey.PAYMENT_DATE).get(), PERIOD.getPaymentDate());
    assertEquals(explain.get(ExplainKey.PAYMENT_CURRENCY).get(), PERIOD.getCurrency());
    assertEquals(explain.get(ExplainKey.DISCOUNT_FACTOR).get(), DISCOUNT_FACTOR, TOLERANCE_PV);

    int daysBetween = (int) DAYS.between(DATE_1, DATE_2);
    assertEquals(explain.get(ExplainKey.START_DATE).get(), PERIOD.getStartDate());
    assertEquals(explain.get(ExplainKey.UNADJUSTED_START_DATE).get(), PERIOD.getUnadjustedStartDate());
    assertEquals(explain.get(ExplainKey.END_DATE).get(), PERIOD.getEndDate());
    assertEquals(explain.get(ExplainKey.UNADJUSTED_END_DATE).get(), PERIOD.getUnadjustedEndDate());
    assertEquals(explain.get(ExplainKey.ACCRUAL_DAYS).get(), (Integer) daysBetween);

    assertEquals(explain.get(ExplainKey.FUTURE_VALUE).get().getCurrency(), PERIOD.getCurrency());
    assertEquals(explain.get(ExplainKey.FUTURE_VALUE).get().getAmount(), AMOUNT_1000, TOLERANCE_PV);
    assertEquals(explain.get(ExplainKey.PRESENT_VALUE).get().getCurrency(), PERIOD.getCurrency());
    assertEquals(explain.get(ExplainKey.PRESENT_VALUE).get().getAmount(), AMOUNT_1000 * DISCOUNT_FACTOR, TOLERANCE_PV);
  }

  public void test_explainPresentValue_inPast() {
    RatesProvider prov = createProvider(VAL_DATE);

    ExplainMapBuilder builder = ExplainMap.builder();
    PRICER.explainPresentValue(PERIOD_PAST, prov, builder);
    ExplainMap explain = builder.build();

    assertEquals(explain.get(ExplainKey.ENTRY_TYPE).get(), "KnownAmountPaymentPeriod");
    assertEquals(explain.get(ExplainKey.PAYMENT_DATE).get(), PERIOD_PAST.getPaymentDate());
    assertEquals(explain.get(ExplainKey.PAYMENT_CURRENCY).get(), PERIOD_PAST.getCurrency());

    int daysBetween = (int) DAYS.between(DATE_1, DATE_2);
    assertEquals(explain.get(ExplainKey.START_DATE).get(), PERIOD_PAST.getStartDate());
    assertEquals(explain.get(ExplainKey.UNADJUSTED_START_DATE).get(), PERIOD_PAST.getUnadjustedStartDate());
    assertEquals(explain.get(ExplainKey.END_DATE).get(), PERIOD_PAST.getEndDate());
    assertEquals(explain.get(ExplainKey.UNADJUSTED_END_DATE).get(), PERIOD_PAST.getUnadjustedEndDate());
    assertEquals(explain.get(ExplainKey.ACCRUAL_DAYS).get(), (Integer) daysBetween);

    assertEquals(explain.get(ExplainKey.FUTURE_VALUE).get().getCurrency(), PERIOD_PAST.getCurrency());
    assertEquals(explain.get(ExplainKey.FUTURE_VALUE).get().getAmount(), 0, TOLERANCE_PV);
    assertEquals(explain.get(ExplainKey.PRESENT_VALUE).get().getCurrency(), PERIOD_PAST.getCurrency());
    assertEquals(explain.get(ExplainKey.PRESENT_VALUE).get().getAmount(), 0 * DISCOUNT_FACTOR, TOLERANCE_PV);
  }

  //-------------------------------------------------------------------------
  // creates a simple provider
  private SimpleRatesProvider createProvider(LocalDate valDate) {
    Curve curve = ConstantNodalCurve.of(Curves.discountFactors("Test", DAY_COUNT), DISCOUNT_FACTOR);
    DiscountFactors df = SimpleDiscountFactors.of(GBP, valDate, curve);
    SimpleRatesProvider prov = new SimpleRatesProvider(valDate);
    prov.setDayCount(DAY_COUNT);
    prov.setDiscountFactors(df);
    return prov;
  }

}
