/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.rate.swap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.basics.index.Index;
import com.opengamma.strata.basics.schedule.Schedule;

/**
 * The accrual calculation part of an interest rate swap leg.
 * <p>
 * An interest rate swap leg is defined by {@link RateCalculationSwapLeg}.
 * The rate to be paid is defined by the implementations of this interface.
 * <p>
 * Implementations must be immutable and thread-safe beans.
 */
public interface RateCalculation {

  /**
   * Gets the type of the leg, such as Fixed or Ibor.
   * <p>
   * This provides a high level categorization of the swap leg.
   * 
   * @return the leg type
   */
  public abstract SwapLegType getType();

  /**
   * Gets the day count convention.
   * <p>
   * This is used to convert schedule period dates to a numerical value.
   * 
   * @return the day count convention
   */
  public abstract DayCount getDayCount();

  /**
   * Collects all the indices referred to by this calculation.
   * <p>
   * A calculation will typically refer to at least one index, such as 'GBP-LIBOR-3M'.
   * Each index that is referred to must be added to the specified builder.
   * 
   * @param builder  the builder to use
   */
  public abstract void collectIndices(ImmutableSet.Builder<Index> builder);

  /**
   * Expands this calculation to accrual periods based on the specified schedule.
   * <p>
   * The specified accrual schedule defines the period dates to be created.
   * One instance of {@link RateAccrualPeriod} must be created for each period in the schedule.
   * 
   * @param accrualSchedule  the accrual schedule
   * @param paymentSchedule  the payment schedule
   * @return the expanded accrual periods
   * @throws RuntimeException if the swap is invalid
   */
  public abstract ImmutableList<RateAccrualPeriod> expand(Schedule accrualSchedule, Schedule paymentSchedule);

}
