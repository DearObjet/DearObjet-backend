package app.dearobjet.backend.domain.story;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Page<Story> findByUser_Id(Long userId, Pageable pageable);

    Page<Story> findByUser_IdAndBlindedFalse(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Story> findByStoryIdAndUser_Id(Long storyId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("select s from Story s where "
            + "lower(s.title) like lower(concat('%', :keyword, '%')) "
            + "or lower(s.user.name) like lower(concat('%', :keyword, '%'))")
    Page<Story> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
