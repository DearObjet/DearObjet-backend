package app.dearobjet.backend.domain.artist.controller;

import app.dearobjet.backend.domain.artist.dto.ArtistListResponse;
import app.dearobjet.backend.domain.artist.service.ArtistService;
import app.dearobjet.backend.global.api.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/artists")
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping
    public ApiResponse<ArtistListResponse> getArtists(
            @RequestParam(required = false) Long seed,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "15") @Min(1) @Max(50) int size
    ) {
        return ApiResponse.of(artistService.getArtists(seed, cursor, size));
    }
}
