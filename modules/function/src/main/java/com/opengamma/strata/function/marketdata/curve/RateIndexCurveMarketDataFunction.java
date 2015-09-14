/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.marketdata.curve;

import java.util.Optional;

import com.opengamma.strata.collect.result.FailureReason;
import com.opengamma.strata.collect.result.Result;
import com.opengamma.strata.engine.marketdata.MarketDataLookup;
import com.opengamma.strata.engine.marketdata.MarketDataRequirements;
import com.opengamma.strata.engine.marketdata.config.MarketDataConfig;
import com.opengamma.strata.engine.marketdata.function.MarketDataFunction;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveGroup;
import com.opengamma.strata.market.id.CurveGroupId;
import com.opengamma.strata.market.id.RateIndexCurveId;

/**
 * Market data function that builds a {@link Curve} representing the forward curve of an index.
 * <p>
 * The curve is not actually built in this class, it is extracted from an existing {@link CurveGroup}.
 * The curve group must be available in the {@code BaseMarketData} passed to the
 * {@link MarketDataFunction#build} method.
 */
public final class RateIndexCurveMarketDataFunction implements MarketDataFunction<Curve, RateIndexCurveId> {

  @Override
  public MarketDataRequirements requirements(RateIndexCurveId id, MarketDataConfig marketDataConfig) {
    CurveGroupId curveGroupId = CurveGroupId.of(id.getCurveGroupName(), id.getMarketDataFeed());
    return MarketDataRequirements.builder()
        .addValues(curveGroupId)
        .build();
  }

  @Override
  public Result<Curve> build(RateIndexCurveId id, MarketDataLookup marketData, MarketDataConfig marketDataConfig) {
    CurveGroupId curveGroupId = CurveGroupId.of(id.getCurveGroupName(), id.getMarketDataFeed());

    if (!marketData.containsValue(curveGroupId)) {
      return Result.failure(FailureReason.MISSING_DATA, "No curve group found with name {}", id.getCurveGroupName());
    }
    CurveGroup curveGroup = marketData.getValue(curveGroupId);
    Optional<Curve> optionalForwardCurve = curveGroup.getForwardCurve(id.getIndex());

    if (optionalForwardCurve.isPresent()) {
      return Result.success(optionalForwardCurve.get());
    } else {
      return Result.failure(
          FailureReason.MISSING_DATA,
          "No forward curve available for index {} in curve group {}",
          id.getIndex().getName(),
          id.getCurveGroupName());
    }
  }

  @Override
  public Class<RateIndexCurveId> getMarketDataIdType() {
    return RateIndexCurveId.class;
  }
}
