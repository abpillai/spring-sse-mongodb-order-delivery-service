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
@Document(collection = "order_event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Field("progress")
    private float progress;
    
    public OrderEvent progress(float progress) {
        this.setProgress(progress);
        return this;
    }
    

    public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

    @Id
    private String id;
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public OrderEvent id(String id) {
        this.setId(id);
        return this;
    }

	//@Field("orderid")

	@Field("orderid")
    private String orderId;

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

    public String getOrderId() {
        return this.orderId;
    }

    public OrderEvent orderId(String orderId) {
        this.setOrderId(orderId);
        return this;
    }
    
    public String getLatitude() {
        return this.latitude;
    }

    public OrderEvent latitude(String latitude) {
        this.setLatitude(latitude);
        return this;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public OrderEvent longitude(String longitude) {
        this.setLongitude(longitude);
        return this;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderType getType() {
        return this.type;
    }

    public OrderEvent type(OrderType type) {
        this.setType(type);
        return this;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Instant getTime() {
        return this.time;
    }

    public OrderEvent time(Instant time) {
        this.setTime(time);
        return this;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public OrderStatus getStatus() {
        return this.status;
    }

    public OrderEvent status(OrderStatus status) {
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

    public OrderEvent customer(Customer customer) {
        this.setCustomer(customer);
        return this;
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public OrderEvent driver(Driver driver) {
        this.setDriver(driver);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrderEvent)) {
            return false;
        }
        return id != null && id.equals(((OrderEvent) o).id);
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
