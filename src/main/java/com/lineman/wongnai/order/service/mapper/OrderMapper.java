package com.lineman.wongnai.order.service.mapper;

import com.lineman.wongnai.order.domain.Customer;
import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.Order;
import com.lineman.wongnai.order.service.dto.CustomerDTO;
import com.lineman.wongnai.order.service.dto.DriverDTO;
import com.lineman.wongnai.order.service.dto.OrderDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Order} and its DTO {@link OrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper extends EntityMapper<OrderDTO, Order> {
    @Mapping(target = "customer", source = "customer", qualifiedByName = "customerId")
    @Mapping(target = "driver", source = "driver", qualifiedByName = "driverId")
    OrderDTO toDto(Order s);

    @Named("customerId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CustomerDTO toDtoCustomerId(Customer customer);

    @Named("driverId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    DriverDTO toDtoDriverId(Driver driver);
}
