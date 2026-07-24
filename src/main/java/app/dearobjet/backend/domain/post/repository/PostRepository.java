package app.dearobjet.backend.domain.post.repository;

import app.dearobjet.backend.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("select p from Post p where p.user.id = :userId and (p.blinded = false or p.blinded is null)")
    Page<Post> findVisibleByUser(@Param("userId") Long userId, Pageable pageable);

    @Query("select p from Post p where p.blinded = false or p.blinded is null")
    Page<Post> findAllVisible(Pageable pageable);

    @Query("select p from Post p where "
            + "lower(p.content) like lower(concat('%', :keyword, '%')) "
            + "or lower(p.user.name) like lower(concat('%', :keyword, '%'))")
    Page<Post> searchForAdmin(@Param("keyword") String keyword, Pageable pageable);
}
