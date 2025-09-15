package it.korea.app_boot.board.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import it.korea.app_boot.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "board")
public class BoardEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int brdId;

    private String title;

    private String contents;

    private String writer;

    private int readCount;

    private int likeCount;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true) // 기본적으로 fetch = FetchType.LAZY
    @Fetch(FetchMode.SUBSELECT) // N+1 문제를 해결하기 위한 설정, 주 엔티티를 조회한 후, 연관 엔티티들은 서브 쿼리(SUBSELECT)를 사용하여 한 번에 일괄적으로 조회하여 불필요한 추가 쿼리 발생을 막아줌.
    // 데이터가 적을 경우에만 해당 옵션을 사용할 것.
    private Set<BoardFileEntity> fileList;

    public void addFiles(BoardFileEntity entity, boolean isUpdate) {
        if (fileList == null) {
            this.fileList = new HashSet<>();
        }

        entity.setBoard(this);
        fileList.add(entity);

        if (isUpdate) { // 만약 파일을 업데이트 했다면...
            this.preUpdate();  // board 의 updateDate 갱신
        }
    }
}
