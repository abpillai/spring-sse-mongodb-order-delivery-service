package com.lineman.wongnai.order.service.mapper;

import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.service.dto.DriverDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Driver} and its DTO {@link DriverDTO}.
 */
@Mapper(componentModel = "spring")
public interface DriverMapper extends EntityMapper<DriverDTO, Driver> {}
