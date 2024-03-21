package com.lineman.wongnai.order.service;

import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;
import com.lineman.wongnai.order.domain.Driver;
import com.lineman.wongnai.order.domain.DriverEvent;
import com.lineman.wongnai.order.domain.Order;
import com.lineman.wongnai.order.domain.OrderEvent;
import com.lineman.wongnai.order.repository.ReactiveDriverEventRepository;
import com.lineman.wongnai.order.repository.ReactiveDriverRepository;
import com.lineman.wongnai.order.service.dto.DriverDTO;
import com.lineman.wongnai.order.service.dto.OrderDTO;
import com.lineman.wongnai.order.service.mapper.DriverMapper;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Driver}.
 */
@Service
public class DriverService {

	private final Logger log = LoggerFactory.getLogger(DriverService.class);

	private final ReactiveDriverRepository driverRepository;

	private final ReactiveDriverEventRepository reactiveDriverEventRepository;

	private final DriverMapper driverMapper;

	public DriverService(ReactiveDriverRepository driverRepository, DriverMapper driverMapper,
			ReactiveDriverEventRepository reactiveDriverEventRepository) {
		this.driverRepository = driverRepository;
		this.driverMapper = driverMapper;
		this.reactiveDriverEventRepository = reactiveDriverEventRepository;
	}

	/**
	 * Save a driver.
	 *
	 * @param driverDTO the entity to save.
	 * @return the persisted entity.
	 */
	public Mono<DriverDTO> save(DriverDTO driverDTO) {
		log.debug("Request to save Driver : {}", driverDTO);
		return driverRepository.save(driverMapper.toEntity(driverDTO)).map(driverMapper::toDto);
	}

	/**
	 * Update a driver.
	 *
	 * @param driverDTO the entity to save.
	 * @return the persisted entity.
	 */
	public Mono<DriverDTO> update(DriverDTO driverDTO) {
		log.debug("Request to partially update Driver : {}", driverDTO);

		return driverRepository.findById(driverDTO.getId()).flatMap(result -> {

			log.debug("rs:" + result.getId());

			result.setStatus(driverDTO.getStatus());

			Mono<Driver> saved = driverRepository.save(result);

			return saved;

		}).flatMap(r -> {
			log.debug("eventId:" + r.getId());
			DriverEvent ordEvt = new DriverEvent();

			ordEvt.setStatus(driverDTO.getStatus());
			ordEvt.setDriverId(driverDTO.getId());

			return reactiveDriverEventRepository.save(ordEvt);
		})

				.map(saved -> {
					DriverDTO dto = new DriverDTO();
					dto.setId(driverDTO.getId());
					return dto;
				});

	}

	public Mono<DriverDTO> partialUpdate(DriverDTO driverDTO) {
		log.debug("Request to partially update Driver : {}", driverDTO);

		return driverRepository.findById(driverDTO.getId()).flatMap(result -> {

			log.debug("rs:" + result.getId());

			result.setStatus(driverDTO.getStatus());
			if (driverDTO.getLatitude() != null)
				result.setLatitude(driverDTO.getLatitude());
			if (driverDTO.getLongitude() != null)
				result.setLongitude(driverDTO.getLongitude());

			Mono<Driver> saved = driverRepository.save(result);

			return saved;

		}).flatMap(r -> {
			log.debug("eventId:" + r.getId());
			DriverEvent ordEvt = new DriverEvent();

			ordEvt.setStatus(driverDTO.getStatus());
			ordEvt.setDriverId(driverDTO.getId());
			ordEvt.setLatitude(r.getLatitude());
			ordEvt.setLongitude(r.getLongitude());

			if (r.getStatus().equals("DRIVING")) {

				try {
					GeoApiContext context = new GeoApiContext.Builder()
							.apiKey("AIzaSyDPMD-OqN552LwDTLxjhX8jgQy76-glHuY").build();

					Map<Integer, Long> map = new TreeMap<>();
					DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context);
					DistanceMatrixRow[] rows = req.origins(driverDTO.getLatitude() + "," + driverDTO.getLongitude())
							.destinations("25.2646231,55.3212473")

							.mode(TravelMode.DRIVING).language("en-EN").await().rows;

					int rwCnt = 0;
					for (DistanceMatrixRow row : rows) {

						DistanceMatrixElement[] eles = row.elements;

						for (DistanceMatrixElement ele : row.elements) {

							map.put(rwCnt, ele.duration.inSeconds);

						}
						rwCnt++;

					}

					if (map.get(0) < 100) {
						ordEvt.setProgress(90);
					} else if (map.get(0) < 500) {
						ordEvt.setProgress(80);
					} else if (map.get(0) < 1000) {
						ordEvt.setProgress(40);
					} else if (map.get(0) < 2000) {
						ordEvt.setProgress(10);
					}

				} catch (Exception e) {
					// TODO: handle exception
				}

			}

			return reactiveDriverEventRepository.save(ordEvt);
		})

				.map(saved -> {
					DriverDTO dto = new DriverDTO();
					dto.setId(driverDTO.getId());
					return dto;
				});

	}

	/**
	 * Get all the drivers.
	 *
	 * @param pageable the pagination information.
	 * @return the list of entities.
	 */
	public Flux<DriverDTO> findAll(Pageable pageable) {
		log.debug("Request to get all Drivers");
		return driverRepository.findAllBy(pageable).map(driverMapper::toDto);
	}

	/**
	 * Returns the number of drivers available.
	 * 
	 * @return the number of entities in the database.
	 *
	 */
	public Mono<Long> countAll() {
		return driverRepository.count();
	}

	/**
	 * Get one driver by id.
	 *
	 * @param id the id of the entity.
	 * @return the entity.
	 */
	public Mono<DriverDTO> findOne(String id) {
		log.debug("Request to get Driver : {}", id);
		return driverRepository.findById(id).map(driverMapper::toDto);
	}

	/**
	 * Delete the driver by id.
	 *
	 * @param id the id of the entity.
	 * @return a Mono to signal the deletion
	 */
	public Mono<Void> delete(String id) {
		log.debug("Request to delete Driver : {}", id);
		return driverRepository.deleteById(id);
	}
}
