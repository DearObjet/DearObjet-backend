package app.dearobjet.backend.domain.post.repository;

import app.dearobjet.backend.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByUser_Id(Long userId, Pageable pageable);
}
