/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.math.impl.rootfinding;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.RiddersSolver;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;

import com.opengamma.strata.math.impl.MathException;
import com.opengamma.strata.math.impl.function.Function1D;
import com.opengamma.strata.math.impl.util.CommonsMathWrapper;

/**
 * Finds a single root of a function using Ridder's method. This class is a wrapper for the
 * <a href="http://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/analysis/solvers/RiddersSolver.html">Commons Math library implementation</a>
 * of Ridder's method.
 */
public class RidderSingleRootFinder extends RealSingleRootFinder {

  private static final int MAX_ITER = 100000;
  private final RiddersSolver _ridder;

  /**
   * Sets the accuracy to 10<sup>-15</sup>
   */
  public RidderSingleRootFinder() {
    this(1e-15);
  }

  /**
   * @param functionValueAccuracy The accuracy of the function evaluations.
   */
  public RidderSingleRootFinder(final double functionValueAccuracy) {
    _ridder = new RiddersSolver(functionValueAccuracy);
  }

  /**
   * @param functionValueAccuracy The accuracy of the function evaluations.
   * @param absoluteAccurary The maximum absolute error of the variable.
   */
  public RidderSingleRootFinder(final double functionValueAccuracy, final double absoluteAccurary) {
    _ridder = new RiddersSolver(functionValueAccuracy, absoluteAccurary);
  }

  /**
   * {@inheritDoc}
   * @throws MathException If the Commons method could not evaluate the function; if the Commons method could not converge.
   */
  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double xLow, final Double xHigh) {
    checkInputs(function, xLow, xHigh);
    final UnivariateFunction wrapped = CommonsMathWrapper.wrapUnivariate(function);
    try {
      return _ridder.solve(MAX_ITER, wrapped, xLow, xHigh);
    } catch (TooManyEvaluationsException | NoBracketingException e) {
      throw new MathException(e);
    }
  }
}
