package com.lineman.wongnai.order.service;

import org.springframework.stereotype.Service;


import com.google.maps.DirectionsApi.RouteRestriction;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleMapsApiService {

	
	public void estimateDistance() {
		GeoApiContext context = new GeoApiContext.Builder().apiKey("AIzaSyDPMD-OqN552LwDTLxjhX8jgQy76-glHuY").build();
	    try {
	        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(context); 
	        DistanceMatrixRow[] rows = req.origins("57WC+WPQ - Downtown Dubai - Dubai - United Arab Emirates",
                    "Crescent Rd - The Palm Jumeirah - Dubai - United Arab Emirates")
	                .destinations("25.1225786,55.2072704")
	                
	                
	                .mode(TravelMode.DRIVING)
	                .language("en-EN")
	                .await().rows;
	       
	        for(DistanceMatrixRow row: rows) {
	        	
	        	
	        	
	        	
	        	for(DistanceMatrixElement ele: row.elements) {
	        		
	        		
	        	}
	        
	        }
	        
	    } catch(ApiException e){
	       
	    } catch(Exception e){
	        
	    }   
	}
}
