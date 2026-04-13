package app.dearobjet.backend.domain.settlement;

import app.dearobjet.backend.domain.settlement.dto.projection.RecentSettlementArtistRow;
import app.dearobjet.backend.domain.settlement.dto.projection.SettlementMonthlySummaryRow;
import app.dearobjet.backend.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query(value = """
    SELECT
        COALESCE(SUM(st.shop_amount), 0) AS expectedShopAmount,
        COUNT(*) AS settlementCount
    FROM settlement st
    JOIN sales s ON s.sales_id = st.sales_id
    JOIN shop_artist_contracts sac ON sac.shop_artist_contracts_id = st.shop_artist_contracts_id
    JOIN shops sh ON sh.shop_id = sac.shop_id
    WHERE sh.user_id = :userId
      AND s.sale_at >= :startInclusive
      AND s.sale_at < :endExclusive
    """, nativeQuery = true)
    SettlementMonthlySummaryRow findMonthlyForecastByUserId(
            @Param("userId") Long userId,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );

    @Query(value = """
    SELECT
        COALESCE(u.name, a.business_name, 'UNKNOWN') AS artistName,
        COALESCE(st.artist_amount, 0) AS amount
    FROM settlement st
    JOIN sales s ON s.sales_id = st.sales_id
    JOIN shop_artist_contracts sac ON sac.shop_artist_contracts_id = st.shop_artist_contracts_id
    JOIN artists a ON a.artists_id = sac.artists_id
    LEFT JOIN users u ON u.user_id = a.user_id
    JOIN shops sh ON sh.shop_id = sac.shop_id
    WHERE sh.user_id = :userId
      AND s.sale_at >= :startInclusive
      AND s.sale_at < :endExclusive
    ORDER BY s.sale_at DESC, st.settlement_id DESC
    LIMIT 3
    """, nativeQuery = true)
    List<RecentSettlementArtistRow> findRecentSettlementsByUserId(
            @Param("userId") Long userId,
            @Param("startInclusive") LocalDateTime startInclusive,
            @Param("endExclusive") LocalDateTime endExclusive
    );
}
