/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.credit;

import java.time.LocalDate;

import com.opengamma.strata.finance.credit.ExpandedCds;
import com.opengamma.strata.market.curve.IsdaCreditCurveParRates;
import com.opengamma.strata.market.curve.IsdaYieldCurveParRates;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;

/**
 * Calculates vector IR01 of a {@code CdsTrade} for each of a set of scenarios.
 * <p>
 * This calculates the vector PV change to a series of 1 basis point shifts in par interest rates at each curve node.
 */
public class CdsIr01BucketedParFunction
    extends AbstractCdsFunction<CurveCurrencyParameterSensitivities> {

  @Override
  protected CurveCurrencyParameterSensitivities execute(
      ExpandedCds product,
      IsdaYieldCurveParRates yieldCurveParRates,
      IsdaCreditCurveParRates creditCurveParRates,
      LocalDate valuationDate,
      double recoveryRate,
      double scalingFactor) {

    return pricer().ir01BucketedPar(
        product, yieldCurveParRates, creditCurveParRates, valuationDate, recoveryRate, scalingFactor);
  }

}
