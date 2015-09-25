/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.function.calculation.payment;

import com.opengamma.strata.engine.config.Measure;
import com.opengamma.strata.engine.config.pricing.DefaultFunctionGroup;
import com.opengamma.strata.engine.config.pricing.FunctionGroup;
import com.opengamma.strata.finance.payment.BulletPaymentTrade;

/**
 * Contains function groups for built-in Bullet Payment calculation functions.
 * <p>
 * Function groups are used in pricing rules to allow the engine to calculate the
 * measures provided by the functions in the group.
 */
public final class BulletPaymentFunctionGroups {

  /**
   * The group with pricers based on discounting methods.
   */
  private static final FunctionGroup<BulletPaymentTrade> DISCOUNTING_GROUP =
      DefaultFunctionGroup.builder(BulletPaymentTrade.class).name("BulletPaymentDiscounting")
          .addFunction(Measure.PRESENT_VALUE, BulletPaymentPvFunction.class)
          .addFunction(Measure.PV01, BulletPaymentPv01Function.class)
          .addFunction(Measure.BUCKETED_PV01, BulletPaymentBucketedPv01Function.class)
          .build();

  /**
   * Restricted constructor.
   */
  private BulletPaymentFunctionGroups() {
  }

  //-------------------------------------------------------------------------
  /**
   * Obtains the function group providing all built-in measures on FRA trades,
   * using the standard discounting calculation method.
   * <p>
   * The supported built-in measures are:
   * <ul>
   *   <li>{@linkplain Measure#PRESENT_VALUE Present value}
   *   <li>{@linkplain Measure#PV01 PV01}
   *   <li>{@linkplain Measure#BUCKETED_PV01 Bucketed PV01}
   * </ul>
   * 
   * @return the function group
   */
  public static FunctionGroup<BulletPaymentTrade> discounting() {
    return DISCOUNTING_GROUP;
  }

}
