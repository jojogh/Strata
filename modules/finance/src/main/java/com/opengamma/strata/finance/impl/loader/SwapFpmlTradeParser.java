/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.impl.loader;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.opengamma.strata.basics.PayReceive;
import com.opengamma.strata.basics.date.AdjustableDate;
import com.opengamma.strata.basics.date.BusinessDayAdjustment;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.index.FloatingRateName;
import com.opengamma.strata.basics.index.FloatingRateType;
import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.index.Index;
import com.opengamma.strata.basics.index.OvernightIndex;
import com.opengamma.strata.basics.schedule.Frequency;
import com.opengamma.strata.basics.schedule.PeriodicSchedule;
import com.opengamma.strata.basics.schedule.StubConvention;
import com.opengamma.strata.basics.value.ValueAdjustment;
import com.opengamma.strata.basics.value.ValueSchedule;
import com.opengamma.strata.basics.value.ValueStep;
import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.collect.io.XmlElement;
import com.opengamma.strata.finance.Trade;
import com.opengamma.strata.finance.TradeInfo;
import com.opengamma.strata.finance.loader.FpmlDocument;
import com.opengamma.strata.finance.loader.FpmlParseException;
import com.opengamma.strata.finance.loader.FpmlTradeParser;
import com.opengamma.strata.finance.rate.swap.CompoundingMethod;
import com.opengamma.strata.finance.rate.swap.FixedRateCalculation;
import com.opengamma.strata.finance.rate.swap.FixingRelativeTo;
import com.opengamma.strata.finance.rate.swap.IborRateAveragingMethod;
import com.opengamma.strata.finance.rate.swap.IborRateCalculation;
import com.opengamma.strata.finance.rate.swap.KnownAmountSwapLeg;
import com.opengamma.strata.finance.rate.swap.NegativeRateMethod;
import com.opengamma.strata.finance.rate.swap.NotionalSchedule;
import com.opengamma.strata.finance.rate.swap.OvernightAccrualMethod;
import com.opengamma.strata.finance.rate.swap.OvernightRateCalculation;
import com.opengamma.strata.finance.rate.swap.PaymentRelativeTo;
import com.opengamma.strata.finance.rate.swap.PaymentSchedule;
import com.opengamma.strata.finance.rate.swap.RateCalculation;
import com.opengamma.strata.finance.rate.swap.RateCalculationSwapLeg;
import com.opengamma.strata.finance.rate.swap.ResetSchedule;
import com.opengamma.strata.finance.rate.swap.StubCalculation;
import com.opengamma.strata.finance.rate.swap.Swap;
import com.opengamma.strata.finance.rate.swap.SwapLeg;
import com.opengamma.strata.finance.rate.swap.SwapTrade;

/**
 * FpML parser for Swaps.
 * <p>
 * This parser handles the subset of FpML necessary to populate the trade model.
 */
final class SwapFpmlTradeParser
    implements FpmlTradeParser {
  // this class is loaded by ExtendedEnum reflection

  /**
   * The singleton instance of the parser.
   */
  public static final SwapFpmlTradeParser INSTANCE = new SwapFpmlTradeParser();

  /**
   * Restricted constructor.
   */
  private SwapFpmlTradeParser() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Trade parseTrade(XmlElement tradeEl, FpmlDocument document) {
    // supported elements:
    //  'swapStream+'
    //  'swapStream/buyerPartyReference'
    //  'swapStream/sellerPartyReference'
    //  'swapStream/calculationPeriodDates'
    //  'swapStream/paymentDates'
    //  'swapStream/resetDates?'
    //  'swapStream/calculationPeriodAmount'
    //  'swapStream/stubCalculationPeriodAmount?'
    //  'swapStream/principalExchanges?'
    //  'swapStream/calculationPeriodAmount/knownAmountSchedule'
    // ignored elements:
    //  'Product.model?'
    //  'swapStream/cashflows?'
    //  'swapStream/settlementProvision?'
    //  'swapStream/formula?'
    //  'earlyTerminationProvision?'
    //  'cancelableProvision?'
    //  'extendibleProvision?'
    //  'additionalPayment*'
    //  'additionalTerms?'
    // rejected elements:
    //  'swapStream/calculationPeriodAmount/calculation/fxLinkedNotionalSchedule'
    //  'swapStream/calculationPeriodAmount/calculation/futureValueNotional'
    TradeInfo.Builder tradeInfoBuilder = document.parseTradeInfo(tradeEl);
    XmlElement swapEl = tradeEl.getChild("swap");
    ImmutableList<XmlElement> legEls = swapEl.getChildren("swapStream");
    ImmutableList.Builder<SwapLeg> legsBuilder = ImmutableList.builder();
    for (XmlElement legEl : legEls) {
      // calculation
      XmlElement calcPeriodAmountEl = legEl.getChild("calculationPeriodAmount");
      XmlElement calcEl = calcPeriodAmountEl.findChild("calculation")
          .orElse(XmlElement.ofChildren("calculation", ImmutableList.of()));
      PeriodicSchedule accrualSchedule = parseSwapAccrualSchedule(legEl, document);
      PaymentSchedule paymentSchedule = parseSwapPaymentSchedule(legEl, calcEl, document);
      // known amount or rate calculation
      Optional<XmlElement> knownAmountOptEl = calcPeriodAmountEl.findChild("knownAmountSchedule");
      if (knownAmountOptEl.isPresent()) {
        XmlElement knownAmountEl = knownAmountOptEl.get();
        document.validateNotPresent(legEl, "stubCalculationPeriodAmount");
        document.validateNotPresent(legEl, "resetDates");
        // pay/receive and counterparty
        PayReceive payReceive = document.parsePayerReceiver(legEl, tradeInfoBuilder);
        ValueSchedule notionalSchedule = parseSchedule(knownAmountEl, document);
        // build
        legsBuilder.add(KnownAmountSwapLeg.builder()
            .payReceive(payReceive)
            .accrualSchedule(accrualSchedule)
            .paymentSchedule(paymentSchedule)
            .amount(notionalSchedule)
            .currency(document.parseCurrency(knownAmountEl.getChild("currency")))
            .build());
      } else {
        document.validateNotPresent(calcEl, "fxLinkedNotionalSchedule");
        document.validateNotPresent(calcEl, "futureValueNotional");
        // pay/receive and counterparty
        PayReceive payReceive = document.parsePayerReceiver(legEl, tradeInfoBuilder);
        NotionalSchedule notionalSchedule = parseSwapNotionalSchedule(legEl, calcEl, document);
        RateCalculation calculation = parseSwapCalculation(legEl, calcEl, accrualSchedule, document);
        // build
        legsBuilder.add(RateCalculationSwapLeg.builder()
            .payReceive(payReceive)
            .accrualSchedule(accrualSchedule)
            .paymentSchedule(paymentSchedule)
            .notionalSchedule(notionalSchedule)
            .calculation(calculation)
            .build());
      }
    }
    return SwapTrade.builder()
        .tradeInfo(tradeInfoBuilder.build())
        .product(Swap.of(legsBuilder.build()))
        .build();
  }

  // parses the accrual schedule
  private PeriodicSchedule parseSwapAccrualSchedule(XmlElement legEl, FpmlDocument document) {
    // supported elements:
    //  'calculationPeriodDates/effectiveDate'
    //  'calculationPeriodDates/relativeEffectiveDate'
    //  'calculationPeriodDates/terminationDate'
    //  'calculationPeriodDates/relativeTerminationDate'
    //  'calculationPeriodDates/calculationPeriodDates'
    //  'calculationPeriodDates/calculationPeriodDatesAdjustments'
    //  'calculationPeriodDates/firstPeriodStartDate?'
    //  'calculationPeriodDates/firstRegularPeriodStartDate?'
    //  'calculationPeriodDates/lastRegularPeriodEndDate?'
    //  'calculationPeriodDates/stubPeriodType?'
    //  'calculationPeriodDates/calculationPeriodFrequency'
    // ignored elements:
    //  'calculationPeriodDates/firstCompoundingPeriodEndDate?'
    PeriodicSchedule.Builder accrualScheduleBuilder = PeriodicSchedule.builder();
    // calculation dates
    XmlElement calcPeriodDatesEl = legEl.getChild("calculationPeriodDates");
    // business day adjustments
    BusinessDayAdjustment bda = document.parseBusinessDayAdjustments(
        calcPeriodDatesEl.getChild("calculationPeriodDatesAdjustments"));
    accrualScheduleBuilder.businessDayAdjustment(bda);
    // start date
    AdjustableDate startDate = calcPeriodDatesEl.findChild("effectiveDate")
        .map(el -> document.parseAdjustableDate(el))
        .orElseGet(() -> document.parseAdjustedRelativeDateOffset(calcPeriodDatesEl.getChild("relativeEffectiveDate")));
    accrualScheduleBuilder.startDate(startDate.getUnadjusted());
    if (!bda.equals(startDate.getAdjustment())) {
      accrualScheduleBuilder.startDateBusinessDayAdjustment(startDate.getAdjustment());
    }
    // end date
    AdjustableDate endDate = calcPeriodDatesEl.findChild("terminationDate")
        .map(el -> document.parseAdjustableDate(el))
        .orElseGet(() -> document.parseAdjustedRelativeDateOffset(calcPeriodDatesEl.getChild("relativeTerminationDate")));
    accrualScheduleBuilder.endDate(endDate.getUnadjusted());
    if (!bda.equals(endDate.getAdjustment())) {
      accrualScheduleBuilder.endDateBusinessDayAdjustment(endDate.getAdjustment());
    }
    // first date (overwrites the start date)
    calcPeriodDatesEl.findChild("firstPeriodStartDate").ifPresent(el -> {
      AdjustableDate actualStartDate = document.parseAdjustableDate(el);
      accrualScheduleBuilder.startDate(actualStartDate.getUnadjusted());
      if (!bda.equals(actualStartDate.getAdjustment())) {
        accrualScheduleBuilder.startDateBusinessDayAdjustment(actualStartDate.getAdjustment());
      }
    });
    // first regular date
    calcPeriodDatesEl.findChild("firstRegularPeriodStartDate").ifPresent(el -> {
      accrualScheduleBuilder.firstRegularStartDate(document.parseDate(el));
    });
    // last regular date
    calcPeriodDatesEl.findChild("lastRegularPeriodEndDate").ifPresent(el -> {
      accrualScheduleBuilder.lastRegularEndDate(document.parseDate(el));
    });
    // stub type
    calcPeriodDatesEl.findChild("stubPeriodType").ifPresent(el -> {
      accrualScheduleBuilder.stubConvention(parseStubConvention(el, document));
    });
    // frequency
    XmlElement freqEl = calcPeriodDatesEl.getChild("calculationPeriodFrequency");
    Frequency accrualFreq = document.parseFrequency(freqEl);
    accrualScheduleBuilder.frequency(accrualFreq);
    // roll convention
    accrualScheduleBuilder.rollConvention(
        document.convertRollConvention(freqEl.getChild("rollConvention").getContent()));
    return accrualScheduleBuilder.build();
  }

  // parses the payment schedule
  private PaymentSchedule parseSwapPaymentSchedule(XmlElement legEl, XmlElement calcEl, FpmlDocument document) {
    // supported elements:
    //  'paymentDates/paymentFrequency'
    //  'paymentDates/payRelativeTo'
    //  'paymentDates/paymentDaysOffset?'
    //  'paymentDates/paymentDatesAdjustments'
    //  'calculationPeriodAmount/calculation/compoundingMethod'
    // ignored elements:
    //  'paymentDates/calculationPeriodDatesReference'
    //  'paymentDates/resetDatesReference'
    //  'paymentDates/valuationDatesReference'
    //  'paymentDates/firstPaymentDate?'
    //  'paymentDates/lastRegularPaymentDate?'
    PaymentSchedule.Builder paymentScheduleBuilder = PaymentSchedule.builder();
    // payment dates
    XmlElement paymentDatesEl = legEl.getChild("paymentDates");
    // frequency
    paymentScheduleBuilder.paymentFrequency(document.parseFrequency(
        paymentDatesEl.getChild("paymentFrequency")));
    paymentScheduleBuilder.paymentRelativeTo(parsePayRelativeTo(paymentDatesEl.getChild("payRelativeTo")));
    // offset
    Optional<XmlElement> paymentOffsetEl = paymentDatesEl.findChild("paymentDaysOffset");
    BusinessDayAdjustment payAdjustment = document.parseBusinessDayAdjustments(
        paymentDatesEl.getChild("paymentDatesAdjustments"));
    if (paymentOffsetEl.isPresent()) {
      Period period = document.parsePeriod(paymentOffsetEl.get());
      if (period.toTotalMonths() != 0) {
        throw new FpmlParseException("Invalid 'paymentDatesAdjustments' value, expected days-based period: " + period);
      }
      Optional<XmlElement> dayTypeEl = paymentOffsetEl.get().findChild("dayType");
      boolean fixingCalendarDays = period.isZero() ||
          (dayTypeEl.isPresent() && dayTypeEl.get().getContent().equals("Calendar"));
      if (fixingCalendarDays) {
        paymentScheduleBuilder.paymentDateOffset(DaysAdjustment.ofCalendarDays(period.getDays(), payAdjustment));
      } else {
        paymentScheduleBuilder.paymentDateOffset(DaysAdjustment.ofBusinessDays(period.getDays(), payAdjustment.getCalendar()));
      }
    } else {
      paymentScheduleBuilder.paymentDateOffset(DaysAdjustment.ofCalendarDays(0, payAdjustment));
    }
    // compounding
    calcEl.findChild("compoundingMethod").ifPresent(compoundingEl -> {
      paymentScheduleBuilder.compoundingMethod(CompoundingMethod.of(compoundingEl.getContent()));
    });
    return paymentScheduleBuilder.build();
  }

  // parses the notional schedule
  private NotionalSchedule parseSwapNotionalSchedule(XmlElement legEl, XmlElement calcEl, FpmlDocument document) {
    // supported elements:
    //  'principalExchanges/initialExchange'
    //  'principalExchanges/finalExchange'
    //  'principalExchanges/intermediateExchange'
    //  'calculationPeriodAmount/calculation/notionalSchedule'
    // rejected elements:
    //  'calculationPeriodAmount/calculation/notionalSchedule/notionalStepParameters'
    NotionalSchedule.Builder notionalScheduleBuilder = NotionalSchedule.builder();
    // exchanges
    legEl.findChild("principalExchanges").ifPresent(el -> {
      notionalScheduleBuilder.initialExchange(Boolean.parseBoolean(el.getChild("initialExchange").getContent()));
      notionalScheduleBuilder.intermediateExchange(
          Boolean.parseBoolean(el.getChild("intermediateExchange").getContent()));
      notionalScheduleBuilder.finalExchange(Boolean.parseBoolean(el.getChild("finalExchange").getContent()));
    });
    // notional schedule
    XmlElement notionalEl = calcEl.getChild("notionalSchedule");
    document.validateNotPresent(notionalEl, "notionalStepParameters");
    XmlElement notionalScheduleEl = notionalEl.getChild("notionalStepSchedule");
    notionalScheduleBuilder.amount(parseSchedule(notionalScheduleEl, document));
    notionalScheduleBuilder.currency(document.parseCurrency(notionalScheduleEl.getChild("currency")));
    return notionalScheduleBuilder.build();
  }

  // parse swap rate calculation
  private RateCalculation parseSwapCalculation(XmlElement legEl, XmlElement calcEl, PeriodicSchedule accrualSchedule, FpmlDocument document) {
    // supported elements:
    //  'calculationPeriodAmount/calculation/fixedRateSchedule'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/floatingRateIndex'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/indexTenor?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/floatingRateMultiplierSchedule?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/spreadSchedule*'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/initialRate?' (Ibor only)
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/averagingMethod?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/negativeInterestRateTreatment?'
    //  'calculationPeriodAmount/calculation/dayCountFraction'
    //  'resetDates/resetRelativeTo'
    //  'resetDates/fixingDates'
    //  'resetDates/rateCutOffDaysOffset' (OIS only)
    //  'resetDates/resetFrequency'
    //  'resetDates/resetDatesAdjustments'
    //  'stubCalculationPeriodAmount/initalStub' (Ibor only)
    //  'stubCalculationPeriodAmount/finalStub' (Ibor only)
    // ignored elements:
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/finalRateRounding?'
    //  'calculationPeriodAmount/calculation/discounting?'
    //  'resetDates/calculationPeriodDatesReference'
    // rejected elements:
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/spreadSchedule/type?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/rateTreatment?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/capRateSchedule?'
    //  'calculationPeriodAmount/calculation/floatingRateCalculation/floorRateSchedule?'
    //  'resetDates/initialFixingDate'
    //  'stubCalculationPeriodAmount/initalStub/stubAmount'
    //  'stubCalculationPeriodAmount/finalStub/stubAmount'
    Optional<XmlElement> fixedOptEl = calcEl.findChild("fixedRateSchedule");
    Optional<XmlElement> floatingOptEl = calcEl.findChild("floatingRateCalculation");

    if (fixedOptEl.isPresent()) {
      // fixed
      // TODO: stubCalculationPeriodAmount could affect this
      return FixedRateCalculation.builder()
          .rate(parseSchedule(fixedOptEl.get(), document))
          .dayCount(document.parseDayCountFraction(calcEl.getChild("dayCountFraction")))
          .build();

    } else if (floatingOptEl.isPresent()) {
      // float
      XmlElement floatingEl = floatingOptEl.get();
      document.validateNotPresent(floatingEl, "rateTreatment");
      document.validateNotPresent(floatingEl, "capRateSchedule");
      document.validateNotPresent(floatingEl, "floorRateSchedule");
      Index index = document.parseIndex(floatingEl);
      if (index instanceof IborIndex) {
        IborRateCalculation.Builder iborRateBuilder = IborRateCalculation.builder();
        // day count
        iborRateBuilder.dayCount(document.parseDayCountFraction(calcEl.getChild("dayCountFraction")));
        // index
        iborRateBuilder.index((IborIndex) document.parseIndex(floatingEl));
        // gearing
        floatingEl.findChild("floatingRateMultiplierSchedule").ifPresent(el -> {
          iborRateBuilder.gearing(parseSchedule(el, document));
        });
        // spread
        if (floatingEl.getChildren("spreadSchedule").size() > 1) {
          throw new FpmlParseException("Only one 'spreadSchedule' is supported");
        }
        floatingEl.findChild("spreadSchedule").ifPresent(el -> {
          document.validateNotPresent(el, "type");
          iborRateBuilder.spread(parseSchedule(el, document));
        });
        // initial fixed rate
        floatingEl.findChild("initialRate").ifPresent(el -> {
          iborRateBuilder.firstRegularRate(document.parseDecimal(el));
        });
        // negative rates
        floatingEl.findChild("negativeInterestRateTreatment").ifPresent(el -> {
          iborRateBuilder.negativeRateMethod(parseNegativeInterestRateTreatment(el));
        });
        // resets
        XmlElement resetDatesEl = legEl.getChild("resetDates");
        document.validateNotPresent(resetDatesEl, "initialFixingDate");
        document.validateNotPresent(resetDatesEl, "rateCutOffDaysOffset");
        resetDatesEl.findChild("resetRelativeTo").ifPresent(el -> {
          iborRateBuilder.fixingRelativeTo(parseResetRelativeTo(el));
        });
        // fixing date offset
        iborRateBuilder.fixingDateOffset(document.parseRelativeDateOffsetDays(resetDatesEl.getChild("fixingDates")));
        Frequency resetFreq = document.parseFrequency(resetDatesEl.getChild("resetFrequency"));
        if (!accrualSchedule.getFrequency().equals(resetFreq)) {
          ResetSchedule.Builder resetScheduleBuilder = ResetSchedule.builder();
          resetScheduleBuilder.resetFrequency(resetFreq);
          floatingEl.findChild("averagingMethod").ifPresent(el -> {
            resetScheduleBuilder.averagingMethod(parseAveragingMethod(el));
          });
          resetScheduleBuilder.businessDayAdjustment(
              document.parseBusinessDayAdjustments(resetDatesEl.getChild("resetDatesAdjustments")));
          iborRateBuilder.resetPeriods(resetScheduleBuilder.build());
        }
        // stubs
        legEl.findChild("stubCalculationPeriodAmount").ifPresent(stubsEl -> {
          stubsEl.findChild("initialStub").ifPresent(el -> {
            iborRateBuilder.initialStub(parseStubCalculation(el, document));
          });
          stubsEl.findChild("finalStub").ifPresent(el -> {
            iborRateBuilder.finalStub(parseStubCalculation(el, document));
          });
        });
        return iborRateBuilder.build();

      } else if (index instanceof OvernightIndex) {
        OvernightRateCalculation.Builder overnightRateBuilder = OvernightRateCalculation.builder();
        document.validateNotPresent(legEl, "stubCalculationPeriodAmount");
        document.validateNotPresent(floatingEl, "initialRate");  // TODO: should support this in the model
        // day count
        overnightRateBuilder.dayCount(document.parseDayCountFraction(calcEl.getChild("dayCountFraction")));
        // index
        overnightRateBuilder.index((OvernightIndex) document.parseIndex(floatingEl));
        // accrual method
        FloatingRateName idx = FloatingRateName.of(floatingEl.getChild("floatingRateIndex").getContent());
        if (idx.getType() == FloatingRateType.OVERNIGHT_COMPOUNDED) {
          overnightRateBuilder.accrualMethod(OvernightAccrualMethod.COMPOUNDED);
        }
        // gearing
        floatingEl.findChild("floatingRateMultiplierSchedule").ifPresent(el -> {
          overnightRateBuilder.gearing(parseSchedule(el, document));
        });
        // spread
        if (floatingEl.getChildren("spreadSchedule").size() > 1) {
          throw new FpmlParseException("Only one 'spreadSchedule' is supported");
        }
        floatingEl.findChild("spreadSchedule").ifPresent(el -> {
          document.validateNotPresent(el, "type");
          overnightRateBuilder.spread(parseSchedule(el, document));
        });
        // negative rates
        floatingEl.findChild("negativeInterestRateTreatment").ifPresent(el -> {
          overnightRateBuilder.negativeRateMethod(parseNegativeInterestRateTreatment(el));
        });
        // rate cut off
        XmlElement resetDatesEl = legEl.getChild("resetDates");
        document.validateNotPresent(resetDatesEl, "initialFixingDate");
        resetDatesEl.findChild("rateCutOffDaysOffset").ifPresent(el -> {
          Period cutOff = document.parsePeriod(el);
          if (cutOff.toTotalMonths() != 0) {
            throw new FpmlParseException("Invalid 'rateCutOffDaysOffset' value, expected days-based period: " + cutOff);
          }
          overnightRateBuilder.rateCutOffDays(-cutOff.getDays());
        });
        return overnightRateBuilder.build();

      } else {
        throw new FpmlParseException("Invalid 'floatingRateIndex' type, not Ibor or Overnight");
      }

    } else {
      throw new FpmlParseException("Invalid 'calculation' type, not fixedRateSchedule or floatingRateCalculation");
    }
  }

  //-------------------------------------------------------------------------
  // Converts an FpML 'StubValue' to a {@code StubCalculation}.
  private StubCalculation parseStubCalculation(XmlElement baseEl, FpmlDocument document) {
    document.validateNotPresent(baseEl, "stubAmount");
    Optional<XmlElement> rateOptEl = baseEl.findChild("stubRate");
    if (rateOptEl.isPresent()) {
      return StubCalculation.ofFixedRate(document.parseDecimal(rateOptEl.get()));
    }
    List<XmlElement> indicesEls = baseEl.getChildren("floatingRate");
    if (indicesEls.size() == 1) {
      XmlElement indexEl = indicesEls.get(0);
      document.validateNotPresent(indexEl, "floatingRateMultiplierSchedule");
      document.validateNotPresent(indexEl, "spreadSchedule");
      document.validateNotPresent(indexEl, "rateTreatment");
      document.validateNotPresent(indexEl, "capRateSchedule");
      document.validateNotPresent(indexEl, "floorRateSchedule");
      return StubCalculation.ofIborRate((IborIndex) document.parseIndex(indexEl));
    } else if (indicesEls.size() == 2) {
      XmlElement index1El = indicesEls.get(0);
      document.validateNotPresent(index1El, "floatingRateMultiplierSchedule");
      document.validateNotPresent(index1El, "spreadSchedule");
      document.validateNotPresent(index1El, "rateTreatment");
      document.validateNotPresent(index1El, "capRateSchedule");
      document.validateNotPresent(index1El, "floorRateSchedule");
      XmlElement index2El = indicesEls.get(1);
      document.validateNotPresent(index2El, "floatingRateMultiplierSchedule");
      document.validateNotPresent(index2El, "spreadSchedule");
      document.validateNotPresent(index2El, "rateTreatment");
      document.validateNotPresent(index2El, "capRateSchedule");
      document.validateNotPresent(index2El, "floorRateSchedule");
      return StubCalculation.ofIborInterpolatedRate(
          (IborIndex) document.parseIndex(index1El),
          (IborIndex) document.parseIndex(index2El));
    }
    throw new FpmlParseException("Unknown stub structure: " + baseEl);
  }

  //-------------------------------------------------------------------------
  // Converts an FpML 'StubPeriodTypeEnum' to a {@code StubConvention}.
  private StubConvention parseStubConvention(XmlElement baseEl, FpmlDocument document) {
    if (baseEl.getContent().equals("ShortInitial")) {
      return StubConvention.SHORT_INITIAL;
    } else if (baseEl.getContent().equals("ShortFinal")) {
      return StubConvention.SHORT_FINAL;
    } else if (baseEl.getContent().equals("LongInitial")) {
      return StubConvention.LONG_INITIAL;
    } else if (baseEl.getContent().equals("LongFinal")) {
      return StubConvention.LONG_FINAL;
    } else {
      throw new FpmlParseException(Messages.format("Unknown 'stubPeriodType': {}", baseEl.getContent()));
    }
  }

  // Converts an FpML 'Schedule' to a {@code ValueSchedule}.
  private ValueSchedule parseSchedule(XmlElement notionalScheduleEl, FpmlDocument document) {
    // FpML content: ('initialValue', 'step*')
    // FpML 'step' content: ('stepDate', 'stepValue')
    double initialValue = document.parseDecimal(notionalScheduleEl.getChild("initialValue"));
    List<XmlElement> stepEls = notionalScheduleEl.getChildren("step");
    ImmutableList.Builder<ValueStep> stepBuilder = ImmutableList.builder();
    for (XmlElement stepEl : stepEls) {
      LocalDate stepDate = document.parseDate(stepEl.getChild("stepDate"));
      double stepValue = document.parseDecimal(stepEl.getChild("stepValue"));
      stepBuilder.add(ValueStep.of(stepDate, ValueAdjustment.ofReplace(stepValue)));
    }
    return ValueSchedule.of(initialValue, stepBuilder.build());
  }

  //-------------------------------------------------------------------------
  // Converts an FpML 'PayRelativeToEnum' to a {@code PaymentRelativeTo}.
  private PaymentRelativeTo parsePayRelativeTo(XmlElement baseEl) {
    if (baseEl.getContent().equals("CalculationPeriodStartDate")) {
      return PaymentRelativeTo.PERIOD_START;
    } else if (baseEl.getContent().equals("CalculationPeriodEndDate")) {
      return PaymentRelativeTo.PERIOD_END;
    } else {
      throw new FpmlParseException(Messages.format("Unknown 'payRelativeTo': {}", baseEl.getContent()));
    }
  }

  // Converts and FpML 'NegativeInterestRateTreatmentEnum' to a {@code NegativeRateMethod}.
  private NegativeRateMethod parseNegativeInterestRateTreatment(XmlElement baseEl) {
    if (baseEl.getContent().equals("NegativeInterestRateMethod")) {
      return NegativeRateMethod.ALLOW_NEGATIVE;
    } else if (baseEl.getContent().equals("ZeroInterestRateMethod")) {
      return NegativeRateMethod.NOT_NEGATIVE;
    } else {
      throw new FpmlParseException(Messages.format("Unknown 'negativeInterestRateTreatment': {}", baseEl.getContent()));
    }
  }

  // Converts an FpML 'AveragingMethodEnum' to a {@code IborRateAveragingMethod}.
  private IborRateAveragingMethod parseAveragingMethod(XmlElement baseEl) {
    if (baseEl.getContent().equals("Unweighted")) {
      return IborRateAveragingMethod.UNWEIGHTED;
    } else if (baseEl.getContent().equals("Weighted")) {
      return IborRateAveragingMethod.WEIGHTED;
    } else {
      throw new FpmlParseException(Messages.format("Unknown 'averagingMethod': {}", baseEl.getContent()));
    }
  }

  // Converts an FpML 'ResetRelativeToEnum' to a {@code FixingRelativeTo}.
  private FixingRelativeTo parseResetRelativeTo(XmlElement baseEl) {
    if (baseEl.getContent().equals("CalculationPeriodStartDate")) {
      return FixingRelativeTo.PERIOD_START;
    } else if (baseEl.getContent().equals("CalculationPeriodEndDate")) {
      return FixingRelativeTo.PERIOD_END;
    } else {
      throw new FpmlParseException(Messages.format("Unknown 'resetRelativeTo': {}", baseEl.getContent()));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String getName() {
    return "swap";
  }

}
