package com.lineman.wongnai.order.web.rest;

import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.DriverEvent;
import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.domain.enumeration.DriverStatus;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.repository.ReactiveDriverEventRepository;
import com.lineman.wongnai.order.repository.ReactiveDriverRepository;
import com.lineman.wongnai.order.service.DriverService;
import com.lineman.wongnai.order.service.dto.DriverDTO;
import com.lineman.wongnai.order.service.dto.OrderDTO;
import com.lineman.wongnai.order.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.lineman.wongnai.order.domain.Driver}.
 */
@RestController
@RequestMapping("/api")
public class DriverResource {

    private final Logger log = LoggerFactory.getLogger(DriverResource.class);

    private static final String ENTITY_NAME = "driver";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DriverService driverService;

    private final ReactiveDriverRepository driverRepository;
    
    private final ReactiveDriverEventRepository reactiveDriverEventRepository;

    public DriverResource(DriverService driverService, ReactiveDriverRepository driverRepository, ReactiveDriverEventRepository reactiveDriverEventRepository) {
        this.driverService = driverService;
        this.driverRepository = driverRepository;
        this.reactiveDriverEventRepository = reactiveDriverEventRepository;
    }

    /**
     * {@code POST  /drivers} : Create a new driver.
     *
     * @param driverDTO the driverDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new driverDTO, or with status {@code 400 (Bad Request)} if the driver has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/drivers")
    public Mono<ResponseEntity<DriverDTO>> createDriver(@RequestBody DriverDTO driverDTO) throws URISyntaxException {
        log.debug("REST request to save Driver : {}", driverDTO);
        if (driverDTO.getId() != null) {
            throw new BadRequestAlertException("A new driver cannot already have an ID", ENTITY_NAME, "idexists");
        }
        
        driverDTO.setStatus("AVAILABLE");
        
        return driverService
            .save(driverDTO)
            .map(result -> {
                try {
                    return ResponseEntity
                        .created(new URI("/api/drivers/" + result.getId()))
                        .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * {@code PUT  /drivers/:id} : Updates an existing driver.
     *
     * @param id the id of the driverDTO to save.
     * @param driverDTO the driverDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated driverDTO,
     * or with status {@code 400 (Bad Request)} if the driverDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the driverDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/drivers/{id}")
    public Mono<ResponseEntity<DriverDTO>> updateDriver(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody DriverDTO driverDTO
    ) throws URISyntaxException {
        log.debug("REST request to update Driver : {}, {}", id, driverDTO);
        if (driverDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, driverDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        
        return driverService
                .update(driverDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(result ->
                    ResponseEntity
                        .ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                        .body(result)
                );

        /*return driverRepository
            .existsById(id)
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                }

                return driverService
                    .update(driverDTO)
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                    .map(result ->
                        ResponseEntity
                            .ok()
                            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
                            .body(result)
                    );
            });*/
    }
    
    
    
   

    /**
     * {@code PATCH  /drivers/:id} : Partial updates given fields of an existing driver, field will ignore if it is null
     *
     * @param id the id of the driverDTO to save.
     * @param driverDTO the driverDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated driverDTO,
     * or with status {@code 400 (Bad Request)} if the driverDTO is not valid,
     * or with status {@code 404 (Not Found)} if the driverDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the driverDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/drivers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public Mono<ResponseEntity<DriverDTO>> partialUpdateDriver(
        @PathVariable(value = "id", required = false) final String id,
        @RequestBody DriverDTO driverDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update Driver partially : {}, {}", id, driverDTO);
        if (driverDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, driverDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

      
        
        return driverRepository
                .existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }
                    
                    Mono<DriverDTO> result = driverService.partialUpdate(driverDTO);
                    
                  
              
                    return result
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(res ->
                            ResponseEntity
                                .ok()
                                .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId()))
                                .body(res)
                        );
                });
    }

    /**
     * {@code GET  /drivers} : get all the drivers.
     *
     * @param pageable the pagination information.
     * @param request a {@link ServerHttpRequest} request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of drivers in body.
     */
    @GetMapping("/drivers")
    public Mono<ResponseEntity<List<DriverDTO>>> getAllDrivers(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable,
        ServerHttpRequest request
    ) {
        log.debug("REST request to get a page of Drivers");
        return driverService
            .countAll()
            .zipWith(driverService.findAll(pageable).collectList())
            .map(countWithEntities ->
                ResponseEntity
                    .ok()
                    .headers(
                        PaginationUtil.generatePaginationHttpHeaders(
                            UriComponentsBuilder.fromHttpRequest(request),
                            new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())
                        )
                    )
                    .body(countWithEntities.getT2())
            );
       
        
    }
    
  
    
   
    
    @GetMapping(path = "/drivers/{id}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DriverEvent> driverLocation(@PathVariable(value = "id", required = true) final String id
    		) {
    	
    	
    	 return reactiveDriverEventRepository.findWithTailableCursorByDriverId(id);
                 
    	
    }

    /**
     * {@code GET  /drivers/:id} : get the "id" driver.
     *
     * @param id the id of the driverDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the driverDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/drivers/{id}")
    public Mono<ResponseEntity<DriverDTO>> getDriver(@PathVariable String id) {
        log.debug("REST request to get Driver : {}", id);
        Mono<DriverDTO> driverDTO = driverService.findOne(id);
        return ResponseUtil.wrapOrNotFound(driverDTO);
    }

    /**
     * {@code DELETE  /drivers/:id} : delete the "id" driver.
     *
     * @param id the id of the driverDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/drivers/{id}")
    public Mono<ResponseEntity<Void>> deleteDriver(@PathVariable String id) {
        log.debug("REST request to delete Driver : {}", id);
        return driverService
            .delete(id)
            .then(
                Mono.just(
                    ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()
                )
            );
    }
}
