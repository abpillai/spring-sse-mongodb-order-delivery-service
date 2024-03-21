package com.lineman.wongnai.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.domain.enumeration.OrderType;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A Order.
 */
@Document(collection = "order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("type")
    private OrderType type;

    @Field("time")
    private Instant time;

    @Field("status")
    private OrderStatus status;
    
    @Field("latitude")
    private String latitude;

    @Field("longitude")
    private String longitude;

    @Field("customer")
    @JsonIgnoreProperties(value = { "orders" }, allowSetters = true)
    private Customer customer;

    @Field("driver")
    @JsonIgnoreProperties(value = { "orders" }, allowSetters = true)
    private Driver driver;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public Order id(String id) {
        this.setId(id);
        return this;
    }
    
    public String getLatitude() {
        return this.latitude;
    }

    public Order latitude(String latitude) {
        this.setLatitude(latitude);
        return this;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public Order longitude(String longitude) {
        this.setLongitude(longitude);
        return this;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OrderType getType() {
        return this.type;
    }

    public Order type(OrderType type) {
        this.setType(type);
        return this;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Instant getTime() {
        return this.time;
    }

    public Order time(Instant time) {
        this.setTime(time);
        return this;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public Order status(OrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Customer getCustomer() {
        return this.customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Order customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Order driver(Driver driver) {
        this.setDriver(driver);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Order)) {
            return false;
        }
        return id != null && id.equals(((Order) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Order{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", time='" + getTime() + "'" +
            ", status='" + getStatus() + "'" +
            ", latitude='" + getLatitude() + "'" +
            ", longitude='" + getLongitude() + "'" +
            "}";
    }
}
