/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.redisstudy.metrics;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

import hu.perit.spvitamin.core.exception.UnexpectedConditionException;
import hu.perit.redisstudy.config.Constants;
import hu.perit.redisstudy.health.HealthIndicatorDatabase;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Getter
@Slf4j
public class MicrometerMetricsService
{

    private final List<HealthIndicator> healthIndicators;
    private final List<HealthIndicator> healthIndicatorsDatabase;

    public MicrometerMetricsService(MeterRegistry registry, MetricsProviderService myMetricProvider,
        HealthContributorRegistry healthContributorRegistry, HealthIndicatorDatabase healthIndicatorDatabase)
    {
        final String METRIC_BOOK_COUNT = Constants.SUBSYSTEM_NAME.toLowerCase() + ".bookcount";
        final String METRIC_HEALTH = Constants.SUBSYSTEM_NAME.toLowerCase() + ".health";
        final String METRIC_HEALTH_DB = Constants.SUBSYSTEM_NAME.toLowerCase() + ".health.db";

        // Book count
        Gauge.builder(METRIC_BOOK_COUNT, myMetricProvider, MetricsProviderService::getBookCount).description(
            "The current count of books").baseUnit("pcs").register(registry);

        // Health indicators
        this.healthIndicators = healthContributorRegistry.stream() //
            .map(c -> this.getIndicatorFromContributor(c)) //
            .collect(Collectors.toList());
        Gauge.builder(METRIC_HEALTH, healthIndicators, MicrometerMetricsService::healthToCode) //
            .description("The current value of the composite health endpoint").register(registry);

        // DB-health indicator
        this.healthIndicatorsDatabase = List.of(healthIndicatorDatabase);
        Gauge.builder(METRIC_HEALTH_DB, healthIndicatorsDatabase, MicrometerMetricsService::healthToCode).description(
            "The current value of the db health endpoint").register(registry);
    }

    private HealthIndicator getIndicatorFromContributor(NamedContributor<HealthContributor> namedContributor)
    {
        log.debug(String.format("Using health contributor: '%s'", namedContributor.getName()));

        HealthContributor contributor = namedContributor.getContributor();
        if (contributor instanceof HealthIndicator)
        {
            return (HealthIndicator) contributor;
        }

        if (contributor instanceof CompositeHealthContributor)
        {
            CompositeHealthContributor compositeHealthContributor = (CompositeHealthContributor) contributor;
            for (NamedContributor<HealthContributor> elementOfComposite : compositeHealthContributor)
            {
                return getIndicatorFromContributor(elementOfComposite);
            }
        }

        throw new UnexpectedConditionException();
    }

    private static int healthToCode(List<HealthIndicator> indicators)
    {
        for (HealthIndicator indicator : indicators)
        {
            Status status = indicator.health().getStatus();
            if (Status.DOWN.equals(status))
            {
                return 0;
            }
        }

        return 1;
    }
}
