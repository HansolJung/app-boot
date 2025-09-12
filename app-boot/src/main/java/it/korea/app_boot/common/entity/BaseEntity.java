package it.korea.app_boot.common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@MappedSuperclass  // 이 클래스의 필드와 매핑 정보를 상속받는 다른 엔티티 클래스에 제공(실제로 상속 관계는 아님)
@EntityListeners(AuditingEntityListener.class) // JPA의 엔티티 리스너를 사용하여 엔티티의 생성 및 수정 시간을 자동으로 기록하고 관리하는 기능을 활성화
public class BaseEntity implements Serializable {

    @CreatedDate  // 생성될때 실행됨
    @Column(updatable = false)  // JPA 는 부분 업데이트가 없다. 그래서 값이 존재하면 그냥 업데이트를 해버리기 때문에 updatable = false 옵션을 줘야한다.
    private LocalDateTime createDate;

    // 업데이트 될때만 갱신
    private LocalDateTime updateDate;

    /**
     * 업데이트 시간 갱신
     * 업데이트 되기 전에 실행됨
     */
    @PreUpdate
    public void preUpdate() {
        this.updateDate = LocalDateTime.now();
    }
}
