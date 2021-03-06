/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.strata.finance;

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

import com.opengamma.strata.collect.id.StandardId;

/**
 * Mock product that is not resolvable.
 */
@BeanDefinition
public class MockSimpleProduct
    implements ImmutableBean, Product, Serializable {

  /**
   * Identifier of mock 1.
   */
  public static final StandardId STANDARD_ID_1 = StandardId.of("OG-Security", "1");
  /**
   * Identifier of mock 2.
   */
  public static final StandardId STANDARD_ID_2 = StandardId.of("OG-Security", "2");
  /**
   * Simple mock 1.
   */
  public static final MockSimpleProduct MOCK1 = MockSimpleProduct.builder().build();
  /**
   * Simple mock 1 wrapped in a security.
   */
  public static final Security<MockSimpleProduct> MOCK1_SECURITY =
      UnitSecurity.builder(MOCK1).standardId(STANDARD_ID_1).build();
  /**
   * Simple mock 2.
   */
  public static final MockSimpleProduct MOCK2 = MockSimpleProduct.builder().name("Resolved").build();
  /**
   * Simple mock 2 wrapped in a security.
   */
  public static final Security<MockSimpleProduct> MOCK2_SECURITY =
      UnitSecurity.builder(MOCK2).standardId(STANDARD_ID_2).build();
  /**
   * Resolvable link to mock 2.
   */
  public static final SecurityLink<MockSimpleProduct> LINK_RESOLVABLE_MOCK2 =
      SecurityLink.resolvable(STANDARD_ID_2, MockSimpleProduct.class);
  /**
   * Resolved link to mock 2.
   */
  public static final SecurityLink<MockSimpleProduct> LINK_RESOLVED_MOCK2 = SecurityLink.resolved(MOCK2_SECURITY);

  @PropertyDefinition
  private final String name;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MockSimpleProduct}.
   * @return the meta-bean, not null
   */
  public static MockSimpleProduct.Meta meta() {
    return MockSimpleProduct.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MockSimpleProduct.Meta.INSTANCE);
  }

  /**
   * The serialization version id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static MockSimpleProduct.Builder builder() {
    return new MockSimpleProduct.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected MockSimpleProduct(MockSimpleProduct.Builder builder) {
    this.name = builder.name;
  }

  @Override
  public MockSimpleProduct.Meta metaBean() {
    return MockSimpleProduct.Meta.INSTANCE;
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
   * Gets the name.
   * @return the value of the property
   */
  public String getName() {
    return name;
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
      MockSimpleProduct other = (MockSimpleProduct) obj;
      return JodaBeanUtils.equal(getName(), other.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("MockSimpleProduct{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MockSimpleProduct}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> name = DirectMetaProperty.ofImmutable(
        this, "name", MockSimpleProduct.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "name");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return name;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public MockSimpleProduct.Builder builder() {
      return new MockSimpleProduct.Builder();
    }

    @Override
    public Class<? extends MockSimpleProduct> beanType() {
      return MockSimpleProduct.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return name;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return ((MockSimpleProduct) bean).getName();
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
   * The bean-builder for {@code MockSimpleProduct}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<MockSimpleProduct> {

    private String name;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(MockSimpleProduct beanToCopy) {
      this.name = beanToCopy.getName();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          return name;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 3373707:  // name
          this.name = (String) newValue;
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
    public MockSimpleProduct build() {
      return new MockSimpleProduct(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the name.
     * @param name  the new value
     * @return this, for chaining, not null
     */
    public Builder name(String name) {
      this.name = name;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("MockSimpleProduct.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
