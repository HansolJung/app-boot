package it.korea.app_boot.board.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import it.korea.app_boot.board.dto.BoardSearchDTO;
import it.korea.app_boot.board.entity.BoardEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class BoardSearchSpecification implements Specification<BoardEntity> {

    private BoardSearchDTO searchDTO;

    public BoardSearchSpecification(BoardSearchDTO searchDTO) {
        this.searchDTO = searchDTO;
    }

    // root 비교대상 -> entity (JPA 가 만들어서 넣어줌)
    // query : sql 조작 (잘 사용하지 않음)
    // cb : where 조건
    @Override
    public Predicate toPredicate(Root<BoardEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        /*
         * query 파라미터는 sql 을 조작할 수 있지만 잘 사용하지 않는다.
         * 그 이유는 service 또는 pageable 객체에서 이미 정렬 또는 페이징 처리를 하기 때문.
         * 여기서 추가로 조작하면 복잡도가 상승하고 오류 발생시 유지보수가 어려워짐.
         */
        //query.distinct(true);
        //query.groupBy(root.get("title"));
        //query.orderBy(cb.desc(root.get("createDate")));

        String likeText = "%" + searchDTO.getSchText() + "%";

        // NPE 를 막는 방법
        if ("title".equals(searchDTO.getSchType())) {
            predicates.add(cb.like(root.get("title"), likeText));   // 'title like %검색어%' 와 같은 식의 쿼리가 만들어짐
        } else if ("writer".equals(searchDTO.getSchType())) {
            predicates.add(cb.like(root.get("writer"), likeText));  // 'writer like %검색어%' 와 같은 식의 쿼리가 만들어짐
        }

        return andTogether(predicates, cb);
    }

    private Predicate andTogether(List<Predicate> predicates, CriteriaBuilder cb) {
        return cb.and(predicates.toArray(new Predicate[0]));   // 타입 추론. array를 만들 때 new Predicate 객체로 만들겠다는 뜻.
    }
}
