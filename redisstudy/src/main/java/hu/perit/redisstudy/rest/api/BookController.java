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

package hu.perit.redisstudy.rest.api;

import hu.perit.redisstudy.config.Constants;
import hu.perit.redisstudy.rest.model.BookDTO;
import hu.perit.redisstudy.rest.model.BookParams;
import hu.perit.redisstudy.rest.model.ResponseUri;
import hu.perit.redisstudy.service.api.BookService;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookController implements BookApi
{
    private final AuthorizationService authorizationService;
    private final BookService bookService;


    //------------------------------------------------------------------------------------------------------------------
    // getAllBooks()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_GET_ALL_BOOKS, subsystem = Constants.SUBSYSTEM_NAME)
    public List<BookDTO> getAllBooks()
    {
        return this.bookService.getAllBooks();
    }


    //------------------------------------------------------------------------------------------------------------------
    // getBookById
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_GET_BOOK_BY_ID, subsystem = Constants.SUBSYSTEM_NAME)
    public BookDTO getBookById(Long id) throws ResourceNotFoundException
    {
        return this.bookService.getBookById(id);
    }


    //------------------------------------------------------------------------------------------------------------------
    // createBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_CREATE_BOOK, subsystem = Constants.SUBSYSTEM_NAME)
    public ResponseUri createBook(BookParams bookParams)
    {
        long newUserId = this.bookService.createBook(bookParams);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newUserId).toUri();
        return new ResponseUri().location(location.toString());
    }


    //------------------------------------------------------------------------------------------------------------------
    // updateBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_UPDATE_BOOK, subsystem = Constants.SUBSYSTEM_NAME)
    public void updateBook(Long id, BookParams bookParams) throws ResourceNotFoundException
    {
        this.bookService.updateBook(id, bookParams);
    }


    //------------------------------------------------------------------------------------------------------------------
    // deleteBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_DELETE_BOOK, subsystem = Constants.SUBSYSTEM_NAME)
    public void deleteBook(Long id) throws ResourceNotFoundException
    {
        this.bookService.deleteBook(id);
    }
}
