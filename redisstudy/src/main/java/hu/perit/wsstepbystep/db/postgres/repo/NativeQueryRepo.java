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

package hu.perit.wsstepbystep.db.postgres.repo;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import hu.perit.spvitamin.spring.data.config.DatasourceCollectionProperties;
import hu.perit.spvitamin.spring.data.config.DatasourceProperties;
import hu.perit.spvitamin.spring.data.nativequery.NativeQueryRepoImpl;
import hu.perit.wsstepbystep.db.postgres.PostgresDbConfig;

@Repository
public class NativeQueryRepo
{
    private final NativeQueryRepoImpl impl;

    public NativeQueryRepo(EntityManager em, DatasourceCollectionProperties dbProperties)
    {
        DatasourceProperties datasourceProperties = dbProperties.getDatasource().get(PostgresDbConfig.PERSISTENCE_UNIT);
        this.impl = new NativeQueryRepoImpl(em, datasourceProperties.getSocketTimeout());
    }

    public List<?> getResultList(String sql)
    {
        return this.getResultList(sql, true);
    }

    public List<?> getResultList(String sql, boolean logSql)
    {
        return this.impl.getResultList(sql, logSql);
    }

    public Object getSingleResult(String sql)
    {
        return this.impl.getSingleResult(sql);
    }

    public Object getSingleResult(String sql, boolean logSql)
    {
        return this.impl.getSingleResult(sql, logSql);
    }

    @Modifying
    @Transactional
    public void executeModifyingQuery(String sql)
    {
        this.impl.executeModifyingQuery(sql);
    }
}
