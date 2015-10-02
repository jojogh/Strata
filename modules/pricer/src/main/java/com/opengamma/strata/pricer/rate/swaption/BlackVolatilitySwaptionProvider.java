/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.rate.swaption;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import com.opengamma.strata.finance.rate.swap.type.FixedIborSwapConvention;
import com.opengamma.strata.market.sensitivity.SurfaceCurrencyParameterSensitivity;
import com.opengamma.strata.market.sensitivity.SwaptionSensitivity;

/**
 * Volatility environment for swaption in the log-normal or Black model.
 */
public interface BlackVolatilitySwaptionProvider {

  /**
   * Returns the log-normal volatility.
   * 
   * @param expiryDate  the option expiry date and time
   * @param tenor  the swaption tenor in years
   * @param strike  the option strike rate
   * @param forwardRate  the forward rate of the underlying swap
   * @return the volatility
   */
  public double getVolatility(ZonedDateTime expiryDate, double tenor, double strike, double forwardRate);

  /**
   * Returns the convention of the swap for which the data is valid.
   * 
   * @return the convention
   */
  public FixedIborSwapConvention getConvention();

  /**
   * Computes the sensitivity to the nodes of the underlying volatility objects 
   * <p>
   * The underlying object is typically curve, surface or cube. 
   * 
   * @param sensitivity  the point sensitivity
   * @return the node sensitivity
   */
  public SurfaceCurrencyParameterSensitivity surfaceCurrencyParameterSensitivity(SwaptionSensitivity sensitivity);

  //-------------------------------------------------------------------------
  /**
   * Converts a time and date to a relative year fraction. 
   * <p>
   * When the date is after the valuation date (and potentially time), the returned number is negative.
   * 
   * @param date  the date/time to find the relative year fraction of
   * @return the relative year fraction
   */
  public abstract double relativeTime(ZonedDateTime date);

  /**
   * Returns the tenor of the swap based on its start date and end date.
   * 
   * @param startDate  the start date
   * @param endDate  the end date
   * @return the tenor
   */
  public abstract double tenor(LocalDate startDate, LocalDate endDate);

  /**
   * Returns the valuation date-time. 
   * <p>
   * All data items in this provider are calibrated for this date-time.
   * 
   * @return the date
   */
  public ZonedDateTime getValuationDateTime();

}
