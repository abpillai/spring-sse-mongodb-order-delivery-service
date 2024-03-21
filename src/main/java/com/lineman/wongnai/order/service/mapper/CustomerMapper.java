package com.lineman.wongnai.order.service.mapper;

import com.lineman.wongnai.order.domain.Customer;
import com.lineman.wongnai.order.service.dto.CustomerDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Customer} and its DTO {@link CustomerDTO}.
 */
@Mapper(componentModel = "spring")
public interface CustomerMapper extends EntityMapper<CustomerDTO, Customer> {}
