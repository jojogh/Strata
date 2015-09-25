/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.rate.future;

import static com.opengamma.strata.collect.TestHelper.assertSerialization;
import static com.opengamma.strata.collect.TestHelper.assertThrows;
import static com.opengamma.strata.collect.TestHelper.coverBeanEquals;
import static com.opengamma.strata.collect.TestHelper.coverImmutableBean;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

import java.time.LocalDate;

import org.testng.annotations.Test;

import com.google.common.reflect.TypeToken;
import com.opengamma.strata.basics.BuySell;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.collect.id.IdentifiableBean;
import com.opengamma.strata.collect.id.LinkResolver;
import com.opengamma.strata.collect.id.StandardId;
import com.opengamma.strata.finance.Security;
import com.opengamma.strata.finance.SecurityLink;
import com.opengamma.strata.finance.TradeInfo;
import com.opengamma.strata.finance.UnitSecurity;
import com.opengamma.strata.finance.rate.swap.Swap;
import com.opengamma.strata.finance.rate.swap.type.FixedIborSwapConventions;

/**
 * Test {@link DeliverableSwapFutureTrade}.
 */
@Test
public class DeliverableSwapFutureTradeTest {
  private static final LocalDate START_DATE = LocalDate.of(2014, 9, 12);
  private static final Swap SWAP = FixedIborSwapConventions.USD_FIXED_6M_LIBOR_3M
      .toTrade(START_DATE, Tenor.TENOR_10Y, BuySell.SELL, 1d, 0.015).getProduct();
  private static final LocalDate LAST_TRADE_DATE = LocalDate.of(2014, 9, 5);
  private static final LocalDate DELIVERY_DATE = LocalDate.of(2014, 9, 9);
  private static final double NOTIONAL = 100000;
  private static final StandardId SWAP_ID = StandardId.of("OG-Ticker", "Swap1");
  private static final Security<Swap> SWAP_SECURITY = UnitSecurity.builder(SWAP).standardId(SWAP_ID).build();
  private static final DeliverableSwapFuture DSF_PRODUCT = DeliverableSwapFuture.builder()
      .deliveryDate(DELIVERY_DATE)
      .lastTradeDate(LAST_TRADE_DATE)
      .notional(NOTIONAL)
      .underlyingSecurity(SWAP_SECURITY)
      .build();
  private static final StandardId DSF_ID = StandardId.of("OG-Ticker", "DSF1");
  private static final Security<DeliverableSwapFuture> DSF_SECURITY = UnitSecurity
      .builder(DSF_PRODUCT)
      .standardId(DSF_ID)
      .build();
  private static final SecurityLink<DeliverableSwapFuture> DSF_RESOLVABLE =
      SecurityLink.resolvable(DSF_ID, DeliverableSwapFuture.class);
  private static final SecurityLink<DeliverableSwapFuture> DSF_RESOLVED = SecurityLink.resolved(DSF_SECURITY);
  private static final TradeInfo TRADE_INFO = TradeInfo.builder()
      .tradeDate(LocalDate.of(2014,6, 12))
      .settlementDate(LocalDate.of(2014, 6, 14))
      .build();
  private static final long QUANTITY = 100L;
  private static final double TRADE_PRICE = 0.99;

  private static final LinkResolver RESOLVER = new LinkResolver() {
    @SuppressWarnings("unchecked")
    @Override
    public <T extends IdentifiableBean> T resolve(StandardId identifier, TypeToken<T> targetType) {
      assertEquals(identifier, DSF_ID);
      return (T) DSF_SECURITY;
    }
  };

  //-------------------------------------------------------------------------
  public void test_builder_resolvable() {
    DeliverableSwapFutureTrade test = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVABLE)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    assertEquals(test.getQuantity(), QUANTITY);
    assertEquals(test.getSecurityLink(), DSF_RESOLVABLE);
    assertEquals(test.getTradeInfo(), TRADE_INFO);
    assertEquals(test.getTradePrice(), TRADE_PRICE);
    assertThrows(() -> test.getProduct(), IllegalStateException.class);
    assertThrows(() -> test.getSecurity(), IllegalStateException.class);
  }

  public void test_builder_resolved() {
    DeliverableSwapFutureTrade test = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVED)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    assertEquals(test.getQuantity(), QUANTITY);
    assertEquals(test.getSecurityLink(), DSF_RESOLVED);
    assertEquals(test.getTradeInfo(), TRADE_INFO);
    assertEquals(test.getTradePrice(), TRADE_PRICE);
    assertEquals(test.getProduct(), DSF_PRODUCT);
    assertEquals(test.getSecurity(), DSF_SECURITY);
  }

  //-------------------------------------------------------------------------
  public void test_resolveLinks_resolvable() {
    DeliverableSwapFutureTrade test = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVABLE)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    DeliverableSwapFutureTrade expected = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVED)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    assertEquals(test.resolveLinks(RESOLVER), expected);
  }

  public void test_resolveLinks_resolved() {
    DeliverableSwapFutureTrade test = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVED)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    assertSame(test.resolveLinks(RESOLVER), test);
  }

  //-------------------------------------------------------------------------
  public void coverage() {
    DeliverableSwapFutureTrade test1 = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVED)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    coverImmutableBean(test1);
    DeliverableSwapFutureTrade test2 = DeliverableSwapFutureTrade.builder()
        .quantity(10L)
        .securityLink(DSF_RESOLVABLE)
        .tradePrice(1.01)
        .build();
    coverBeanEquals(test1, test2);
  }

  public void test_serialization() {
    DeliverableSwapFutureTrade test = DeliverableSwapFutureTrade.builder()
        .quantity(QUANTITY)
        .securityLink(DSF_RESOLVED)
        .tradeInfo(TRADE_INFO)
        .tradePrice(TRADE_PRICE)
        .build();
    assertSerialization(test);
  }
}
