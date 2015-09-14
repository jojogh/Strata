/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.rate.fra;

import static com.opengamma.strata.basics.date.DayCounts.ACT_360;
import static com.opengamma.strata.collect.TestHelper.coverPrivateConstructor;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.currency.Currency;
import com.opengamma.strata.basics.currency.CurrencyAmount;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.collect.CollectProjectAssertions;
import com.opengamma.strata.engine.calculation.function.CalculationSingleFunction;
import com.opengamma.strata.engine.calculation.function.result.FxConvertibleList;
import com.opengamma.strata.engine.config.FunctionConfig;
import com.opengamma.strata.engine.config.Measure;
import com.opengamma.strata.engine.config.pricing.FunctionGroup;
import com.opengamma.strata.engine.marketdata.FunctionRequirements;
import com.opengamma.strata.finance.rate.fra.FraTrade;
import com.opengamma.strata.function.marketdata.curve.MarketDataMap;
import com.opengamma.strata.market.curve.ConstantNodalCurve;
import com.opengamma.strata.market.curve.Curves;
import com.opengamma.strata.market.key.DiscountFactorsKey;
import com.opengamma.strata.market.key.IborIndexRatesKey;
import com.opengamma.strata.market.key.IndexRateKey;
import com.opengamma.strata.market.value.DiscountFactors;
import com.opengamma.strata.market.value.SimpleDiscountFactors;
import com.opengamma.strata.pricer.rate.fra.FraDummyData;

/**
 * Test {@link FraFunctionGroups}.
 */
@Test
public class FraFunctionGroupsTest {

  private static final FraTrade FRA_TRADE = FraDummyData.FRA_TRADE;

  public void test_discounting() {
    FunctionGroup<FraTrade> test = FraFunctionGroups.discounting();
    assertThat(test.configuredMeasures(FRA_TRADE)).contains(
        Measure.PAR_RATE,
        Measure.PAR_SPREAD,
        Measure.PRESENT_VALUE,
        Measure.EXPLAIN_PRESENT_VALUE,
        Measure.PV01,
        Measure.BUCKETED_PV01,
        Measure.BUCKETED_GAMMA_PV01);
  }

  public void test_presentValue() {
    Currency ccy = FRA_TRADE.getProduct().getCurrency();
    IborIndex index = FRA_TRADE.getProduct().getIndex();
    LocalDate valDate = FRA_TRADE.getProduct().getEndDate().plusDays(7);

    FunctionConfig<FraTrade> config = FraFunctionGroups.discounting().functionConfig(FRA_TRADE, Measure.PRESENT_VALUE).get();
    CalculationSingleFunction<FraTrade, ?> function = config.createFunction();
    FunctionRequirements reqs = function.requirements(FRA_TRADE);
    assertThat(reqs.getOutputCurrencies()).containsOnly(ccy);
    assertThat(reqs.getSingleValueRequirements()).isEqualTo(
        ImmutableSet.of(IborIndexRatesKey.of(index), DiscountFactorsKey.of(ccy)));
    assertThat(reqs.getTimeSeriesRequirements()).isEqualTo(ImmutableSet.of(IndexRateKey.of(index)));
    CollectProjectAssertions.assertThat(function.defaultReportingCurrency(FRA_TRADE)).hasValue(ccy);
    DiscountFactors df = SimpleDiscountFactors.of(
        ccy, valDate, ConstantNodalCurve.of(Curves.discountFactors("Test", ACT_360), 0.99));
    MarketDataMap md = new MarketDataMap(valDate, ImmutableMap.of(DiscountFactorsKey.of(ccy), df), ImmutableMap.of());
    assertThat(function.execute(FRA_TRADE, md)).isEqualTo(FxConvertibleList.of(ImmutableList.of(CurrencyAmount.of(ccy, 0d))));
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    coverPrivateConstructor(FraFunctionGroups.class);
  }

}
