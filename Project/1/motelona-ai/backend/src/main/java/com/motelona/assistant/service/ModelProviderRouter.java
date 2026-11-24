package com.motelona.assistant.service;

import java.util.Comparator;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Tries configured providers in priority order, then lets the caller use local retrieval. */
@Service
public class ModelProviderRouter {
    private static final Logger log = LoggerFactory.getLogger(ModelProviderRouter.class);
    private static final Duration TOTAL_REQUEST_BUDGET = Duration.ofSeconds(12);
    private static final Duration MAX_PROVIDER_ATTEMPT = Duration.ofSeconds(7);
    private final List<GenerativeModelProvider> providers;

    public ModelProviderRouter(List<GenerativeModelProvider> providers) {
        this.providers = providers.stream().sorted(Comparator.comparingInt(GenerativeModelProvider::priority)).toList();
    }

    public ProviderAnswer answer(ModelPrompt prompt) {
        AiUnavailableException lastFailure = null;
        long deadline = System.nanoTime() + TOTAL_REQUEST_BUDGET.toNanos();
        for (GenerativeModelProvider provider : providers) {
            if (!provider.isConfigured()) continue;
            long remainingNanos = deadline - System.nanoTime();
            if (remainingNanos < Duration.ofMillis(100).toNanos()) break;
            Duration attemptTimeout = Duration.ofNanos(Math.min(remainingNanos, MAX_PROVIDER_ATTEMPT.toNanos()));
            try {
                long attemptStarted = System.nanoTime();
                String text = provider.answer(prompt, attemptTimeout);
                long elapsedMillis = (System.nanoTime() - attemptStarted) / 1_000_000;
                log.debug("Model provider {} completed in {}ms", provider.name(), elapsedMillis);
                return new ProviderAnswer(provider.name(), provider.modelName(), text);
            } catch (ModelRefusalException ex) {
                throw ex;
            } catch (RuntimeException ex) {
                lastFailure = ex instanceof AiUnavailableException unavailable
                        ? unavailable : new AiUnavailableException("Model provider request failed.");
                long remainingMillis = Math.max(0, (deadline - System.nanoTime()) / 1_000_000);
                log.warn("Model provider {} failed; request budget remaining={}ms", provider.name(), remainingMillis);
            }
        }
        if (lastFailure != null) throw new AiUnavailableException("Không có model provider nào phản hồi trong thời gian cho phép.", lastFailure);
        throw new AiUnavailableException("Chưa cấu hình model provider nào.");
    }

    public boolean isConfigured() { return providers.stream().anyMatch(GenerativeModelProvider::isConfigured); }
    public String primaryProvider() {
        return providers.stream().filter(GenerativeModelProvider::isConfigured).map(GenerativeModelProvider::name)
                .findFirst().orElse("local-knowledge-fallback");
    }
    public String primaryModel() {
        return providers.stream().filter(GenerativeModelProvider::isConfigured).map(GenerativeModelProvider::modelName)
                .findFirst().orElse("local-knowledge-fallback");
    }
    public List<String> configuredProviders() {
        return providers.stream().filter(GenerativeModelProvider::isConfigured).map(GenerativeModelProvider::name).toList();
    }

    public record ProviderAnswer(String provider, String model, String text) { }
}
