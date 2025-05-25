package id.ac.ui.cs.advprog.authprofile.client;

import id.ac.ui.cs.advprog.authprofile.dto.rating.ApiResponseDto;
import id.ac.ui.cs.advprog.authprofile.dto.rating.RatingResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Service
public class RatingClientService {

    private final RestTemplate restTemplate;
    private final String ratingServiceUrl;

    public RatingClientService(RestTemplate restTemplate,
                               @Value("${service.rating.url}") String ratingServiceUrl) {
        this.restTemplate = restTemplate;
        this.ratingServiceUrl = ratingServiceUrl;
    }

    public List<RatingResponseDto> getRatingsByDoctorId(Long doctorId) {
        String url = ratingServiceUrl + "/api/rating/doctor/" + doctorId;

        ResponseEntity<ApiResponseDto<RatingResponseDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<RatingResponseDto>>() {}
        );

        if (response.getBody() != null && response.getBody().getSuccess() == 1) {
            return response.getBody().getData();
        }
        return List.of();
    }
}
