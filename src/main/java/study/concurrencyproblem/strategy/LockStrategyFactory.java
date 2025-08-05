package study.concurrencyproblem.strategy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;


@Component
public class LockStrategyFactory {
    private final Map<Strategy, LockStrategy> strategies;

    public LockStrategyFactory(List<LockStrategy> strategyBeans) {
        this.strategies = strategyBeans.stream()
            .collect(Collectors.toUnmodifiableMap(
                LockStrategy::getStrategyType,
                Function.identity()
            ));
    }

    public LockStrategy create(Strategy strategy) {
        LockStrategy strat = strategies.get(strategy);
        if (strat == null) {
            throw new IllegalArgumentException("지원하지 않는 전략: " + strategy);
        }
        return strat;
    }
}