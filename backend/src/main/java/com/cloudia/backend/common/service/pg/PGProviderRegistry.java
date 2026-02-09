package com.cloudia.backend.common.service.pg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PGProviderRegistry {

    /**
     * PG 종류 > Provider 매핑
     */
    private final Map<String, PGProvider> providerMap = new HashMap<>();

    /**
     * 구/신 PG 타입 명칭 차이, 프론트 상수 변경 등에 대비한 alias 맵핑
     */
    private static final Map<String, String> TYPE_ALIASES = Map.of(
            "PAYVERSE", "COOKIEPAY"
    );

    /**
     * 생성자에서 스프링이 PGProvider 구현체들을 자동 주입
     */
    public PGProviderRegistry(List<PGProvider> providers) {
        for (PGProvider provider : providers) {
            String type = provider.getProviderType().toUpperCase();
            providerMap.put(type, provider);
            log.info("[PGProviderRegistry] Registered PG Provider: {}", type);
        }
    }

    /**
     * Provider 를 가져오기
     */
    public PGProvider getProvider(String pgType) {

        if (pgType == null) {
            throw new IllegalArgumentException("PG 타입이 null 입니다.");
        }

        String normalized = pgType.toUpperCase();
        String resolved = TYPE_ALIASES.getOrDefault(normalized, normalized);

        PGProvider provider = providerMap.get(resolved);

        if (provider == null) {
            throw new IllegalArgumentException("지원하지 않는 PG 타입입니다: " + pgType);
        }

        return provider;
    }
}
