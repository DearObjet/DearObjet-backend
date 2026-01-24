package app.dearobjet.backend.domain.notification;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice,Long> {
    Page<Notice> findByCategory(NoticeCategory noticeCategory, Pageable pageable);
}
