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

package hu.perit.redisstudy.service.authorservice;

import hu.perit.redisstudy.db.postgres.repo.AuthorRepo;
import hu.perit.redisstudy.db.postgres.table.AuthorEntity;
import hu.perit.redisstudy.mapper.AuthorWithBooksMapper;
import hu.perit.redisstudy.rest.model.AuthorWithBooksDTO;
import hu.perit.redisstudy.service.api.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService
{
    private final AuthorRepo authorRepo;

    //------------------------------------------------------------------------------------------------------------------
    // getAllAuthors()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public List<AuthorWithBooksDTO> getAllAuthors()
    {
        List<AuthorEntity> authorEntities = this.authorRepo.findAll();
        return authorEntities.stream() //
                .map(ae -> mapAuthorEntity2DTO(ae)) //
                .collect(Collectors.toList());
    }


    private AuthorWithBooksDTO mapAuthorEntity2DTO(AuthorEntity authorEntity)
    {
        return AuthorWithBooksMapper.INSTANCE.mapEntityToDTO(authorEntity);
    }
}
