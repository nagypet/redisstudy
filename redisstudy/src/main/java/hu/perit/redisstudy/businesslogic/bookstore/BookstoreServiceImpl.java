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

package hu.perit.redisstudy.businesslogic.bookstore;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import hu.perit.redisstudy.rest.model.AuthorWithBooksDTO;
import hu.perit.redisstudy.rest.model.BookDTO;
import hu.perit.redisstudy.rest.model.BookParams;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.perit.spvitamin.core.typehelpers.LongUtils;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.redisstudy.businesslogic.api.BookstoreService;
import hu.perit.redisstudy.db.postgres.repo.AuthorRepo;
import hu.perit.redisstudy.db.postgres.repo.BookRepo;
import hu.perit.redisstudy.db.postgres.table.AuthorEntity;
import hu.perit.redisstudy.db.postgres.table.BookEntity;

@Service
public class BookstoreServiceImpl implements BookstoreService
{
    @Autowired
    private BookRepo bookRepo;

    @Autowired
    private AuthorRepo authorRepo;

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
        ModelMapper modelMapper = new ModelMapper();

        return modelMapper.map(be, BookDTO.class);
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
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

        BookEntity bookEntity = null;
        if (destinationBookEntity != null)
        {
            // Mapping fields of bookParams into destinationBookEntity
            bookEntity = destinationBookEntity;
            modelMapper.map(bookParams, bookEntity);
        }
        else
        {
            // Creating a new BookEntity object
            bookEntity = modelMapper.map(bookParams, BookEntity.class);
        }

        // Authors
        if (bookParams.getAuthors() != null)
        {
            // First we save the authors without id
            List<AuthorEntity> newAuthorsToSave = bookParams.getAuthors().stream() //
                    .filter(a -> LongUtils.isBlank(a.getId())) //
                    .map(dto -> modelMapper.map(dto, AuthorEntity.class)).collect(Collectors.toList());
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

            if (!bookEntity.getAuthors().equals(authorEntities))
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


    private AuthorWithBooksDTO mapAuthorEntity2DTO(AuthorEntity be)
    {
        ModelMapper modelMapper = new ModelMapper();

        return modelMapper.map(be, AuthorWithBooksDTO.class);
    }
}
