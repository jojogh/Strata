/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.strata.pricer.rate.swaption;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.strata.basics.date.DayCount;
import com.opengamma.strata.finance.rate.swap.type.FixedIborSwapConvention;

/**
 * Volatility environment for swaptions in the log-normal or Black model. 
 * The volatility is represented by a surface on the expiration and swap tenor dimensions.
 * The expiry is measured in days according to a day count convention; the expiry time is ignored.
 */
@BeanDefinition(builderScope = "private")
public class BlackVolatilityExpiryTenorSwaptionProvider
  implements  BlackVolatilitySwaptionProvider, ImmutableBean, Serializable { 
  
  /** The normal volatility surface. The order of the dimensions is expiry/swap tenor */
  @PropertyDefinition(validate = "notNull")
  private final InterpolatedDoublesSurface surface;
  /** The swap convention for which the data is valid. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final FixedIborSwapConvention convention;
  /** The day count applicable to the model. */
  @PropertyDefinition(validate = "notNull")
  private final DayCount dayCount;
  /** The valuation date. All data items in this environment are calibrated for this date. */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final LocalDate valuationDate;
  /** The valuation time. All data items in this environment are calibrated for this time. */
  @PropertyDefinition(validate = "notNull")
  private final LocalTime valuationTime;
  /** The valuation zone.*/
  @PropertyDefinition(validate = "notNull")
  private final ZoneId valuationZone;
  
  /**
   * Creates a provider from the implied volatility surface and the date, time and zone for which it is valid.
   * @param surface  the implied volatility surface
   * @param convention  the swap convention for which the data is valid
   * @param dayCount  the day count applicable to the model
   * @param valuationDate  the valuation date
   * @param valuationTime  the valuation time
   * @param valuationZone  the valuation time zone
   * @return the provider
   */
  public static BlackVolatilityExpiryTenorSwaptionProvider of(InterpolatedDoublesSurface surface, 
      FixedIborSwapConvention convention, DayCount dayCount, 
      LocalDate valuationDate, LocalTime valuationTime, ZoneId valuationZone) {
    return new BlackVolatilityExpiryTenorSwaptionProvider(surface, convention, dayCount, valuationDate, 
        valuationTime, valuationZone);
  }
  
  /**
   * Creates a provider from the implied volatility surface and the date. 
   * The valuation time and zone are defaulted to noon UTC.
   * @param surface  the implied volatility surface
   * @param convention  the swap convention for which the data is valid
   * @param dayCount  the day count applicable to the model
   * @param valuationDate  the valuation date
   * @return the provider
   */
  public static BlackVolatilityExpiryTenorSwaptionProvider of(InterpolatedDoublesSurface surface, 
      FixedIborSwapConvention convention, DayCount dayCount, LocalDate valuationDate) {
    return new BlackVolatilityExpiryTenorSwaptionProvider(surface, convention, dayCount, valuationDate, LocalTime.NOON,
        ZoneOffset.UTC);
  }
  
  private BlackVolatilityExpiryTenorSwaptionProvider(InterpolatedDoublesSurface surface, 
      FixedIborSwapConvention convention, DayCount dayCount, LocalDate valuationDate, LocalTime valuationTime, 
      ZoneId valuationZone) {
    this.surface = surface;
    this.convention = convention;
    this.dayCount = dayCount;
    this.valuationDate = valuationDate;
    this.valuationTime = valuationTime;
    this.valuationZone = valuationZone;
  }  

  @Override
  public double getVolatility(ZonedDateTime expiryDate, double tenor, double strike, double forwardRate) {
    double expiryTime = relativeYearFraction(expiryDate);
    double volatility = surface.getZValue(expiryTime, tenor);
    return volatility;
  }

  @Override
  public double relativeYearFraction(ZonedDateTime dateTime) {
    LocalDate date = dateTime.toLocalDate();
    boolean timeIsNegative = valuationDate.isAfter(date);
    if (timeIsNegative) {
      return -dayCount.yearFraction(date, valuationDate);
    }
    return dayCount.yearFraction(valuationDate, date);
  }

  @Override
  public double tenor(LocalDate startDate, LocalDate endDate) {
    // rounded number of months. the rounding is to ensure that an integer number of year even with holidays/leap year
    return Math.round((endDate.toEpochDay() - startDate.toEpochDay()) / 365.25 * 12) / 12  ;
  }
  
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BlackVolatilityExpiryTenorSwaptionProvider}.
   * @return the meta-bean, not null
   */
  public static BlackVolatilityExpiryTenorSwaptionProvider.Meta meta() {
    return BlackVolatilityExpiryTenorSwaptionProvider.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BlackVolatilityExpiryTenorSwaptionProvider.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected BlackVolatilityExpiryTenorSwaptionProvider(BlackVolatilityExpiryTenorSwaptionProvider.Builder builder) {
    JodaBeanUtils.notNull(builder.surface, "surface");
    JodaBeanUtils.notNull(builder.convention, "convention");
    JodaBeanUtils.notNull(builder.dayCount, "dayCount");
    JodaBeanUtils.notNull(builder.valuationDate, "valuationDate");
    JodaBeanUtils.notNull(builder.valuationTime, "valuationTime");
    JodaBeanUtils.notNull(builder.valuationZone, "valuationZone");
    this.surface = builder.surface;
    this.convention = builder.convention;
    this.dayCount = builder.dayCount;
    this.valuationDate = builder.valuationDate;
    this.valuationTime = builder.valuationTime;
    this.valuationZone = builder.valuationZone;
  }

  @Override
  public BlackVolatilityExpiryTenorSwaptionProvider.Meta metaBean() {
    return BlackVolatilityExpiryTenorSwaptionProvider.Meta.INSTANCE;
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
   * Gets the normal volatility surface. The order of the dimensions is expiry/swap tenor
   * @return the value of the property, not null
   */
  public InterpolatedDoublesSurface getSurface() {
    return surface;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the swap convention for which the data is valid.
   * @return the value of the property, not null
   */
  @Override
  public FixedIborSwapConvention getConvention() {
    return convention;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count applicable to the model.
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return dayCount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation date. All data items in this environment are calibrated for this date.
   * @return the value of the property, not null
   */
  @Override
  public LocalDate getValuationDate() {
    return valuationDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation time. All data items in this environment are calibrated for this time.
   * @return the value of the property, not null
   */
  public LocalTime getValuationTime() {
    return valuationTime;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the valuation zone.
   * @return the value of the property, not null
   */
  public ZoneId getValuationZone() {
    return valuationZone;
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BlackVolatilityExpiryTenorSwaptionProvider other = (BlackVolatilityExpiryTenorSwaptionProvider) obj;
      return JodaBeanUtils.equal(getSurface(), other.getSurface()) &&
          JodaBeanUtils.equal(getConvention(), other.getConvention()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getValuationDate(), other.getValuationDate()) &&
          JodaBeanUtils.equal(getValuationTime(), other.getValuationTime()) &&
          JodaBeanUtils.equal(getValuationZone(), other.getValuationZone());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getSurface());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConvention());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValuationZone());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("BlackVolatilityExpiryTenorSwaptionProvider{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("surface").append('=').append(JodaBeanUtils.toString(getSurface())).append(',').append(' ');
    buf.append("convention").append('=').append(JodaBeanUtils.toString(getConvention())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(getValuationDate())).append(',').append(' ');
    buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(getValuationTime())).append(',').append(' ');
    buf.append("valuationZone").append('=').append(JodaBeanUtils.toString(getValuationZone())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BlackVolatilityExpiryTenorSwaptionProvider}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code surface} property.
     */
    private final MetaProperty<InterpolatedDoublesSurface> surface = DirectMetaProperty.ofImmutable(
        this, "surface", BlackVolatilityExpiryTenorSwaptionProvider.class, InterpolatedDoublesSurface.class);
    /**
     * The meta-property for the {@code convention} property.
     */
    private final MetaProperty<FixedIborSwapConvention> convention = DirectMetaProperty.ofImmutable(
        this, "convention", BlackVolatilityExpiryTenorSwaptionProvider.class, FixedIborSwapConvention.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> dayCount = DirectMetaProperty.ofImmutable(
        this, "dayCount", BlackVolatilityExpiryTenorSwaptionProvider.class, DayCount.class);
    /**
     * The meta-property for the {@code valuationDate} property.
     */
    private final MetaProperty<LocalDate> valuationDate = DirectMetaProperty.ofImmutable(
        this, "valuationDate", BlackVolatilityExpiryTenorSwaptionProvider.class, LocalDate.class);
    /**
     * The meta-property for the {@code valuationTime} property.
     */
    private final MetaProperty<LocalTime> valuationTime = DirectMetaProperty.ofImmutable(
        this, "valuationTime", BlackVolatilityExpiryTenorSwaptionProvider.class, LocalTime.class);
    /**
     * The meta-property for the {@code valuationZone} property.
     */
    private final MetaProperty<ZoneId> valuationZone = DirectMetaProperty.ofImmutable(
        this, "valuationZone", BlackVolatilityExpiryTenorSwaptionProvider.class, ZoneId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "surface",
        "convention",
        "dayCount",
        "valuationDate",
        "valuationTime",
        "valuationZone");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1853231955:  // surface
          return surface;
        case 2039569265:  // convention
          return convention;
        case 1905311443:  // dayCount
          return dayCount;
        case 113107279:  // valuationDate
          return valuationDate;
        case 113591406:  // valuationTime
          return valuationTime;
        case 113775949:  // valuationZone
          return valuationZone;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BlackVolatilityExpiryTenorSwaptionProvider> builder() {
      return new BlackVolatilityExpiryTenorSwaptionProvider.Builder();
    }

    @Override
    public Class<? extends BlackVolatilityExpiryTenorSwaptionProvider> beanType() {
      return BlackVolatilityExpiryTenorSwaptionProvider.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code surface} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<InterpolatedDoublesSurface> surface() {
      return surface;
    }

    /**
     * The meta-property for the {@code convention} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FixedIborSwapConvention> convention() {
      return convention;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return dayCount;
    }

    /**
     * The meta-property for the {@code valuationDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> valuationDate() {
      return valuationDate;
    }

    /**
     * The meta-property for the {@code valuationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalTime> valuationTime() {
      return valuationTime;
    }

    /**
     * The meta-property for the {@code valuationZone} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZoneId> valuationZone() {
      return valuationZone;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1853231955:  // surface
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getSurface();
        case 2039569265:  // convention
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getConvention();
        case 1905311443:  // dayCount
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getDayCount();
        case 113107279:  // valuationDate
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getValuationDate();
        case 113591406:  // valuationTime
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getValuationTime();
        case 113775949:  // valuationZone
          return ((BlackVolatilityExpiryTenorSwaptionProvider) bean).getValuationZone();
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
   * The bean-builder for {@code BlackVolatilityExpiryTenorSwaptionProvider}.
   */
  private static class Builder extends DirectFieldsBeanBuilder<BlackVolatilityExpiryTenorSwaptionProvider> {

    private InterpolatedDoublesSurface surface;
    private FixedIborSwapConvention convention;
    private DayCount dayCount;
    private LocalDate valuationDate;
    private LocalTime valuationTime;
    private ZoneId valuationZone;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1853231955:  // surface
          return surface;
        case 2039569265:  // convention
          return convention;
        case 1905311443:  // dayCount
          return dayCount;
        case 113107279:  // valuationDate
          return valuationDate;
        case 113591406:  // valuationTime
          return valuationTime;
        case 113775949:  // valuationZone
          return valuationZone;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case -1853231955:  // surface
          this.surface = (InterpolatedDoublesSurface) newValue;
          break;
        case 2039569265:  // convention
          this.convention = (FixedIborSwapConvention) newValue;
          break;
        case 1905311443:  // dayCount
          this.dayCount = (DayCount) newValue;
          break;
        case 113107279:  // valuationDate
          this.valuationDate = (LocalDate) newValue;
          break;
        case 113591406:  // valuationTime
          this.valuationTime = (LocalTime) newValue;
          break;
        case 113775949:  // valuationZone
          this.valuationZone = (ZoneId) newValue;
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
    public BlackVolatilityExpiryTenorSwaptionProvider build() {
      return new BlackVolatilityExpiryTenorSwaptionProvider(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(224);
      buf.append("BlackVolatilityExpiryTenorSwaptionProvider.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("surface").append('=').append(JodaBeanUtils.toString(surface)).append(',').append(' ');
      buf.append("convention").append('=').append(JodaBeanUtils.toString(convention)).append(',').append(' ');
      buf.append("dayCount").append('=').append(JodaBeanUtils.toString(dayCount)).append(',').append(' ');
      buf.append("valuationDate").append('=').append(JodaBeanUtils.toString(valuationDate)).append(',').append(' ');
      buf.append("valuationTime").append('=').append(JodaBeanUtils.toString(valuationTime)).append(',').append(' ');
      buf.append("valuationZone").append('=').append(JodaBeanUtils.toString(valuationZone)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
