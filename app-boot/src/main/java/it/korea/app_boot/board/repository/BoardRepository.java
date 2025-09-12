package it.korea.app_boot.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.korea.app_boot.board.entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
    
    // fetch join 사용해서 N + 1 문제 해결
    @Query(value = """
        select b from BoardEntity b left join fetch b.fileList where b.brdId =:brdId
        """)
    Optional<BoardEntity> getBoard(@Param("brdId") int brdId);
    
}
