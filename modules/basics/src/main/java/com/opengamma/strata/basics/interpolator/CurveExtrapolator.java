/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.interpolator;

import org.joda.convert.FromString;

import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.collect.named.ExtendedEnum;
import com.opengamma.strata.collect.named.Named;

/**
 * Interface for extrapolators which extrapolate beyond the ends of a curve.
 * <p>
 * This is a marker interface implemented by legacy extrapolator implementations. At some point it will
 * be expanded to include extrapolation operations.
 */
public interface CurveExtrapolator extends Named {

  /**
   * Obtains a {@code CurveExtrapolator} from a unique name.
   *
   * @param uniqueName  the unique name
   * @return the index
   * @throws IllegalArgumentException if the name is not known
   */
  @FromString
  public static CurveExtrapolator of(String uniqueName) {
    ArgChecker.notNull(uniqueName, "uniqueName");
    return extendedEnum().lookup(uniqueName);
  }

  /**
   * Gets the extended enum helper.
   * <p>
   * This helper allows instances of {@code CurveExtrapolator} to be lookup up.
   * It also provides the complete set of available instances.
   *
   * @return the extended enum helper
   */
  public static ExtendedEnum<CurveExtrapolator> extendedEnum() {
    return CurveExtrapolatorHelper.ENUM_LOOKUP;
  }
}
