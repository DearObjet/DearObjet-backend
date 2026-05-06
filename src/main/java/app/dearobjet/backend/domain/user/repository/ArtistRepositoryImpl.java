package app.dearobjet.backend.domain.user.repository;

import app.dearobjet.backend.domain.artist.entity.Artist;
import app.dearobjet.backend.domain.artist.support.ArtistRandomOrder;
import app.dearobjet.backend.domain.artist.support.ArtistRandomOrder.OrderSpec;
import app.dearobjet.backend.domain.user.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ArtistRepositoryImpl implements ArtistRepositoryCustom {

    private static final String ORDER_EXPRESSION = """
            MOD(
                (MOD(a.id, :orderModulus) * MOD(a.id, :orderModulus) * :quadratic)
                + (MOD(a.id, :orderModulus) * :linear)
                + :offset,
                :orderModulus
            )
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Artist> findRandomArtists(long seed, Long cursorArtistId, int limit) {
        OrderSpec orderSpec = ArtistRandomOrder.createOrderSpec(seed);
        StringBuilder jpql = new StringBuilder("""
                select a
                from Artist a
                join fetch a.user u
                where u.userStatus = :userStatus
                """);

        if (cursorArtistId != null) {
            jpql.append("""
                     and (
                        """).append(ORDER_EXPRESSION).append("""
                         > :cursorOrderKey
                         or (
                            """).append(ORDER_EXPRESSION).append("""
                             = :cursorOrderKey
                             and a.id > :cursorArtistId
                         )
                     )
                    """);
        }

        jpql.append(" order by ")
                .append(ORDER_EXPRESSION)
                .append(" asc, a.id asc");

        TypedQuery<Artist> query = entityManager.createQuery(jpql.toString(), Artist.class);
        applyOrderParameters(query, orderSpec);
        query.setParameter("userStatus", UserStatus.ACTIVE)
                .setMaxResults(limit);

        if (cursorArtistId != null) {
            query.setParameter("cursorArtistId", cursorArtistId);
            query.setParameter("cursorOrderKey", ArtistRandomOrder.calculateOrderKey(seed, cursorArtistId));
        }

        return query.getResultList();
    }

    private void applyOrderParameters(TypedQuery<Artist> query, OrderSpec orderSpec) {
        query.setParameter("orderModulus", ArtistRandomOrder.ORDER_MODULUS);
        query.setParameter("quadratic", orderSpec.quadratic());
        query.setParameter("linear", orderSpec.linear());
        query.setParameter("offset", orderSpec.offset());
    }
}
