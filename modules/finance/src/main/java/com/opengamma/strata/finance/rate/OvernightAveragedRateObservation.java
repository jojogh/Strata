/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance.rate;

import java.io.Serializable;
import java.time.LocalDate;
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

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.basics.index.Index;
import com.opengamma.strata.basics.index.OvernightIndex;
import com.opengamma.strata.collect.ArgChecker;

/**
 * Defines the observation of a rate from a single Overnight index that is averaged daily.
 * <p>
 * An interest rate determined directly from an Overnight index by averaging the value
 * of each day's rate over the period.
 * For example, a rate determined averaging values from 'USD-FED-FUND'.
 */
@BeanDefinition
public final class OvernightAveragedRateObservation
    implements RateObservation, ImmutableBean, Serializable {

  /**
   * The Overnight index.
   * <p>
   * The rate to be paid is based on this index.
   * It will be a well known market index such as 'USD-FED-FUND'.
   */
  @PropertyDefinition(validate = "notNull")
  private final OvernightIndex index;
  /**
   * The first date in the fixing period.
   * <p>
   * The overnight rate is observed from this date onwards.
   * <p>
   * In general, the fixing dates and accrual dates are the same for an overnight index.
   * However, in the case of a Tomorrow/Next index, the fixing period is one business day
   * before the accrual period.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate startDate;
  /**
   * The last date in the fixing period.
   * <p>
   * The overnight rate is observed until this date.
   * The last fixing date will be one business day before this date.
   * <p>
   * In general, the fixing dates and accrual dates are the same for an overnight index.
   * However, in the case of a Tomorrow/Next index, the fixing period is one business day
   * before the accrual period.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate endDate;
  /**
   * The number of business days before the end of the period that the rate is cut off.
   * <p>
   * When a rate cut-off applies, the final daily rate is determined this number of days
   * before the end of the period, with any subsequent days having the same rate.
   * <p>
   * The amount must be zero or positive.
   * A value of zero or one will have no effect on the standard calculation.
   * The fixing holiday calendar of the index is used to determine business days.
   * <p>
   * For example, a value of {@code 3} means that the rate observed on
   * {@code (periodEndDate - 3 business days)} is also to be used on
   * {@code (periodEndDate - 2 business days)} and {@code (periodEndDate - 1 business day)}.
   * <p>
   * If there are multiple accrual periods in the payment period, then this
   * should typically only be non-zero in the last accrual period.
   */
  @PropertyDefinition(validate = "ArgChecker.notNegative")
  private final int rateCutOffDays;

  //-------------------------------------------------------------------------
  /**
   * Creates an {@code OvernightAveragedRateObservation} from an index and period dates
   * <p>
   * No rate cut-off applies.
   * 
   * @param index  the index
   * @param startDate  the first date of the period
   * @param endDate  the last date of the period
   * @return the Overnight compounded rate
   */
  public static OvernightAveragedRateObservation of(OvernightIndex index, LocalDate startDate, LocalDate endDate) {
    return of(index, startDate, endDate, 0);
  }

  /**
   * Creates an {@code OvernightAveragedRateObservation} from an index, period dates and rate cut-off.
   * <p>
   * Rate cut-off applies if the cut-off is 2 or greater.
   * 
   * @param index  the index
   * @param startDate  the first date of the period
   * @param endDate  the last date of the period
   * @param rateCutOffDays  the rate cut-off days offset, not negative
   * @return the Overnight compounded rate
   */
  public static OvernightAveragedRateObservation of(
      OvernightIndex index,
      LocalDate startDate,
      LocalDate endDate,
      int rateCutOffDays) {
    return OvernightAveragedRateObservation.builder()
        .index(index)
        .startDate(startDate)
        .endDate(endDate)
        .rateCutOffDays(rateCutOffDays)
        .build();
  }

  //-------------------------------------------------------------------------
  @ImmutableValidator
  private void validate() {
    ArgChecker.inOrderNotEqual(startDate, endDate, "startDate", "endDate");
  }

  //-------------------------------------------------------------------------
  @Override
  public void collectIndices(ImmutableSet.Builder<Index> builder) {
    builder.add(index);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code OvernightAveragedRateObservation}.
   * @return the meta-bean, not null
   */
  public static OvernightAveragedRateObservation.Meta meta() {
    return OvernightAveragedRateObservation.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(OvernightAveragedRateObservation.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static OvernightAveragedRateObservation.Builder builder() {
    return new OvernightAveragedRateObservation.Builder();
  }

  private OvernightAveragedRateObservation(
      OvernightIndex index,
      LocalDate startDate,
      LocalDate endDate,
      int rateCutOffDays) {
    JodaBeanUtils.notNull(index, "index");
    JodaBeanUtils.notNull(startDate, "startDate");
    JodaBeanUtils.notNull(endDate, "endDate");
    ArgChecker.notNegative(rateCutOffDays, "rateCutOffDays");
    this.index = index;
    this.startDate = startDate;
    this.endDate = endDate;
    this.rateCutOffDays = rateCutOffDays;
    validate();
  }

  @Override
  public OvernightAveragedRateObservation.Meta metaBean() {
    return OvernightAveragedRateObservation.Meta.INSTANCE;
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
   * Gets the Overnight index.
   * <p>
   * The rate to be paid is based on this index.
   * It will be a well known market index such as 'USD-FED-FUND'.
   * @return the value of the property, not null
   */
  public OvernightIndex getIndex() {
    return index;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first date in the fixing period.
   * <p>
   * The overnight rate is observed from this date onwards.
   * <p>
   * In general, the fixing dates and accrual dates are the same for an overnight index.
   * However, in the case of a Tomorrow/Next index, the fixing period is one business day
   * before the accrual period.
   * @return the value of the property, not null
   */
  public LocalDate getStartDate() {
    return startDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last date in the fixing period.
   * <p>
   * The overnight rate is observed until this date.
   * The last fixing date will be one business day before this date.
   * <p>
   * In general, the fixing dates and accrual dates are the same for an overnight index.
   * However, in the case of a Tomorrow/Next index, the fixing period is one business day
   * before the accrual period.
   * @return the value of the property, not null
   */
  public LocalDate getEndDate() {
    return endDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of business days before the end of the period that the rate is cut off.
   * <p>
   * When a rate cut-off applies, the final daily rate is determined this number of days
   * before the end of the period, with any subsequent days having the same rate.
   * <p>
   * The amount must be zero or positive.
   * A value of zero or one will have no effect on the standard calculation.
   * The fixing holiday calendar of the index is used to determine business days.
   * <p>
   * For example, a value of {@code 3} means that the rate observed on
   * {@code (periodEndDate - 3 business days)} is also to be used on
   * {@code (periodEndDate - 2 business days)} and {@code (periodEndDate - 1 business day)}.
   * <p>
   * If there are multiple accrual periods in the payment period, then this
   * should typically only be non-zero in the last accrual period.
   * @return the value of the property
   */
  public int getRateCutOffDays() {
    return rateCutOffDays;
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
      OvernightAveragedRateObservation other = (OvernightAveragedRateObservation) obj;
      return JodaBeanUtils.equal(getIndex(), other.getIndex()) &&
          JodaBeanUtils.equal(getStartDate(), other.getStartDate()) &&
          JodaBeanUtils.equal(getEndDate(), other.getEndDate()) &&
          (getRateCutOffDays() == other.getRateCutOffDays());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getIndex());
    hash = hash * 31 + JodaBeanUtils.hashCode(getStartDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getEndDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRateCutOffDays());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("OvernightAveragedRateObservation{");
    buf.append("index").append('=').append(getIndex()).append(',').append(' ');
    buf.append("startDate").append('=').append(getStartDate()).append(',').append(' ');
    buf.append("endDate").append('=').append(getEndDate()).append(',').append(' ');
    buf.append("rateCutOffDays").append('=').append(JodaBeanUtils.toString(getRateCutOffDays()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code OvernightAveragedRateObservation}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code index} property.
     */
    private final MetaProperty<OvernightIndex> index = DirectMetaProperty.ofImmutable(
        this, "index", OvernightAveragedRateObservation.class, OvernightIndex.class);
    /**
     * The meta-property for the {@code startDate} property.
     */
    private final MetaProperty<LocalDate> startDate = DirectMetaProperty.ofImmutable(
        this, "startDate", OvernightAveragedRateObservation.class, LocalDate.class);
    /**
     * The meta-property for the {@code endDate} property.
     */
    private final MetaProperty<LocalDate> endDate = DirectMetaProperty.ofImmutable(
        this, "endDate", OvernightAveragedRateObservation.class, LocalDate.class);
    /**
     * The meta-property for the {@code rateCutOffDays} property.
     */
    private final MetaProperty<Integer> rateCutOffDays = DirectMetaProperty.ofImmutable(
        this, "rateCutOffDays", OvernightAveragedRateObservation.class, Integer.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "index",
        "startDate",
        "endDate",
        "rateCutOffDays");

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
        case -2129778896:  // startDate
          return startDate;
        case -1607727319:  // endDate
          return endDate;
        case -92095804:  // rateCutOffDays
          return rateCutOffDays;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public OvernightAveragedRateObservation.Builder builder() {
      return new OvernightAveragedRateObservation.Builder();
    }

    @Override
    public Class<? extends OvernightAveragedRateObservation> beanType() {
      return OvernightAveragedRateObservation.class;
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
    public MetaProperty<OvernightIndex> index() {
      return index;
    }

    /**
     * The meta-property for the {@code startDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> startDate() {
      return startDate;
    }

    /**
     * The meta-property for the {@code endDate} property.
     * @return the meta-property, not null
     */
    public MetaProperty<LocalDate> endDate() {
      return endDate;
    }

    /**
     * The meta-property for the {@code rateCutOffDays} property.
     * @return the meta-property, not null
     */
    public MetaProperty<Integer> rateCutOffDays() {
      return rateCutOffDays;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return ((OvernightAveragedRateObservation) bean).getIndex();
        case -2129778896:  // startDate
          return ((OvernightAveragedRateObservation) bean).getStartDate();
        case -1607727319:  // endDate
          return ((OvernightAveragedRateObservation) bean).getEndDate();
        case -92095804:  // rateCutOffDays
          return ((OvernightAveragedRateObservation) bean).getRateCutOffDays();
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
   * The bean-builder for {@code OvernightAveragedRateObservation}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<OvernightAveragedRateObservation> {

    private OvernightIndex index;
    private LocalDate startDate;
    private LocalDate endDate;
    private int rateCutOffDays;

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(OvernightAveragedRateObservation beanToCopy) {
      this.index = beanToCopy.getIndex();
      this.startDate = beanToCopy.getStartDate();
      this.endDate = beanToCopy.getEndDate();
      this.rateCutOffDays = beanToCopy.getRateCutOffDays();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          return index;
        case -2129778896:  // startDate
          return startDate;
        case -1607727319:  // endDate
          return endDate;
        case -92095804:  // rateCutOffDays
          return rateCutOffDays;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 100346066:  // index
          this.index = (OvernightIndex) newValue;
          break;
        case -2129778896:  // startDate
          this.startDate = (LocalDate) newValue;
          break;
        case -1607727319:  // endDate
          this.endDate = (LocalDate) newValue;
          break;
        case -92095804:  // rateCutOffDays
          this.rateCutOffDays = (Integer) newValue;
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
    public OvernightAveragedRateObservation build() {
      return new OvernightAveragedRateObservation(
          index,
          startDate,
          endDate,
          rateCutOffDays);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the Overnight index.
     * <p>
     * The rate to be paid is based on this index.
     * It will be a well known market index such as 'USD-FED-FUND'.
     * @param index  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder index(OvernightIndex index) {
      JodaBeanUtils.notNull(index, "index");
      this.index = index;
      return this;
    }

    /**
     * Sets the first date in the fixing period.
     * <p>
     * The overnight rate is observed from this date onwards.
     * <p>
     * In general, the fixing dates and accrual dates are the same for an overnight index.
     * However, in the case of a Tomorrow/Next index, the fixing period is one business day
     * before the accrual period.
     * @param startDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder startDate(LocalDate startDate) {
      JodaBeanUtils.notNull(startDate, "startDate");
      this.startDate = startDate;
      return this;
    }

    /**
     * Sets the last date in the fixing period.
     * <p>
     * The overnight rate is observed until this date.
     * The last fixing date will be one business day before this date.
     * <p>
     * In general, the fixing dates and accrual dates are the same for an overnight index.
     * However, in the case of a Tomorrow/Next index, the fixing period is one business day
     * before the accrual period.
     * @param endDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder endDate(LocalDate endDate) {
      JodaBeanUtils.notNull(endDate, "endDate");
      this.endDate = endDate;
      return this;
    }

    /**
     * Sets the number of business days before the end of the period that the rate is cut off.
     * <p>
     * When a rate cut-off applies, the final daily rate is determined this number of days
     * before the end of the period, with any subsequent days having the same rate.
     * <p>
     * The amount must be zero or positive.
     * A value of zero or one will have no effect on the standard calculation.
     * The fixing holiday calendar of the index is used to determine business days.
     * <p>
     * For example, a value of {@code 3} means that the rate observed on
     * {@code (periodEndDate - 3 business days)} is also to be used on
     * {@code (periodEndDate - 2 business days)} and {@code (periodEndDate - 1 business day)}.
     * <p>
     * If there are multiple accrual periods in the payment period, then this
     * should typically only be non-zero in the last accrual period.
     * @param rateCutOffDays  the new value
     * @return this, for chaining, not null
     */
    public Builder rateCutOffDays(int rateCutOffDays) {
      ArgChecker.notNegative(rateCutOffDays, "rateCutOffDays");
      this.rateCutOffDays = rateCutOffDays;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("OvernightAveragedRateObservation.Builder{");
      buf.append("index").append('=').append(JodaBeanUtils.toString(index)).append(',').append(' ');
      buf.append("startDate").append('=').append(JodaBeanUtils.toString(startDate)).append(',').append(' ');
      buf.append("endDate").append('=').append(JodaBeanUtils.toString(endDate)).append(',').append(' ');
      buf.append("rateCutOffDays").append('=').append(JodaBeanUtils.toString(rateCutOffDays));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
