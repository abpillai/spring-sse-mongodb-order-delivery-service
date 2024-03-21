package com.lineman.wongnai.order.service.dto;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * A DTO for the {@link com.lineman.wongnai.order.domain.Driver} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")


public class DriverDTO implements Serializable {
	
	
	
	

  private static final long serialVersionUID = 1L;

private String status;
  
  

	public String getStatus() {
	return status;
}

public void setStatus(String status) {
	this.status = status;
}

	private String id;

    private String name;

    private String phone;

    private String latitude;

    private String longitude;
    
    private String orderId;
    
    

    public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DriverDTO)) {
            return false;
        }

        DriverDTO driverDTO = (DriverDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, driverDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DriverDTO{" +
            "id='" + getId() + "'" +
            ", name='" + getName() + "'" +
            ", phone='" + getPhone() + "'" +
            ", latitude='" + getLatitude() + "'" +
            ", longitude='" + getLongitude() + "'" +
            ", status='" + getStatus() + "'" +
            "}";
    }
}
