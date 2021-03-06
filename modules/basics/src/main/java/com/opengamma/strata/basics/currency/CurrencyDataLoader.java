/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.basics.currency;

import static com.opengamma.strata.collect.Guavate.toImmutableList;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.opengamma.strata.collect.io.IniFile;
import com.opengamma.strata.collect.io.PropertySet;
import com.opengamma.strata.collect.io.ResourceLocator;

/**
 * Internal loader of currency and currency pair data.
 * <p>
 * This loads configuration files for {@link Currency} and {@link CurrencyPair}.
 */
final class CurrencyDataLoader {

  /**
   * INI file for currency data.
   */
  private static final String CURRENCY_INI = "com/opengamma/strata/basics/currency/Currency.ini";
  /**
   * INI file for currency pair data.
   */
  private static final String PAIR_INI = "com/opengamma/strata/basics/currency/CurrencyPair.ini";
  /**
   * INI file containing a list of general currency data.
   * This in includes a list of currencies in priority order used to choose the base currency of the market
   * convention pair for pairs that aren't configured in CurrencyPair.ini.
   */
  private static final String CURRENCY_DATA_INI = "com/opengamma/strata/basics/currency/CurrencyData.ini";

  // restricted constructor
  private CurrencyDataLoader() {
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the available currencies.
   * 
   * @param loadHistoric  whether to load the historic or active currencies
   * @return the map of known currencies
   */
  static ImmutableMap<String, Currency> loadCurrencies(boolean loadHistoric) {
    try {
      IniFile ini = IniFile.ofChained(
          ResourceLocator.streamOfClasspathResources(CURRENCY_INI).map(ResourceLocator::getCharSource));
      return parseCurrencies(ini, loadHistoric);

    } catch (RuntimeException ex) {
      // logging used because this is loaded in a static variable
      Logger logger = Logger.getLogger(CurrencyDataLoader.class.getName());
      logger.severe(Throwables.getStackTraceAsString(ex));
      // return an empty instance to avoid ExceptionInInitializerError
      return ImmutableMap.of();
    }
  }

  // parse currency info
  private static ImmutableMap<String, Currency> parseCurrencies(IniFile ini, boolean loadHistoric) {
    ImmutableMap.Builder<String, Currency> builder = ImmutableMap.builder();
    for (Entry<String, PropertySet> entry : ini.asMap().entrySet()) {
      String currencyCode = entry.getKey();
      if (Currency.REGEX_FORMAT.matcher(currencyCode).matches()) {
        PropertySet properties = entry.getValue();
        boolean isHistoric =
            (properties.keys().contains("historic") && Boolean.parseBoolean(properties.value("historic")));
        if (isHistoric == loadHistoric) {
          Integer minorUnits = Integer.parseInt(properties.value("minorUnitDigits"));
          String triangulationCurrency = properties.value("triangulationCurrency");
          builder.put(currencyCode, new Currency(currencyCode, minorUnits, triangulationCurrency));
        }
      }
    }
    return builder.build();
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the available currency pairs.
   * 
   * @return the map of known currency pairs, where the value is the number of digits in the rate
   */
  static ImmutableMap<CurrencyPair, Integer> loadPairs() {
    try {
      IniFile ini = IniFile.ofChained(
          ResourceLocator.streamOfClasspathResources(PAIR_INI).map(ResourceLocator::getCharSource));
      return parsePairs(ini);

    } catch (RuntimeException ex) {
      // logging used because this is loaded in a static variable
      Logger logger = Logger.getLogger(CurrencyDataLoader.class.getName());
      logger.severe(Throwables.getStackTraceAsString(ex));
      // return an empty instance to avoid ExceptionInInitializerError
      return ImmutableMap.of();
    }
  }

  // parse pair info
  private static ImmutableMap<CurrencyPair, Integer> parsePairs(IniFile ini) {
    ImmutableMap.Builder<CurrencyPair, Integer> builder = ImmutableMap.builder();
    for (Entry<String, PropertySet> entry : ini.asMap().entrySet()) {
      String pairStr = entry.getKey();
      if (CurrencyPair.REGEX_FORMAT.matcher(pairStr).matches()) {
        CurrencyPair pair = CurrencyPair.parse(pairStr);
        PropertySet properties = entry.getValue();
        Integer rateDigits = Integer.parseInt(properties.value("rateDigits"));
        builder.put(pair, rateDigits);
      }
    }
    return builder.build();
  }

  /**
   * Loads the priority order of currencies, used to determine the base currency of the market convention pair
   * for pairs that aren't explicitly configured.
   *
   * @return a map of currency to order
   */
  static ImmutableMap<Currency, Integer> loadOrdering() {
    try {
      Stream<ResourceLocator> resourceLocators = ResourceLocator.streamOfClasspathResources(CURRENCY_DATA_INI);
      IniFile ini = IniFile.ofChained(resourceLocators.map(ResourceLocator::getCharSource));
      PropertySet section = ini.section("marketConventionPriority");
      String list = section.value("ordering");
      // The currency ordering is defined as a comma-separated list
      List<Currency> currencies = Arrays.stream(list.split(","))
          .map(String::trim)
          .map(Currency::of)
          .collect(toImmutableList());

      ImmutableMap.Builder<Currency, Integer> orderBuilder = ImmutableMap.builder();

      for (int i = 0; i < currencies.size(); i++) {
        orderBuilder.put(currencies.get(i), i + 1);
      }
      return orderBuilder.build();
    } catch (Exception ex) {
      // logging used because this is loaded in a static variable
      Logger logger = Logger.getLogger(CurrencyDataLoader.class.getName());
      logger.severe(Throwables.getStackTraceAsString(ex));
      // return an empty instance to avoid ExceptionInInitializerError
      return ImmutableMap.of();
    }
  }
}
