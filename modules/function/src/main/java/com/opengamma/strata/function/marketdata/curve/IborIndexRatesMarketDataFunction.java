/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.marketdata.curve;

import java.time.LocalDate;

import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.engine.marketdata.MarketDataLookup;
import com.opengamma.strata.engine.marketdata.MarketDataRequirements;
import com.opengamma.strata.engine.marketdata.config.MarketDataConfig;
import com.opengamma.strata.engine.marketdata.function.MarketDataFunction;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveGroup;
import com.opengamma.strata.market.curve.CurveMetadata;
import com.opengamma.strata.market.id.IborIndexRatesId;
import com.opengamma.strata.market.id.IndexRateId;
import com.opengamma.strata.market.id.RateIndexCurveId;
import com.opengamma.strata.market.value.DiscountIborIndexRates;
import com.opengamma.strata.market.value.IborIndexRates;
import com.opengamma.strata.market.value.SimpleDiscountFactors;
import com.opengamma.strata.market.value.ValueType;
import com.opengamma.strata.market.value.ZeroRateDiscountFactors;

/**
 * Market data function that builds the provider of Ibor index rates.
 * <p>
 * This function creates an instance of {@link DiscountIborIndexRates} based on an underlying curve.
 * The curve may be wrapped in {@link ZeroRateDiscountFactors} or {@link SimpleDiscountFactors}.
 * The type is chosen based on the {@linkplain ValueType value type} held in
 * the {@linkplain CurveMetadata#getYValueType() y-value metadata}.
 * <p>
 * The curve is not actually built in this class, it is extracted from an existing {@link CurveGroup}.
 * The curve group must be available in the {@code MarketDataLookup} passed to the {@link #build} method.
 */
public class IborIndexRatesMarketDataFunction
    implements MarketDataFunction<IborIndexRates, IborIndexRatesId> {

  @Override
  public MarketDataRequirements requirements(IborIndexRatesId id, MarketDataConfig config) {
    RateIndexCurveId curveId = RateIndexCurveId.of(id.getIndex(), id.getCurveGroupName(), id.getMarketDataFeed());
    IndexRateId timeSeriesId = IndexRateId.of(id.getIndex(), id.getMarketDataFeed());
    return MarketDataRequirements.builder()
        .addValues(curveId)
        .addTimeSeries(timeSeriesId)
        .build();
  }

  @Override
  public Result<IborIndexRates> build(IborIndexRatesId id, MarketDataLookup marketData, MarketDataConfig config) {
    // find time-series
    IndexRateId timeSeriesId = IndexRateId.of(id.getIndex(), id.getMarketDataFeed());
    if (!marketData.containsTimeSeries(timeSeriesId)) {
      return Result.failure(
          FailureReason.MISSING_DATA,
          "No time-series found: Index: {}, Feed: {}",
          id.getIndex(),
          id.getMarketDataFeed());
    }
    LocalDateDoubleTimeSeries timeSeries = marketData.getTimeSeries(timeSeriesId);

    // find curve
    RateIndexCurveId curveId = RateIndexCurveId.of(id.getIndex(), id.getCurveGroupName(), id.getMarketDataFeed());
    if (!marketData.containsValue(curveId)) {
      return Result.failure(
          FailureReason.MISSING_DATA,
          "No curve found: Index: {}, Group: {}, Feed: {}",
          id.getIndex(),
          id.getCurveGroupName(),
          id.getMarketDataFeed());
    }
    Curve curve = marketData.getValue(curveId);

    // create Ibor rates
    return Result.wrap(() -> createIborIndexRates(id.getIndex(), marketData.getValuationDate(), timeSeries, curve));
  }

  // create the instance of IborIndexRates
  private Result<IborIndexRates> createIborIndexRates(
      IborIndex index,
      LocalDate valuationDate,
      LocalDateDoubleTimeSeries timeSeries,
      Curve curve) {

    ValueType yValueType = curve.getMetadata().getYValueType();
    if (ValueType.ZERO_RATE.equals(yValueType)) {
      ZeroRateDiscountFactors df = ZeroRateDiscountFactors.of(index.getCurrency(), valuationDate, curve);
      return Result.success(DiscountIborIndexRates.of(index, timeSeries, df));

    } else if (ValueType.DISCOUNT_FACTOR.equals(yValueType)) {
      SimpleDiscountFactors df = SimpleDiscountFactors.of(index.getCurrency(), valuationDate, curve);
      return Result.success(DiscountIborIndexRates.of(index, timeSeries, df));

    } else {
      return Result.failure(
          FailureReason.MISSING_DATA,
          "Invalid curve, must have ValueType of 'ZeroRate' or 'DiscountFactor', but was: {}",
          yValueType);
    }
  }

  @Override
  public Class<IborIndexRatesId> getMarketDataIdType() {
    return IborIndexRatesId.class;
  }

}
