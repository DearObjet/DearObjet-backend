package app.dearobjet.backend.domain.artist.support;

import java.util.concurrent.ThreadLocalRandom;

public final class ArtistRandomOrder {

    public static final long MULTIPLIER = 48_271L;
    public static final long PRIME = 2_147_483_647L;

    private ArtistRandomOrder() {
    }

    public static long generateSeed() {
        return ThreadLocalRandom.current().nextLong(0, PRIME);
    }

    public static long normalizeSeed(long seed) {
        return Math.floorMod(seed, PRIME);
    }

    public static long calculateOrderKey(long seed, long artistId) {
        return Math.floorMod((artistId * MULTIPLIER) + normalizeSeed(seed), PRIME);
    }
}
