/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.fx;

import com.opengamma.strata.finance.fx.ExpandedFxSingle;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Calculates the bucketed PV01, the present value curve parameter sensitivity of a {@code FxSingleTrade}.
 * This operates by algorithmic differentiation (AD).
 */
public class FxSingleBucketedPv01Function
    extends AbstractFxSingleFunction<CurveCurrencyParameterSensitivities> {

  @Override
  protected CurveCurrencyParameterSensitivities execute(ExpandedFxSingle product, RatesProvider provider) {
    PointSensitivities pointSensitivity = pricer().presentValueSensitivity(product, provider);
    return provider.curveParameterSensitivity(pointSensitivity);
  }

}
