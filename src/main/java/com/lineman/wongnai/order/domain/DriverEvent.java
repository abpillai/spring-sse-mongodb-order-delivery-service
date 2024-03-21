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
@Document(collection = "driver_event")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DriverEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public DriverEvent id(String id) {
        this.setId(id);
        return this;
    }

	//@Field("orderid")

	@Field("driverid")
    private String driverId;

    @Field("type")
    private OrderType type;

    @Field("progress")
    private float progress;
    
    public DriverEvent progress(float progress) {
        this.setProgress(progress);
        return this;
    }
    

    public float getProgress() {
		return progress;
	}

	public void setProgress(float progress) {
		this.progress = progress;
	}

	@Field("status")
    private String status;
    
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

    public String getDriverId() {
        return this.driverId;
    }

    public DriverEvent driverId(String driverId) {
        this.setDriverId(driverId);
        return this;
    }
    
    public String getLatitude() {
        return this.latitude;
    }

    public DriverEvent latitude(String latitude) {
        this.setLatitude(latitude);
        return this;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public DriverEvent longitude(String longitude) {
        this.setLongitude(longitude);
        return this;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    
    

    public String getStatus() {
        return this.status;
    }

    public DriverEvent status(String status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(String status) {
        this.status = status;
    }

   

    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DriverEvent)) {
            return false;
        }
        return id != null && id.equals(((DriverEvent) o).id);
    }

    @Override
    public int hashCode() {
        
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Order{" +
            "id=" + getId() +
           
            ", status='" + getStatus() + "'" +
            ", latitude='" + getLatitude() + "'" +
            ", longitude='" + getLongitude() + "'" +
            "}";
    }
}
