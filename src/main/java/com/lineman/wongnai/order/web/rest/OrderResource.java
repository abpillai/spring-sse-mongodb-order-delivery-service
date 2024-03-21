package com.lineman.wongnai.order.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.repository.OrderRepository;
import com.lineman.wongnai.order.repository.ReactiveDriverRepository;
import com.lineman.wongnai.order.repository.ReactiveOrderEventRepository;
import com.lineman.wongnai.order.repository.ReactiveOrderRepository;
import com.lineman.wongnai.order.service.OrderService;
import com.lineman.wongnai.order.service.dto.OrderDTO;
import com.lineman.wongnai.order.web.rest.errors.BadRequestAlertException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link com.lineman.wongnai.order.domain.Order}.
 */
@RestController
@RequestMapping("/api")
public class OrderResource {

	private final Logger log = LoggerFactory.getLogger(OrderResource.class);

	private static final String ENTITY_NAME = "order";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final OrderService orderService;

	private final ReactiveOrderRepository orderRepository;

	private final ReactiveOrderEventRepository reactiveOrderEventRepository;

	public OrderResource(OrderService orderService, ReactiveOrderRepository orderRepository,
			ReactiveDriverRepository driverRepository, ReactiveOrderEventRepository reactiveOrderEventRepository,
			OrderRepository orderRepositoryy) {
		this.orderService = orderService;
		this.orderRepository = orderRepository;
		this.reactiveOrderEventRepository = reactiveOrderEventRepository;

	}

	/**
	 * {@code POST  /orders} : Create a new order.
	 *
	 * @param orderDTO the orderDTO to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with
	 *         body the new orderDTO, or with status {@code 400 (Bad Request)} if
	 *         the order has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("/orders")
	public Mono<ResponseEntity<OrderDTO>> createOrder(@RequestBody OrderDTO orderDTO) throws URISyntaxException {
		log.debug("REST request to save Order : {}", orderDTO);
		if (orderDTO.getId() != null) {
			throw new BadRequestAlertException("A new order cannot already have an ID", ENTITY_NAME, "idexists");
		}

		orderDTO.setStatus(OrderStatus.INIT);
		orderDTO.setTime(Instant.now());
		return orderService.save(orderDTO).map(result -> {
			try {

				orderDTO.setId(result.getId());

				orderService.placeOrder(orderDTO);

				return ResponseEntity
						.created(new URI("/api/orders/" + result.getId())).headers(HeaderUtil
								.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
						.body(result);
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		});

	}

	/**
	 * {@code PUT  /orders/:id} : Updates an existing order.
	 *
	 * @param id       the id of the orderDTO to save.
	 * @param orderDTO the orderDTO to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
	 *         the updated orderDTO, or with status {@code 400 (Bad Request)} if the
	 *         orderDTO is not valid, or with status
	 *         {@code 500 (Internal Server Error)} if the orderDTO couldn't be
	 *         updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/orders/{id}")
	public Mono<ResponseEntity<OrderDTO>> updateOrder(@PathVariable(value = "id", required = false) final String id,
			@RequestBody OrderDTO orderDTO) throws URISyntaxException {
		log.debug("REST request to update Order : {}, {}", id, orderDTO);
		if (orderDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, orderDTO.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		return orderService.update(orderDTO)
				.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
				.map(result -> ResponseEntity.ok()
						.headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId()))
						.body(result));

	}

	@GetMapping(path = "/orders/{id}/status", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<OrderEvent> driverLocation(@PathVariable(value = "id", required = true) final String id) {

		return reactiveOrderEventRepository.findWithTailableCursorByOrderId(id);

	}

	/**
	 * {@code PATCH  /orders/:id} : Partial updates given fields of an existing
	 * order, field will ignore if it is null
	 *
	 * @param id       the id of the orderDTO to save.
	 * @param orderDTO the orderDTO to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
	 *         the updated orderDTO, or with status {@code 400 (Bad Request)} if the
	 *         orderDTO is not valid, or with status {@code 404 (Not Found)} if the
	 *         orderDTO is not found, or with status
	 *         {@code 500 (Internal Server Error)} if the orderDTO couldn't be
	 *         updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PatchMapping(value = "/orders/{id}", consumes = { "application/json", "application/merge-patch+json" })
	public Mono<ResponseEntity<OrderDTO>> partialUpdateOrder(
			@PathVariable(value = "id", required = false) final String id, @RequestBody OrderDTO orderDTO)
			throws URISyntaxException {
		log.debug("REST request to partial update Order partially : {}, {}", id, orderDTO);
		if (orderDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		if (!Objects.equals(id, orderDTO.getId())) {
			throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
		}

		if (orderDTO.getStatus().equals(OrderStatus.CANCELLED) || orderDTO.getStatus().equals(OrderStatus.DELIVERED)) {
			orderService.placeOrder(orderDTO);
		} else {
			return orderRepository.existsById(id).flatMap(exists -> {
				if (!exists) {
					return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
				}
				orderDTO.setTime(Instant.now());
				Mono<OrderDTO> result = orderService.partialUpdate(orderDTO);

				return result.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
						.map(res -> ResponseEntity.ok().headers(
								HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId()))
								.body(res));
			});
		}

		return null;

	}

	/**
	 * {@code GET  /orders} : get all the orders.
	 *
	 * @param pageable the pagination information.
	 * @param request  a {@link ServerHttpRequest} request.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list
	 *         of orders in body.
	 */
	@GetMapping("/orders")
	public Mono<ResponseEntity<List<OrderDTO>>> getAllOrders(
			@org.springdoc.api.annotations.ParameterObject Pageable pageable, ServerHttpRequest request) {
		log.debug("REST request to get a page of Orders");
		return orderService.countAll().zipWith(orderService.findAll(pageable).collectList())
				.map(countWithEntities -> ResponseEntity.ok()
						.headers(PaginationUtil.generatePaginationHttpHeaders(
								UriComponentsBuilder.fromHttpRequest(request),
								new PageImpl<>(countWithEntities.getT2(), pageable, countWithEntities.getT1())))
						.body(countWithEntities.getT2()));
	}

	/**
	 * {@code GET  /orders/:id} : get the "id" order.
	 *
	 * @param id the id of the orderDTO to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body
	 *         the orderDTO, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/orders/{id}")
	public Mono<ResponseEntity<OrderDTO>> getOrder(@PathVariable String id) {
		log.debug("REST request to get Order : {}", id);
		Mono<OrderDTO> orderDTO = orderService.findOne(id);
		return ResponseUtil.wrapOrNotFound(orderDTO);
	}

	/**
	 * {@code DELETE  /orders/:id} : delete the "id" order.
	 *
	 * @param id the id of the orderDTO to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/orders/{id}")
	public Mono<ResponseEntity<Void>> deleteOrder(@PathVariable String id) {
		log.debug("REST request to delete Order : {}", id);
		return orderService.delete(id).then(Mono.just(ResponseEntity.noContent()
				.headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build()));
	}
}
