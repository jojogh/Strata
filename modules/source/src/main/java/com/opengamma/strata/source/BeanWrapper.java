/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.source;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableSet;
import com.opengamma.strata.collect.id.IdentifiableBean;
import com.opengamma.strata.collect.id.StandardId;

/**
 * Wrapper class for an {@link IdentifiableBean} that allows additional identifiers
 * to be associated with the bean.
 */
@BeanDefinition
public final class BeanWrapper implements ImmutableBean, Serializable {

  /**
   * The bean to associate the additional identifiers with.
   */
  @PropertyDefinition(validate = "notNull")
  private final IdentifiableBean bean;

  /**
   * The set of additional identifiers to be associated with the bean.
   * May be empty.
   */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableSet<StandardId> additionalIdentifiers;

  /**
   * Static factory providing easier alternative to the builder method.
   *
   * @param bean  the bean being wrapped
   * @param additionalIdentifiers  the additional identifiers for the bean, may be empty
   * @return a new bean wrapper
   */
  public static BeanWrapper of(IdentifiableBean bean, Set<StandardId> additionalIdentifiers) {
    return new BeanWrapper(bean, additionalIdentifiers);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BeanWrapper}.
   * @return the meta-bean, not null
   */
  public static BeanWrapper.Meta meta() {
    return BeanWrapper.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BeanWrapper.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static BeanWrapper.Builder builder() {
    return new BeanWrapper.Builder();
  }

  private BeanWrapper(
      IdentifiableBean bean,
      Set<StandardId> additionalIdentifiers) {
    JodaBeanUtils.notNull(bean, "bean");
    JodaBeanUtils.notNull(additionalIdentifiers, "additionalIdentifiers");
    this.bean = bean;
    this.additionalIdentifiers = ImmutableSet.copyOf(additionalIdentifiers);
  }

  @Override
  public BeanWrapper.Meta metaBean() {
    return BeanWrapper.Meta.INSTANCE;
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
   * Gets the bean to associate the additional identifiers with.
   * @return the value of the property, not null
   */
  public IdentifiableBean getBean() {
    return bean;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of additional identifiers to be associated with the bean.
   * May be empty.
   * @return the value of the property, not null
   */
  public ImmutableSet<StandardId> getAdditionalIdentifiers() {
    return additionalIdentifiers;
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
      BeanWrapper other = (BeanWrapper) obj;
      return JodaBeanUtils.equal(getBean(), other.getBean()) &&
          JodaBeanUtils.equal(getAdditionalIdentifiers(), other.getAdditionalIdentifiers());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getBean());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAdditionalIdentifiers());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("BeanWrapper{");
    buf.append("bean").append('=').append(getBean()).append(',').append(' ');
    buf.append("additionalIdentifiers").append('=').append(JodaBeanUtils.toString(getAdditionalIdentifiers()));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BeanWrapper}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code bean} property.
     */
    private final MetaProperty<IdentifiableBean> bean = DirectMetaProperty.ofImmutable(
        this, "bean", BeanWrapper.class, IdentifiableBean.class);
    /**
     * The meta-property for the {@code additionalIdentifiers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableSet<StandardId>> additionalIdentifiers = DirectMetaProperty.ofImmutable(
        this, "additionalIdentifiers", BeanWrapper.class, (Class) ImmutableSet.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "bean",
        "additionalIdentifiers");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3019696:  // bean
          return bean;
        case -1604662717:  // additionalIdentifiers
          return additionalIdentifiers;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanWrapper.Builder builder() {
      return new BeanWrapper.Builder();
    }

    @Override
    public Class<? extends BeanWrapper> beanType() {
      return BeanWrapper.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code bean} property.
     * @return the meta-property, not null
     */
    public MetaProperty<IdentifiableBean> bean() {
      return bean;
    }

    /**
     * The meta-property for the {@code additionalIdentifiers} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableSet<StandardId>> additionalIdentifiers() {
      return additionalIdentifiers;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3019696:  // bean
          return ((BeanWrapper) bean).getBean();
        case -1604662717:  // additionalIdentifiers
          return ((BeanWrapper) bean).getAdditionalIdentifiers();
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
   * The bean-builder for {@code BeanWrapper}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<BeanWrapper> {

    private IdentifiableBean bean;
    private Set<StandardId> additionalIdentifiers = ImmutableSet.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(BeanWrapper beanToCopy) {
      this.bean = beanToCopy.getBean();
      this.additionalIdentifiers = beanToCopy.getAdditionalIdentifiers();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3019696:  // bean
          return bean;
        case -1604662717:  // additionalIdentifiers
          return additionalIdentifiers;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3019696:  // bean
          this.bean = (IdentifiableBean) newValue;
          break;
        case -1604662717:  // additionalIdentifiers
          this.additionalIdentifiers = (Set<StandardId>) newValue;
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
    public BeanWrapper build() {
      return new BeanWrapper(
          bean,
          additionalIdentifiers);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the bean to associate the additional identifiers with.
     * @param bean  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder bean(IdentifiableBean bean) {
      JodaBeanUtils.notNull(bean, "bean");
      this.bean = bean;
      return this;
    }

    /**
     * Sets the set of additional identifiers to be associated with the bean.
     * May be empty.
     * @param additionalIdentifiers  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder additionalIdentifiers(Set<StandardId> additionalIdentifiers) {
      JodaBeanUtils.notNull(additionalIdentifiers, "additionalIdentifiers");
      this.additionalIdentifiers = additionalIdentifiers;
      return this;
    }

    /**
     * Sets the {@code additionalIdentifiers} property in the builder
     * from an array of objects.
     * @param additionalIdentifiers  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder additionalIdentifiers(StandardId... additionalIdentifiers) {
      return additionalIdentifiers(ImmutableSet.copyOf(additionalIdentifiers));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("BeanWrapper.Builder{");
      buf.append("bean").append('=').append(JodaBeanUtils.toString(bean)).append(',').append(' ');
      buf.append("additionalIdentifiers").append('=').append(JodaBeanUtils.toString(additionalIdentifiers));
      buf.append('}');
      return buf.toString();
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
