/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.function.rate.swap;

import com.opengamma.strata.engine.config.Measure;
import com.opengamma.strata.engine.config.pricing.DefaultFunctionGroup;
import com.opengamma.strata.engine.config.pricing.FunctionGroup;
import com.opengamma.strata.finance.rate.swap.SwapTrade;

/**
 * Contains function groups for built-in swap engine functions.
 * <p>
 * Function groups are used in pricing rules to allow the engine to calculate the
 * measures provided by the functions in the group.
 */
public final class SwapFunctionGroups {

  /**
   * The group with pricers based on discounting methods.
   */
  private static final FunctionGroup<SwapTrade> DISCOUNTING_GROUP =
      DefaultFunctionGroup.builder(SwapTrade.class).name("SwapDiscounting")
          .addFunction(Measure.MATURITY_DATE, SwapMaturityDateFunction.class)
          .addFunction(Measure.LEG_INITIAL_NOTIONAL, SwapLegNotionalFunction.class)
          .addFunction(Measure.PRESENT_VALUE, SwapPvFunction.class)
          .addFunction(Measure.LEG_PRESENT_VALUE, SwapLegPvFunction.class)
          .addFunction(Measure.PV01, SwapPv01Function.class)
          .addFunction(Measure.PAR_RATE, SwapParRateFunction.class)
          .addFunction(Measure.ACCRUED_INTEREST, SwapAccruedInterestFunction.class)
          .build();

  /**
   * Restricted constructor.
   */
  private SwapFunctionGroups() {
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains the function group providing all built-in measures on swap trades,
   * using the standard discounting calculation method.
   * <p>
   * The supported built-in measures are:
   * <ul>
   *   <li>{@linkplain Measure#MATURITY_DATE Maturity date}
   *   <li>{@linkplain Measure#LEG_INITIAL_NOTIONAL Leg initial notional}
   *   <li>{@linkplain Measure#PRESENT_VALUE Present value}
   *   <li>{@linkplain Measure#LEG_PRESENT_VALUE Leg present value}
   *   <li>{@linkplain Measure#PV01 PV01}
   *   <li>{@linkplain Measure#PAR_RATE Par rate}
   *   <li>{@linkplain Measure#ACCRUED_INTEREST Accrued interest}
   * </ul>
   * 
   * @return the function group
   */
  public static FunctionGroup<SwapTrade> discounting() {
    return DISCOUNTING_GROUP;
  }

}
