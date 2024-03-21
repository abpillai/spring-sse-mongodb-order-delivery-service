package com.lineman.wongnai.order.service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;
import com.lineman.wongnai.order.domain.Customer;
import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.DriverEvent;
import com.lineman.wongnai.order.domain.Order;
import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.domain.enumeration.OrderStatus;
import com.lineman.wongnai.order.producer.SqsMessageProducer;
import com.lineman.wongnai.order.repository.DriverEventRepository;
import com.lineman.wongnai.order.repository.DriverRepository;
import com.lineman.wongnai.order.repository.OrderEventRepository;
import com.lineman.wongnai.order.repository.OrderRepository;
import com.lineman.wongnai.order.repository.ReactiveDriverRepository;
import com.lineman.wongnai.order.repository.ReactiveOrderEventRepository;
import com.lineman.wongnai.order.repository.ReactiveOrderRepository;
import com.lineman.wongnai.order.service.dto.CustomerDTO;
import com.lineman.wongnai.order.service.dto.DriverDTO;
import com.lineman.wongnai.order.service.dto.OrderDTO;
import com.lineman.wongnai.order.service.mapper.OrderMapper;
import com.lineman.wongnai.order.web.rest.errors.BadRequestAlertException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;

/**
 * Service Implementation for managing {@link Order}.
 */
@Service
public class OrderService {

	private final Logger log = LoggerFactory.getLogger(OrderService.class);

	private final ReactiveOrderRepository orderRepository;

	private final ReactiveDriverRepository driverRepository;

	private final DriverRepository blockingDriverRepositoryy;

	private final OrderMapper orderMapper;

	private final SqsMessageProducer producer;

	private final OrderRepository lockingOrderRepository;

	private final ReactiveOrderEventRepository reactiveOrderEventRepository;

	private final DriverEventRepository driverEventRepository;

	private final OrderEventRepository orderEventRepository;

	public OrderService(ReactiveOrderRepository orderRepository, OrderMapper orderMapper, SqsMessageProducer producer,
			ReactiveDriverRepository driverRepository, OrderRepository lockingOrderRepository,
			DriverRepository blockingDriverRepositoryy, ReactiveOrderEventRepository reactiveOrderEventRepository,
			DriverEventRepository driverEventRepository, OrderEventRepository orderEventRepository) {
		this.orderRepository = orderRepository;
		this.orderMapper = orderMapper;
		this.producer = producer;
		this.driverRepository = driverRepository;
		this.lockingOrderRepository = lockingOrderRepository;
		this.blockingDriverRepositoryy = blockingDriverRepositoryy;
		this.reactiveOrderEventRepository = reactiveOrderEventRepository;
		this.driverEventRepository = driverEventRepository;
		this.orderEventRepository = orderEventRepository;
	}

	/**
	 * Save a order.
	 *
	 * @param orderDTO the entity to save.
	 * @return the persisted entity.
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Mono<OrderDTO> save(OrderDTO orderDTO) {
		log.debug("Request to save Order : {}", orderDTO);

		return orderRepository.save(orderMapper.toEntity(orderDTO)).map(orderMapper::toDto);
	}

	public Mono<OrderDTO> partialUpdate(OrderDTO orderDTO) {
		log.debug("Request to partially update Order : {}", orderDTO);

		return orderRepository.findById(orderDTO.getId()).flatMap(result -> {
			// result.setId(result.getId());
			log.debug("rs:" + result.getId());
			// result.setId("65e454039854d45cece1f51d");
			result.setStatus(orderDTO.getStatus());
			result.setTime(Instant.now());
			Mono<Order> saved = orderRepository.save(result);

			return saved;

		}).flatMap(r -> {
			log.debug("eventId:" + r.getId());

			OrderEvent ordEvt = new OrderEvent();

			ordEvt.setStatus(orderDTO.getStatus());
			ordEvt.setOrderId(orderDTO.getId());
			ordEvt.setTime(Instant.now());

			if (r.getStatus().equals(OrderStatus.ACCEPTED))
				ordEvt.setProgress(5);
			else if (r.getStatus().equals(OrderStatus.ASSIGNED))
				ordEvt.setProgress(10);
			else if (r.getStatus().equals(OrderStatus.PREPARED))
				ordEvt.setProgress(50);

			return reactiveOrderEventRepository.save(ordEvt);

		})

				.map(saved -> {
					OrderDTO dto = new OrderDTO();
					dto.setId(orderDTO.getId());
					return dto;
				});

		/*
		 * .flatMap(ss -> {
		 * 
		 * OrderEvent ordEvt = new OrderEvent(); ordEvt.setStatus(ss.getStatus());
		 * ordEvt.setOrderId(ss.getId()); ordEvt.setTime(Instant.now()); return
		 * reactiveOrderEventRepository.save(ordEvt);
		 * 
		 * });
		 */

	}

	public Mono<OrderDTO> update(OrderDTO orderDTO) {
		log.debug("Request to update Order : {}", orderDTO);

		return orderRepository.findById(orderDTO.getId()).flatMap(result -> {

			log.debug("rs:" + result.getId());

			result.setStatus(orderDTO.getStatus());
			result.setTime(Instant.now());
			Mono<Order> saved = orderRepository.save(result);

			return saved;

		}).flatMap(r -> {
			log.debug("eventId:" + r.getId());
			OrderEvent ordEvt = new OrderEvent();

			ordEvt.setStatus(orderDTO.getStatus());
			ordEvt.setOrderId(orderDTO.getId());
			ordEvt.setTime(Instant.now());
			return reactiveOrderEventRepository.save(ordEvt);
		})

				.map(saved -> {
					OrderDTO dto = new OrderDTO();
					dto.setId(saved.getId());
					return dto;
				});

	}

	@Transactional
	public void cancelOrder(OrderDTO orderDto) throws Exception {
		Optional<Order> orderFound = lockingOrderRepository.findById(orderDto.getId());
		Order order = orderFound.get();
		order.setStatus(orderDto.getStatus());
		lockingOrderRepository.save(order);

		OrderEvent orEv = new OrderEvent();
		orEv.setOrderId(order.getId());
		orEv.setStatus(orderDto.getStatus());
		orEv.setTime(Instant.now());
		orEv.setProgress(0);
		orderEventRepository.save(orEv);

		Driver d = orderFound.get().getDriver();
		Optional<Driver> driverFound = blockingDriverRepositoryy.findById(d.getId());
		Driver foundD = driverFound.get();
		foundD.setStatus("AVAILABLE");
		blockingDriverRepositoryy.save(foundD);

		DriverEvent dre = new DriverEvent();
		dre.setStatus("AVAILABLE");
		dre.setDriverId(foundD.getId());
		dre.setLatitude(foundD.getLatitude());
		dre.setLongitude(foundD.getLongitude());
		dre.setProgress(0);
		driverEventRepository.save(dre);

	}

	@Transactional
	public void completeOrder(OrderDTO orderDto) throws Exception {
		Optional<Order> orderFound = lockingOrderRepository.findById(orderDto.getId());
		Order order = orderFound.get();
		order.setStatus(orderDto.getStatus());
		lockingOrderRepository.save(order);

		OrderEvent orEv = new OrderEvent();
		orEv.setOrderId(order.getId());
		orEv.setStatus(orderDto.getStatus());
		orEv.setTime(Instant.now());
		orEv.setProgress(100);
		orderEventRepository.save(orEv);

		Driver d = orderFound.get().getDriver();
		Optional<Driver> driverFound = blockingDriverRepositoryy.findById(d.getId());
		Driver foundD = driverFound.get();
		foundD.setStatus("AVAILABLE");
		blockingDriverRepositoryy.save(foundD);

		DriverEvent dre = new DriverEvent();
		dre.setStatus("AVAILABLE");
		dre.setDriverId(foundD.getId());
		dre.setLatitude(foundD.getLatitude());
		dre.setLongitude(foundD.getLongitude());
		dre.setProgress(0);
		driverEventRepository.save(dre);

	}

	@Transactional
	public void processOrder(OrderDTO orderDto, List<Driver> drivers) throws Exception {
		log.debug(" ################################## Process order from queue {} " + orderDto);

		String latitude = orderDto.getLatitude();
		String longitude = orderDto.getLongitude();

		String[] locations = new String[drivers.size()];
		for (int i = 0; i < drivers.size(); i++)
			locations[i] = drivers.get(i).getLatitude() + "," + drivers.get(i).getLongitude();

		GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyDPMD-OqN552LwDTLxjhX8jgQy76-glHuY").build();
		try {
			Map<Long, Integer> map = new TreeMap<>();
			DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context);
			DistanceMatrixRow[] rows = req.origins(locations).destinations(latitude + "," + longitude)

					.mode(TravelMode.DRIVING).language("en-EN").await().rows;

			int rwCnt = 0;
			for (DistanceMatrixRow row : rows) {

				DistanceMatrixElement[] eles = row.elements;

				for (DistanceMatrixElement ele : row.elements) {

					map.put(ele.distance.inMeters, rwCnt);

				}
				rwCnt++;

			}

			Map.Entry<Long, Integer> entry = map.entrySet().iterator().next();
			Integer value = entry.getValue();

			Optional<Order> orderFound = lockingOrderRepository.findById(orderDto.getId());

			Order ord = orderFound.get();
			Driver d = new Driver();
			d.setId(drivers.get(value).getId());
			ord.setDriver(d);
			ord.setStatus(OrderStatus.ASSIGNED);
			lockingOrderRepository.save(ord);
			
			OrderEvent ore = new OrderEvent();
			ore.setStatus(OrderStatus.ASSIGNED);
			ore.setOrderId(ord.getId());
			ore.setTime(Instant.now());
			ore.setProgress(10);
			orderEventRepository.save(ore);
			
		
			Optional<Driver> driverFound = blockingDriverRepositoryy.findById(drivers.get(value).getId());
			Driver drv = driverFound.get();
			drv.setStatus("ASSIGNED");
			drv.setOrderId(ord.getId());
			blockingDriverRepositoryy.save(drv);

			DriverEvent dre = new DriverEvent();
			dre.setStatus("ASSIGNED");
			dre.setDriverId(drv.getId());
			dre.setLatitude(drv.getLatitude());
			dre.setLongitude(drv.getLongitude());
			driverEventRepository.save(dre);

		} catch (ApiException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		/*
		 * do the good order processing logic here not going to do that here, thats not
		 * the point of this demo app
		 * 
		 */
	}

	public OrderDTO placeOrder(OrderDTO orderDto) {
		log.debug(" {} Place order {} " + orderDto);
		// orderDto.setOrderLatitude(UUID.randomUUID());
		// orderDto.setOrderDate(new Date());

		Map<String, Object> headers = new HashMap<>();
		headers.put("Message-Type", "ORDER");
		// headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		headers.put("message-group-id", "ORDER");
		// headers.put("message-deduplication-id", "ORDER");
		log.debug("{} Send message to Queue {} ");
		this.producer.send(orderDto, headers);
		return orderDto;
	}

	/**
	 * Update a order.
	 *
	 * @param orderDTO the entity to save.
	 * @return the persisted entity.
	 */

	/**
	 * Partially update a order.
	 *
	 * @param orderDTO the entity to update partially.
	 * @return the persisted entity.
	 */

	public Optional<OrderDTO> partialUpdateCustom(OrderDTO orderDTO) {
		log.debug("Request to partially update Order : {}", orderDTO);

		return lockingOrderRepository.findById(orderDTO.getId()).map(existingOrder -> {
			orderMapper.partialUpdate(existingOrder, orderDTO);

			return existingOrder;
		}).map(lockingOrderRepository::save).map(orderMapper::toDto);
	}

	/**
	 * Get all the orders.
	 *
	 * @param pageable the pagination information.
	 * @return the list of entities.
	 */
	public Flux<OrderDTO> findAll(Pageable pageable) {
		log.debug("Request to get all Orders");
		return orderRepository.findAllBy(pageable).map(orderMapper::toDto);
	}

	/**
	 * Returns the number of orders available.
	 * 
	 * @return the number of entities in the database.
	 *
	 */
	public Mono<Long> countAll() {
		return orderRepository.count();
	}

	/**
	 * Get one order by id.
	 *
	 * @param id the id of the entity.
	 * @return the entity.
	 */
	public Mono<OrderDTO> findOne(String id) {
		log.debug("Service to get Order : {}", id);
		return orderRepository.findById(id).map(this::toDto);
	}

	public OrderDTO toDto(Order s) {
		log.debug("toDto");
		if (s == null) {
			return null;
		}

		OrderDTO orderDTO = new OrderDTO();

		orderDTO.setCustomer(toDtoCustomer(s.getCustomer()));
		orderDTO.setDriver(toDtoDriver(s.getDriver()));
		orderDTO.setId(s.getId());
		orderDTO.setStatus(s.getStatus());
		orderDTO.setTime(s.getTime());
		orderDTO.setType(s.getType());

		return orderDTO;
	}

	public CustomerDTO toDtoCustomer(Customer customer) {
		log.debug("toDtoCustomer");
		if (customer == null) {
			return null;
		}

		CustomerDTO customerDTO = new CustomerDTO();

		customerDTO.setId(customer.getId());
		log.debug("customer.getLatitude():" + customer.getLatitude());
		customerDTO.setLatitude(customer.getLatitude());
		return customerDTO;
	}

	public DriverDTO toDtoDriver(Driver driver) {
		if (driver == null) {
			return null;
		}

		DriverDTO driverDTO = new DriverDTO();

		driverDTO.setId(driver.getId());
		driverDTO.setLatitude(driver.getLatitude());
		return driverDTO;
	}

	/**
	 * Delete the order by id.
	 *
	 * @param id the id of the entity.
	 * @return a Mono to signal the deletion
	 */
	public Mono<Void> delete(String id) {
		log.debug("Request to delete Order : {}", id);
		return orderRepository.deleteById(id);
	}
}
