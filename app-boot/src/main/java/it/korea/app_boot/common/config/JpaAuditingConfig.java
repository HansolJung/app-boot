package it.korea.app_boot.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;
import java.util.Optional;

// JPA Auditing은 Spring Data JPA의 기능으로, 엔티티가 생성되거나 수정될 때,
// 생성일, 수정일, 생성자, 수정자 등의 메타데이터를 자동으로 기록하여 데이터 변경 시점을 추적할 수 있도록 하는 기술

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "auditorDateTimeProvider")
public class JpaAuditingConfig {

	@Bean(name = "auditorDateTimeProvider")
	public DateTimeProvider auditorDateTimeProvider() {
		return () -> Optional.of(LocalDateTime.now());
	}
}
