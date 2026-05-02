package app.dearobjet.backend.domain.artist.service;

import app.dearobjet.backend.domain.artist.dto.ArtistResponse;
import app.dearobjet.backend.domain.artist.dto.ArtistListResponse;
import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.artist.support.ArtistRandomOrder;
import app.dearobjet.backend.domain.user.repository.ArtistRepository;
import app.dearobjet.backend.global.exception.ErrorCode;
import app.dearobjet.backend.global.exception.InvalidInputException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;


    @Override
    @Transactional(readOnly = true)
    public ArtistListResponse getArtists(Long seed, Long cursor, int size) {
        if (cursor != null && seed == null) {
            throw new InvalidInputException(ErrorCode.INVALID_INPUT, "cursor 요청에는 seed가 필요합니다.");
        }

        long resolvedSeed = seed == null
                ? ArtistRandomOrder.generateSeed()
                : ArtistRandomOrder.normalizeSeed(seed);

        List<Artist> artists = artistRepository.findRandomArtists(resolvedSeed, cursor, size + 1);
        boolean hasNext = artists.size() > size;

        if (hasNext) {
            artists = artists.subList(0, size);
        }

        Long nextCursor = hasNext && !artists.isEmpty()
                ? artists.get(artists.size() - 1).getId()
                : null;

        List<ArtistResponse> artistResponses = artists.stream()
                .map(ArtistResponse::from)
                .toList();

        return new ArtistListResponse(artistResponses, resolvedSeed, nextCursor, hasNext);
    }
}
