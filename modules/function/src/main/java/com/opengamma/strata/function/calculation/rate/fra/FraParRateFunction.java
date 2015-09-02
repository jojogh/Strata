/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.rate.fra;

import com.opengamma.strata.finance.rate.fra.ExpandedFra;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Calculates the par rate of a {@code FraTrade} for each of a set of scenarios.
 */
public class FraParRateFunction
    extends AbstractFraFunction<Double> {

  @Override
  protected Double execute(ExpandedFra product, RatesProvider provider) {
    return pricer().parRate(product, provider);
  }

}
