package app.dearobjet.backend.domain.notification;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice,Long> {

    @Query("select n from Notice n where "
            + "(:target is null or n.target = :target) and "
            + "(:category is null or n.category = :category) and "
            + "lower(n.title) like lower(concat('%', :keyword, '%')) "
            + "order by case when n.pinned = true then 0 else 1 end, n.publishedAt desc")
    Page<Notice> searchForAdmin(@Param("target") NoticeTarget target,
                                @Param("category") NoticeCategory category,
                                @Param("keyword") String keyword,
                                Pageable pageable);

    @Query("select n from Notice n where n.target = :target "
            + "and (:category is null or n.category = :category) "
            + "and (n.status is null or n.status = app.dearobjet.backend.domain.notification.NoticeStatus.PUBLISHED)")
    Page<Notice> findPublicByTarget(@Param("target") NoticeTarget target,
                                    @Param("category") NoticeCategory category,
                                    Pageable pageable);
}
