/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.collect;

import static com.opengamma.strata.collect.CollectProjectAssertions.assertThat;

import java.util.Optional;

import org.testng.annotations.Test;

@Test
public class OptionalAssertTest {

  private static final Optional<String> PRESENT = Optional.of("foo");
  private static final Optional<String> EMPTY = Optional.empty();

  public void isPresent() {
    assertThat(PRESENT).isPresent();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void isPresentFail() {
    assertThat(EMPTY).isPresent();
  }

  public void isEmpty() {
    assertThat(EMPTY).isEmpty();
  }

  @Test(expectedExceptions = AssertionError.class)
  public void isEmptyFail() {
    assertThat(PRESENT).isEmpty();
  }

  public void hasValue() {
    assertThat(PRESENT).hasValue("foo");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void hasValueFail() {
    assertThat(EMPTY).hasValue("foo");
  }

  @Test(expectedExceptions = AssertionError.class)
  public void hasValueFail2() {
    assertThat(PRESENT).hasValue("bar");
  }
}
