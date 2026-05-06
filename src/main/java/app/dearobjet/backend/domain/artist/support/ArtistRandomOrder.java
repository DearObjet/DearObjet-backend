package app.dearobjet.backend.domain.artist.support;

import java.util.concurrent.ThreadLocalRandom;

public final class ArtistRandomOrder {

    public static final long ORDER_MODULUS = 10_007L;

    private static final long SEED_BOUND = 2_147_483_647L;
    private static final long LCG_MULTIPLIER = 48_271L;
    private static final long LCG_INCREMENT = 12_820_163L;

    private ArtistRandomOrder() {
    }

    public static long generateSeed() {
        return ThreadLocalRandom.current().nextLong(SEED_BOUND);
    }

    public static long normalizeSeed(long seed) {
        return Math.floorMod(seed, SEED_BOUND);
    }

    public static OrderSpec createOrderSpec(long seed) {
        long normalizedSeed = normalizeSeed(seed);
        long state1 = nextState(normalizedSeed);
        long state2 = nextState(state1);
        long state3 = nextState(state2);

        return new OrderSpec(
                (state1 % (ORDER_MODULUS - 1)) + 1,
                (state2 % (ORDER_MODULUS - 1)) + 1,
                state3 % ORDER_MODULUS
        );
    }

    public static long calculateOrderKey(long seed, long artistId) {
        OrderSpec orderSpec = createOrderSpec(seed);

        return calculatePolynomialKey(
                artistId,
                ORDER_MODULUS,
                orderSpec.quadratic(),
                orderSpec.linear(),
                orderSpec.offset()
        );
    }

    private static long nextState(long state) {
        return Math.floorMod((state * LCG_MULTIPLIER) + LCG_INCREMENT, SEED_BOUND);
    }

    private static long calculatePolynomialKey(
            long artistId,
            long modulus,
            long quadratic,
            long linear,
            long offset
    ) {
        long idInModulus = Math.floorMod(artistId, modulus);
        long squaredTerm = (idInModulus * idInModulus) % modulus;

        return Math.floorMod(
                (squaredTerm * quadratic) + (idInModulus * linear) + offset,
                modulus
        );
    }

    public record OrderSpec(
            long quadratic,
            long linear,
            long offset
    ) {
    }
}
