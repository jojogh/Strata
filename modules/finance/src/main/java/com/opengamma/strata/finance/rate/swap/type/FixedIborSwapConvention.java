/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.rate.swap.type;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.BuySell;
import com.opengamma.strata.basics.PayReceive;
import com.opengamma.strata.basics.date.DaysAdjustment;
import com.opengamma.strata.basics.date.Tenor;
import com.opengamma.strata.collect.ArgChecker;
import com.opengamma.strata.finance.Convention;
import com.opengamma.strata.finance.TradeInfo;
import com.opengamma.strata.finance.rate.swap.Swap;
import com.opengamma.strata.finance.rate.swap.SwapLeg;
import com.opengamma.strata.finance.rate.swap.SwapTrade;

/**
 * A market convention for Fixed-Ibor swap trades.
 * <p>
 * This defines the market convention for a Fixed-Ibor single currency swap.
 * The convention is formed by combining two swap leg conventions in the same currency.
 * <p>
 * See also {@link FixedIborSwapTemplate}.
 */
@BeanDefinition
public final class FixedIborSwapConvention
    implements Convention, ImmutableBean, Serializable {

  /**
   * The market convention of the fixed leg.
   */
  @PropertyDefinition(validate = "notNull")
  private final FixedRateSwapLegConvention fixedLeg;
  /**
   * The market convention of the floating leg.
   */
  @PropertyDefinition(validate = "notNull")
  private final IborRateSwapLegConvention floatingLeg;
  /**
   * The offset of the spot value date from the trade date, optional with defaulting getter.
   * <p>
   * The offset is applied to the trade date and is typically plus 2 business days.
   * The start date of the swap is relative to the spot date.
   */
  @PropertyDefinition(get = "field")
  private final DaysAdjustment spotDateOffset;

  //-------------------------------------------------------------------------
  /**
   * Creates a convention based on the specified conventions.
   * <p>
   * The spot date offset is set to be the effective date offset of the index.
   * 
   * @param fixedLeg  the market convention for the fixed leg
   * @param floatingLeg  the market convention for the floating leg
   * @return the convention
   */
  public static FixedIborSwapConvention of(FixedRateSwapLegConvention fixedLeg, IborRateSwapLegConvention floatingLeg) {
    return FixedIborSwapConvention.builder()
        .fixedLeg(fixedLeg)
        .floatingLeg(floatingLeg)
        .build();
  }

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    ArgChecker.isTrue(fixedLeg.getCurrency().equals(floatingLeg.getCurrency()), "Conventions must have same currency");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the offset of the spot value date from the trade date,
   * providing a default result if no override specified.
   * <p>
   * The offset is applied to the trade date and is typically plus 2 business days.
   * The start date of the swap is relative to the spot date.
   * <p>
   * This will default to the effective date offset of the index if not specified.
   * 
   * @return the spot date offset, not null
   */
  public DaysAdjustment getSpotDateOffset() {
    return spotDateOffset != null ? spotDateOffset : floatingLeg.getIndex().getEffectiveDateOffset();
  }

  //-------------------------------------------------------------------------
  /**
   * Expands this convention, returning an instance where all the optional fields are present.
   * <p>
   * This returns an equivalent instance where any empty optional have been filled in.
   * 
   * @return the expanded convention
   */
  public FixedIborSwapConvention expand() {
    return FixedIborSwapConvention.builder()
        .fixedLeg(fixedLeg.expand())
        .floatingLeg(floatingLeg.expand())
        .spotDateOffset(getSpotDateOffset())
        .build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a spot-starting trade based on this convention.
   * <p>
   * This returns a trade based on the specified tenor. For example, a tenor
   * of 5 years creates a swap starting on the spot date and maturing 5 years later.
   * <p>
   * The notional is unsigned, with buy/sell determining the direction of the trade.
   * If buying the swap, the floating rate is received from the counterparty, with the fixed rate being paid.
   * If selling the swap, the floating rate is paid to the counterparty, with the fixed rate being received.
   * 
   * @param tradeDate  the date of the trade
   * @param tenor  the tenor of the swap
   * @param buySell  the buy/sell flag
   * @param notional  the notional amount, in the payment currency of the template
   * @param fixedRate  the fixed rate, typically derived from the market
   * @return the trade
   */
  public SwapTrade toTrade(
      LocalDate tradeDate,
      Tenor tenor,
      BuySell buySell,
      double notional,
      double fixedRate) {

    return toTrade(tradeDate, Period.ZERO, tenor, buySell, notional, fixedRate);
  }

  /**
   * Creates a forward-starting trade based on this convention.
   * <p>
   * This returns a trade based on the specified period and tenor. For example, a period of
   * 3 months and a tenor of 5 years creates a swap starting three months after the spot date
   * and maturing 5 years later.
   * <p>
   * The notional is unsigned, with buy/sell determining the direction of the trade.
   * If buying the swap, the floating rate is received from the counterparty, with the fixed rate being paid.
   * If selling the swap, the floating rate is paid to the counterparty, with the fixed rate being received.
   * 
   * @param tradeDate  the date of the trade
   * @param periodToStart  the period between the spot date and the start date
   * @param tenor  the tenor of the swap
   * @param buySell  the buy/sell flag
   * @param notional  the notional amount, in the payment currency of the template
   * @param fixedRate  the fixed rate, typically derived from the market
   * @return the trade
   */
  public SwapTrade toTrade(
      LocalDate tradeDate,
      Period periodToStart,
      Tenor tenor,
      BuySell buySell,
      double notional,
      double fixedRate) {

    LocalDate spotValue = getSpotDateOffset().adjust(tradeDate);
    LocalDate startDate = spotValue.plus(periodToStart);
    LocalDate endDate = startDate.plus(tenor.getPeriod());
    return toTrade(tradeDate, startDate, endDate, buySell, notional, fixedRate);
  }

  /**
   * Creates a trade based on this convention.
   * <p>
   * This returns a trade based on the specified dates.
   * <p>
   * The notional is unsigned, with buy/sell determining the direction of the trade.
   * If buying the swap, the floating rate is received from the counterparty, with the fixed rate being paid.
   * If selling the swap, the floating rate is paid to the counterparty, with the fixed rate being received.
   * 
   * @param tradeDate  the date of the trade
   * @param startDate  the start date
   * @param endDate  the end date
   * @param buySell  the buy/sell flag
   * @param notional  the notional amount, in the payment currency of the template
   * @param fixedRate  the fixed rate, typically derived from the market
   * @return the trade
   */
  public SwapTrade toTrade(
      LocalDate tradeDate,
      LocalDate startDate,
      LocalDate endDate,
      BuySell buySell,
      double notional,
      double fixedRate) {

    ArgChecker.inOrderOrEqual(tradeDate, startDate, "tradeDate", "startDate");
    SwapLeg leg1 = fixedLeg.toLeg(startDate, endDate, PayReceive.ofPay(buySell.isBuy()), notional, fixedRate);
    SwapLeg leg2 = floatingLeg.toLeg(startDate, endDate, PayReceive.ofPay(buySell.isSell()), notional);
    return SwapTrade.builder()
        .tradeInfo(TradeInfo.builder()
            .tradeDate(tradeDate)
            .build())
        .product(Swap.of(leg1, leg2))
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FixedIborSwapConvention}.
   * @return the meta-bean, not null
   */
  public static FixedIborSwapConvention.Meta meta() {
    return FixedIborSwapConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FixedIborSwapConvention.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FixedIborSwapConvention.Builder builder() {
    return new FixedIborSwapConvention.Builder();
  }

  private FixedIborSwapConvention(
      FixedRateSwapLegConvention fixedLeg,
      IborRateSwapLegConvention floatingLeg,
      DaysAdjustment spotDateOffset) {
    JodaBeanUtils.notNull(fixedLeg, "fixedLeg");
    JodaBeanUtils.notNull(floatingLeg, "floatingLeg");
    this.fixedLeg = fixedLeg;
    this.floatingLeg = floatingLeg;
    this.spotDateOffset = spotDateOffset;
    validate();
  }

  @Override
  public FixedIborSwapConvention.Meta metaBean() {
    return FixedIborSwapConvention.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market convention of the fixed leg.
   * @return the value of the property, not null
   */
  public FixedRateSwapLegConvention getFixedLeg() {
    return fixedLeg;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the market convention of the floating leg.
   * @return the value of the property, not null
   */
  public IborRateSwapLegConvention getFloatingLeg() {
    return floatingLeg;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FixedIborSwapConvention other = (FixedIborSwapConvention) obj;
      return JodaBeanUtils.equal(getFixedLeg(), other.getFixedLeg()) &&
          JodaBeanUtils.equal(getFloatingLeg(), other.getFloatingLeg()) &&
          JodaBeanUtils.equal(spotDateOffset, other.spotDateOffset);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getFixedLeg());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFloatingLeg());
    hash = hash * 31 + JodaBeanUtils.hashCode(spotDateOffset);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FixedIborSwapConvention{");
    buf.append("fixedLeg").append('=').append(getFixedLeg()).append(',').append(' ');
    buf.append("floatingLeg").append('=').append(getFloatingLeg()).append(',').append(' ');
    buf.append("spotDateOffset").append('=').append(JodaBeanUtils.toString(spotDateOffset));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FixedIborSwapConvention}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code fixedLeg} property.
     */
    private final MetaProperty<FixedRateSwapLegConvention> fixedLeg = DirectMetaProperty.ofImmutable(
        this, "fixedLeg", FixedIborSwapConvention.class, FixedRateSwapLegConvention.class);
    /**
     * The meta-property for the {@code floatingLeg} property.
     */
    private final MetaProperty<IborRateSwapLegConvention> floatingLeg = DirectMetaProperty.ofImmutable(
        this, "floatingLeg", FixedIborSwapConvention.class, IborRateSwapLegConvention.class);
    /**
     * The meta-property for the {@code spotDateOffset} property.
     */
    private final MetaProperty<DaysAdjustment> spotDateOffset = DirectMetaProperty.ofImmutable(
        this, "spotDateOffset", FixedIborSwapConvention.class, DaysAdjustment.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "fixedLeg",
        "floatingLeg",
        "spotDateOffset");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -391537158:  // fixedLeg
          return fixedLeg;
        case -1177101272:  // floatingLeg
          return floatingLeg;
        case 746995843:  // spotDateOffset
          return spotDateOffset;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FixedIborSwapConvention.Builder builder() {
      return new FixedIborSwapConvention.Builder();
    }

    @Override
    public Class<? extends FixedIborSwapConvention> beanType() {
      return FixedIborSwapConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code fixedLeg} property.
     * @return the meta-property, not null
     */
    public MetaProperty<FixedRateSwapLegConvention> fixedLeg() {
      return fixedLeg;
    }

    /**
     * The meta-property for the {@code floatingLeg} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IborRateSwapLegConvention> floatingLeg() {
      return floatingLeg;
    }

    /**
     * The meta-property for the {@code spotDateOffset} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DaysAdjustment> spotDateOffset() {
      return spotDateOffset;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -391537158:  // fixedLeg
          return ((FixedIborSwapConvention) bean).getFixedLeg();
        case -1177101272:  // floatingLeg
          return ((FixedIborSwapConvention) bean).getFloatingLeg();
        case 746995843:  // spotDateOffset
          return ((FixedIborSwapConvention) bean).spotDateOffset;
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code FixedIborSwapConvention}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<FixedIborSwapConvention> {

    private FixedRateSwapLegConvention fixedLeg;
    private IborRateSwapLegConvention floatingLeg;
    private DaysAdjustment spotDateOffset;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(FixedIborSwapConvention beanToCopy) {
      this.fixedLeg = beanToCopy.getFixedLeg();
      this.floatingLeg = beanToCopy.getFloatingLeg();
      this.spotDateOffset = beanToCopy.spotDateOffset;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -391537158:  // fixedLeg
          return fixedLeg;
        case -1177101272:  // floatingLeg
          return floatingLeg;
        case 746995843:  // spotDateOffset
          return spotDateOffset;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -391537158:  // fixedLeg
          this.fixedLeg = (FixedRateSwapLegConvention) newValue;
          break;
        case -1177101272:  // floatingLeg
          this.floatingLeg = (IborRateSwapLegConvention) newValue;
          break;
        case 746995843:  // spotDateOffset
          this.spotDateOffset = (DaysAdjustment) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FixedIborSwapConvention build() {
      return new FixedIborSwapConvention(
          fixedLeg,
          floatingLeg,
          spotDateOffset);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the market convention of the fixed leg.
     * @param fixedLeg  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fixedLeg(FixedRateSwapLegConvention fixedLeg) {
      JodaBeanUtils.notNull(fixedLeg, "fixedLeg");
      this.fixedLeg = fixedLeg;
      return this;
    }

    /**
     * Sets the market convention of the floating leg.
     * @param floatingLeg  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder floatingLeg(IborRateSwapLegConvention floatingLeg) {
      JodaBeanUtils.notNull(floatingLeg, "floatingLeg");
      this.floatingLeg = floatingLeg;
      return this;
    }

    /**
     * Sets the offset of the spot value date from the trade date, optional with defaulting getter.
     * <p>
     * The offset is applied to the trade date and is typically plus 2 business days.
     * The start date of the swap is relative to the spot date.
     * @param spotDateOffset  the new value
     * @return this, for chaining, not null
     */
    public Builder spotDateOffset(DaysAdjustment spotDateOffset) {
      this.spotDateOffset = spotDateOffset;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("FixedIborSwapConvention.Builder{");
      buf.append("fixedLeg").append('=').append(JodaBeanUtils.toString(fixedLeg)).append(',').append(' ');
      buf.append("floatingLeg").append('=').append(JodaBeanUtils.toString(floatingLeg)).append(',').append(' ');
      buf.append("spotDateOffset").append('=').append(JodaBeanUtils.toString(spotDateOffset));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
