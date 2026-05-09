package app.dearobjet.backend.domain.story;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Page<Story> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Story> findByStoryIdAndUser_Id(Long storyId, Long userId);
}
