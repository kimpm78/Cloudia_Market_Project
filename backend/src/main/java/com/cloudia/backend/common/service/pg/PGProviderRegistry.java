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
     * PG種別 → Provider のマッピング
     */
    private final Map<String, PGProvider> providerMap = new HashMap<>();

    /**
     * 旧／新PGタイプ名の差異やフロント定数変更に備えたエイリアスのマッピング
     */
    private static final Map<String, String> TYPE_ALIASES = Map.of(
            "PAYVERSE", "COOKIEPAY"
    );

    /**
     * コンストラクタで Spring が PGProvider 実装を自動注入
     */
    public PGProviderRegistry(List<PGProvider> providers) {
        for (PGProvider provider : providers) {
            String type = provider.getProviderType().toUpperCase();
            providerMap.put(type, provider);
            log.info("[PGProviderRegistry] Registered PG Provider: {}", type);
        }
    }

    /**
     * Provider を取得
     */
    public PGProvider getProvider(String pgType) {

        if (pgType == null) {
            throw new IllegalArgumentException("PGタイプが null です。");
        }

        String normalized = pgType.toUpperCase();
        String resolved = TYPE_ALIASES.getOrDefault(normalized, normalized);

        PGProvider provider = providerMap.get(resolved);

        if (provider == null) {
            throw new IllegalArgumentException("未対応のPGタイプです: " + pgType);
        }

        return provider;
    }
}
