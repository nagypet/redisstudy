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

package hu.perit.redisstudy.service.bookservice;

import hu.perit.redisstudy.db.postgres.repo.AuthorRepo;
import hu.perit.redisstudy.db.postgres.repo.BookRepo;
import hu.perit.redisstudy.db.postgres.table.AuthorEntity;
import hu.perit.redisstudy.db.postgres.table.BookEntity;
import hu.perit.redisstudy.mapper.AuthorMapper;
import hu.perit.redisstudy.mapper.BookMapper;
import hu.perit.redisstudy.rest.model.BookDTO;
import hu.perit.redisstudy.rest.model.BookParams;
import hu.perit.redisstudy.service.api.BookService;
import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService
{
    private final BookRepo bookRepo;
    private final AuthorRepo authorRepo;

    //------------------------------------------------------------------------------------------------------------------
    // getAllBooks()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public List<BookDTO> getAllBooks()
    {
        List<BookEntity> bookEntities = this.bookRepo.findAll();
        return bookEntities.stream() //
                .map(be -> mapBookEntity2DTO(be)) //
                .collect(Collectors.toList());
    }


    //------------------------------------------------------------------------------------------------------------------
    // getBookById
    //------------------------------------------------------------------------------------------------------------------
    @Override
    //@Cacheable(cacheNames = "book", key = "#id")
    public BookDTO getBookById(Long id) throws ResourceNotFoundException
    {
        Optional<BookEntity> bookEntity = this.bookRepo.findById(id);
        if (bookEntity.isPresent())
        {
            return mapBookEntity2DTO(bookEntity.get());
        }

        throw new ResourceNotFoundException(String.format("Book with id %d cannot be found!", id));
    }


    private BookDTO mapBookEntity2DTO(BookEntity be)
    {
        return BookMapper.INSTANCE.mapEntityToDTO(be);
    }


    //------------------------------------------------------------------------------------------------------------------
    // createBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    public long createBook(BookParams bookParams)
    {
        return createOrUpdateBookEntity(bookParams, null);
    }


    public long createOrUpdateBookEntity(BookParams bookParams, BookEntity destinationBookEntity)
    {
        BookEntity bookEntity = null;
        if (destinationBookEntity != null)
        {
            // Mapping fields of bookParams into destinationBookEntity
            bookEntity = destinationBookEntity;
            BookMapper.INSTANCE.updateEntityWithNonNulls(bookParams, bookEntity);
        }
        else
        {
            // Creating a new BookEntity object
            bookEntity = BookMapper.INSTANCE.createEntity(bookParams);
        }

        // Authors
        if (bookParams.getAuthors() != null)
        {
            // First we save the authors without id
            List<AuthorEntity> newAuthorsToSave = bookParams.getAuthors().stream() //
                    .filter(a -> LongUtils.isBlank(a.getId())) //
                    .map(dto -> AuthorMapper.INSTANCE.createEntity(dto)).collect(Collectors.toList());
            List<AuthorEntity> newAuthorEntities = this.authorRepo.saveAll(newAuthorsToSave);

            // Now gather authors with id
            List<Long> authorIds = bookParams.getAuthors().stream() //
                    .filter(a -> LongUtils.isNotBlank(a.getId())) //
                    .map(dto -> dto.getId())
                    .distinct()
                    .collect(Collectors.toList());
            List<AuthorEntity> existingAuthorEntities = this.authorRepo.findAllById(authorIds);

            Set<AuthorEntity> authorEntities = new HashSet<>();
            authorEntities.addAll(existingAuthorEntities);
            authorEntities.addAll(newAuthorEntities);

            if (!authorEntities.equals(bookEntity.getAuthorEntities()))
            {
                bookEntity.setAuthorEntities(authorEntities);
            }
        }
        else
        {
            bookEntity.setAuthorEntities(null);
        }

        BookEntity savedEntity = this.bookRepo.save(bookEntity);

        return savedEntity.getId();
    }


    //------------------------------------------------------------------------------------------------------------------
    // updateBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @Transactional
    //@CacheEvict(cacheNames = "book", key = "#id")
    public void updateBook(Long id, BookParams bookParams) throws ResourceNotFoundException
    {
        Optional<BookEntity> byId = this.bookRepo.findById(id);
        if (!byId.isPresent())
        {
            throw new ResourceNotFoundException(String.format("Book with id %d cannot be found!", id));
        }

        createOrUpdateBookEntity(bookParams, byId.get());
    }


    //------------------------------------------------------------------------------------------------------------------
    // deleteBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void deleteBook(Long id) throws ResourceNotFoundException
    {
        Optional<BookEntity> byId = this.bookRepo.findById(id);
        if (!byId.isPresent())
        {
            throw new ResourceNotFoundException(String.format("Book with id %d cannot be found!", id));
        }

        this.bookRepo.deleteById(id);
    }
}
