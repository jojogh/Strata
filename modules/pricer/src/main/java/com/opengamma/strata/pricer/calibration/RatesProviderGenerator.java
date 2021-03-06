/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.calibration;

import com.opengamma.strata.pricer.rate.ImmutableRatesProvider;
import com.opengamma.strata.pricer.rate.RatesProvider;

/**
 * Generates a {@link RatesProvider} from a set of parameters.
 * <p>
 * This creates a new provider based on the specified parameters.
 */
public interface RatesProviderGenerator {

  /**
   * Generates a rates provider from a set of parameters.
   * <p>
   * The number of parameters passed has to match the total number of parameters in all the curves generated.
   * 
   * @param parameters  the parameters describing the provider
   * @return the provider
   */
  public abstract ImmutableRatesProvider generate(double[] parameters);

}
