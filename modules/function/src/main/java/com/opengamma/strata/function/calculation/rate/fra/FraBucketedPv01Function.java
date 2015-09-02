/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.rate.fra;

import static com.opengamma.strata.function.calculation.AbstractCalculationFunction.ONE_BASIS_POINT;

import com.opengamma.strata.finance.rate.fra.ExpandedFra;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.PointSensitivities;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Calculates the bucketed PV01, the present value curve parameter sensitivity of a {@code FraTrade}.
 * This operates by algorithmic differentiation (AD).
 */
public class FraBucketedPv01Function
    extends AbstractFraFunction<CurveCurrencyParameterSensitivities> {

  @Override
  protected CurveCurrencyParameterSensitivities execute(ExpandedFra product, RatesProvider provider) {
    PointSensitivities pointSensitivity = pricer().presentValueSensitivity(product, provider);
    return provider.curveParameterSensitivity(pointSensitivity).multipliedBy(ONE_BASIS_POINT);
  }

}
