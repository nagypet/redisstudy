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

import java.util.concurrent.TimeoutException;

import hu.perit.redisstudy.db.postgres.repo.BookRepo;
import hu.perit.spvitamin.core.timeoutlatch.TimeoutLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.perit.spvitamin.spring.config.SysConfig;
import hu.perit.spvitamin.spring.metrics.AsyncExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class MetricsProviderService
{
    @Autowired
    private BookRepo bookRepo;

    private TimeoutLatch timeoutLatch;

    @PostConstruct
    private void PostConstruct() {
        this.timeoutLatch = new TimeoutLatch(SysConfig.getMetricsProperties().getMetricsGatheringHysteresisMillis());
    }

    public double getBookCount()
    {
        try
        {
            long bookCount = AsyncExecutor.invoke(this::getTotalBookCount, null);
            return (double) bookCount;
        }
        catch (TimeoutException ex)
        {
            this.timeoutLatch.setClosed();
            log.error(String.format("getTotalBookCount() did not complete within %d ms! The database is not reachable or slow!",
                SysConfig.getMetricsProperties().getTimeoutMillis()));
        }

        return 0.0;
    }


    private long getTotalBookCount()
    {
        if (this.timeoutLatch.isClosed())
        {
            return 0;
        }

        return bookRepo.count();
    }
}
