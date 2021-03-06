/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.market.value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableDefaults;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.strata.basics.index.IborIndex;
import com.opengamma.strata.basics.market.Perturbation;
import com.opengamma.strata.collect.Messages;
import com.opengamma.strata.collect.timeseries.LocalDateDoubleTimeSeries;
import com.opengamma.strata.market.curve.Curve;
import com.opengamma.strata.market.curve.CurveName;
import com.opengamma.strata.market.sensitivity.CurveCurrencyParameterSensitivities;
import com.opengamma.strata.market.sensitivity.CurveUnitParameterSensitivities;
import com.opengamma.strata.market.sensitivity.IborRateSensitivity;
import com.opengamma.strata.market.sensitivity.PointSensitivityBuilder;

/**
 * An Ibor index curve providing rates from discount factors.
 * <p>
 * This provides historic and forward rates for a single {@link IborIndex}, such as 'GBP-LIBOR-3M'.
 * <p>
 * This implementation is based on an underlying curve that is stored with maturities
 * and zero-coupon continuously-compounded rates.
 */
@BeanDefinition(builderScope = "private")
public final class DiscountIborIndexRates
    implements IborIndexRates, ImmutableBean, Serializable {

  /**
   * The index that the rates are for.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final IborIndex index;
  /**
   * The time-series.
   * This covers known historical fixings and may be empty.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDateDoubleTimeSeries timeSeries;
  /**
   * The underlying discount factor curve.
   */
  @PropertyDefinition(validate = "notNull")
  private final DiscountFactors discountFactors;

  //-------------------------------------------------------------------------
  /**
   * Creates a new Ibor index rates instance with no historic fixings.
   * <p>
   * The forward curve is specified by an instance of {@link DiscountFactors}.
   * 
   * @param index  the Ibor index
   * @param discountFactors  the underlying discount factor forward curve
   * @return the rates instance
   */
  public static DiscountIborIndexRates of(IborIndex index, DiscountFactors discountFactors) {
    return of(index, LocalDateDoubleTimeSeries.empty(), discountFactors);
  }

  /**
   * Creates a new Ibor index rates instance.
   * <p>
   * The forward curve is specified by an instance of {@link DiscountFactors}.
   * 
   * @param index  the Ibor index
   * @param fixings  the known historical fixings
   * @param discountFactors  the underlying discount factor forward curve
   * @return the rates instance
   */
  public static DiscountIborIndexRates of(
      IborIndex index,
      LocalDateDoubleTimeSeries fixings,
      DiscountFactors discountFactors) {

    return new DiscountIborIndexRates(index, fixings, discountFactors);
  }

  //-------------------------------------------------------------------------
  @ImmutableDefaults
  private static void applyDefaults(Builder builder) {
    builder.timeSeries = LocalDateDoubleTimeSeries.empty();
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getValuationDate() {
    return discountFactors.getValuationDate();
  }

  @Override
  public CurveName getCurveName() {
    return discountFactors.getCurveName();
  }

  @Override
  public int getParameterCount() {
    return discountFactors.getParameterCount();
  }

  //-------------------------------------------------------------------------
  @Override
  public double rate(LocalDate fixingDate) {
    if (!fixingDate.isAfter(getValuationDate())) {
      return historicRate(fixingDate);
    }
    return forwardRate(fixingDate);
  }

  // historic rate
  private double historicRate(LocalDate fixingDate) {
    OptionalDouble fixedRate = timeSeries.get(fixingDate);
    if (fixedRate.isPresent()) {
      return fixedRate.getAsDouble();
    } else if (fixingDate.isBefore(getValuationDate())) { // the fixing is required
      if (timeSeries.isEmpty()) {
        throw new IllegalArgumentException(
            Messages.format("Unable to get fixing for {} on date {}, no time-series supplied", index, fixingDate));
      }
      throw new IllegalArgumentException(Messages.format("Unable to get fixing for {} on date {}", index, fixingDate));
    } else {
      return forwardRate(fixingDate);
    }
  }

  // forward rate
  private double forwardRate(LocalDate fixingDate) {
    LocalDate fixingStartDate = index.calculateEffectiveFromFixing(fixingDate);
    LocalDate fixingEndDate = index.calculateMaturityFromEffective(fixingStartDate);
    double fixingYearFraction = index.getDayCount().yearFraction(fixingStartDate, fixingEndDate);
    return simplyCompoundForwardRate(fixingStartDate, fixingEndDate, fixingYearFraction);
  }

  // compounded from discount factors
  private double simplyCompoundForwardRate(LocalDate startDate, LocalDate endDate, double accrualFactor) {
    return (discountFactors.discountFactor(startDate) / discountFactors.discountFactor(endDate) - 1) / accrualFactor;
  }

  //-------------------------------------------------------------------------
  @Override
  public PointSensitivityBuilder ratePointSensitivity(LocalDate fixingDate) {
    LocalDate valuationDate = getValuationDate();
    if (fixingDate.isBefore(valuationDate) ||
        (fixingDate.equals(valuationDate) && timeSeries.get(fixingDate).isPresent())) {
      return PointSensitivityBuilder.none();
    }
    return IborRateSensitivity.of(index, fixingDate, 1d);
  }

  //-------------------------------------------------------------------------
  @Override
  public CurveUnitParameterSensitivities unitParameterSensitivity(LocalDate fixingDate) {
    LocalDate valuationDate = getValuationDate();
    if (fixingDate.isBefore(valuationDate) ||
        (fixingDate.equals(valuationDate) && timeSeries.get(fixingDate).isPresent())) {
      return CurveUnitParameterSensitivities.empty();
    }
    return discountFactors.unitParameterSensitivity(fixingDate);
  }

  //-------------------------------------------------------------------------
  @Override
  public CurveCurrencyParameterSensitivities curveParameterSensitivity(IborRateSensitivity pointSensitivity) {
    IborIndex index = pointSensitivity.getIndex();
    LocalDate startDate = index.calculateEffectiveFromFixing(pointSensitivity.getFixingDate());
    LocalDate endDate = index.calculateMaturityFromEffective(startDate);
    double accrualFactor = index.getDayCount().yearFraction(startDate, endDate);
    double forwardBar = pointSensitivity.getSensitivity();
    double dfForwardStart = discountFactors.discountFactor(startDate);
    double dfForwardEnd = discountFactors.discountFactor(endDate);
    double dfStartBar = forwardBar / (accrualFactor * dfForwardEnd);
    double dfEndBar = -forwardBar * dfForwardStart / (accrualFactor * dfForwardEnd * dfForwardEnd);
    double zrStartBar = discountFactors.zeroRatePointSensitivity(startDate).getSensitivity() * dfStartBar;
    double zrEndBar = discountFactors.zeroRatePointSensitivity(endDate).getSensitivity() * dfEndBar;
    CurveUnitParameterSensitivities dzrdpStart = discountFactors.unitParameterSensitivity(startDate);
    CurveUnitParameterSensitivities dzrdpEnd = discountFactors.unitParameterSensitivity(endDate);
    // combine unit and point sensitivities at start and end
    CurveCurrencyParameterSensitivities sensStart = dzrdpStart.multipliedBy(pointSensitivity.getCurrency(), zrStartBar);
    CurveCurrencyParameterSensitivities sensEnd = dzrdpEnd.multipliedBy(pointSensitivity.getCurrency(), zrEndBar);
    return sensStart.combinedWith(sensEnd);
  }

  //-------------------------------------------------------------------------
  @Override
  public DiscountIborIndexRates applyPerturbation(Perturbation<Curve> perturbation) {
    return withDiscountFactors(discountFactors.applyPerturbation(perturbation));
  }

  /**
   * Returns a new instance with different discount factors.
   * 
   * @param factors  the new discount factors
   * @return the new instance
   */
  public DiscountIborIndexRates withDiscountFactors(DiscountFactors factors) {
    return new DiscountIborIndexRates(index, timeSeries, factors);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DiscountIborIndexRates}.
   * @return the meta-bean, not null
   */
  public static DiscountIborIndexRates.Meta meta() {
    return DiscountIborIndexRates.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DiscountIborIndexRates.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  private DiscountIborIndexRates(
      IborIndex index,
      LocalDateDoubleTimeSeries timeSeries,
      DiscountFactors discountFactors) {
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(timeSeries, "timeSeries");
    JodaBeanUtils.notNull(discountFactors, "discountFactors");
    this.index = index;
    this.timeSeries = timeSeries;
    this.discountFactors = discountFactors;
  }

  @Override
  public DiscountIborIndexRates.Meta metaBean() {
    return DiscountIborIndexRates.Meta.INSTANCE;
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
   * Gets the index that the rates are for.
   * @return the value of the property, not null
   */
  @Override
  public IborIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series.
   * This covers known historical fixings and may be empty.
   * @return the value of the property, not null
   */
  @Override
  public LocalDateDoubleTimeSeries getTimeSeries() {
    return timeSeries;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying discount factor curve.
   * @return the value of the property, not null
   */
  public DiscountFactors getDiscountFactors() {
    return discountFactors;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DiscountIborIndexRates other = (DiscountIborIndexRates) obj;
      return JodaBeanUtils.equal(getIndex(), other.getIndex()) &&
          JodaBeanUtils.equal(getTimeSeries(), other.getTimeSeries()) &&
          JodaBeanUtils.equal(getDiscountFactors(), other.getDiscountFactors());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndex());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTimeSeries());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDiscountFactors());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("DiscountIborIndexRates{");
    buf.append("index").append('=').append(getIndex()).append(',').append(' ');
    buf.append("timeSeries").append('=').append(getTimeSeries()).append(',').append(' ');
    buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(getDiscountFactors()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DiscountIborIndexRates}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<IborIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", DiscountIborIndexRates.class, IborIndex.class);
    /**
     * The meta-property for the {@code timeSeries} property.
     */
    private final MetaProperty<LocalDateDoubleTimeSeries> timeSeries = DirectMetaProperty.ofImmutable(
        this, "timeSeries", DiscountIborIndexRates.class, LocalDateDoubleTimeSeries.class);
    /**
     * The meta-property for the {@code discountFactors} property.
     */
    private final MetaProperty<DiscountFactors> discountFactors = DirectMetaProperty.ofImmutable(
        this, "discountFactors", DiscountIborIndexRates.class, DiscountFactors.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "index",
        "timeSeries",
        "discountFactors");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case 779431844:  // timeSeries
          return timeSeries;
        case -91613053:  // discountFactors
          return discountFactors;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DiscountIborIndexRates> builder() {
      return new DiscountIborIndexRates.Builder();
    }

    @Override
    public Class<? extends DiscountIborIndexRates> beanType() {
      return DiscountIborIndexRates.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code index} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IborIndex> index() {
      return index;
    }

    /**
     * The meta-property for the {@code timeSeries} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDateDoubleTimeSeries> timeSeries() {
      return timeSeries;
    }

    /**
     * The meta-property for the {@code discountFactors} property.
     * @return the meta-property, not null
     */
    public MetaProperty<DiscountFactors> discountFactors() {
      return discountFactors;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return ((DiscountIborIndexRates) bean).getIndex();
        case 779431844:  // timeSeries
          return ((DiscountIborIndexRates) bean).getTimeSeries();
        case -91613053:  // discountFactors
          return ((DiscountIborIndexRates) bean).getDiscountFactors();
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
   * The bean-builder for {@code DiscountIborIndexRates}.
   */
  private static final class Builder extends DirectFieldsBeanBuilder<DiscountIborIndexRates> {

    private IborIndex index;
    private LocalDateDoubleTimeSeries timeSeries;
    private DiscountFactors discountFactors;

    /**
     * Restricted constructor.
     */
    private Builder() {
      applyDefaults(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case 779431844:  // timeSeries
          return timeSeries;
        case -91613053:  // discountFactors
          return discountFactors;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          this.index = (IborIndex) newValue;
          break;
        case 779431844:  // timeSeries
          this.timeSeries = (LocalDateDoubleTimeSeries) newValue;
          break;
        case -91613053:  // discountFactors
          this.discountFactors = (DiscountFactors) newValue;
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
    public DiscountIborIndexRates build() {
      return new DiscountIborIndexRates(
          index,
          timeSeries,
          discountFactors);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(128);
      buf.append("DiscountIborIndexRates.Builder{");
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("timeSeries").append('=').append(JodaBeanUtils.toString(timeSeries)).append(',').append(' ');
      buf.append("discountFactors").append('=').append(JodaBeanUtils.toString(discountFactors));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
