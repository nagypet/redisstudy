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

import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.exception.ResourceNotFoundException;
import hu.perit.spvitamin.spring.logging.AbstractInterfaceLogger;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import hu.perit.redisstudy.service.api.BookService;
import hu.perit.redisstudy.config.Constants;
import hu.perit.redisstudy.rest.model.BookDTO;
import hu.perit.redisstudy.rest.model.BookParams;
import hu.perit.redisstudy.rest.model.ResponseUri;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
public class BookController extends AbstractInterfaceLogger implements BookApi
{
    private final AuthorizationService authorizationService;
    private final BookService bookService;

    protected BookController(HttpServletRequest httpRequest, AuthorizationService authorizationService, BookService bookService)
    {
        super(httpRequest);
        this.authorizationService = authorizationService;
        this.bookService = bookService;
    }


    //------------------------------------------------------------------------------------------------------------------
    // getAllBooks()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public List<BookDTO> getAllBooks()
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_ALL_BOOKS, "");

            return this.bookService.getAllBooks();
        }
        catch (Exception ex)
        {
            traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_ALL_BOOKS, ex);
            throw ex;
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // getBookById
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public BookDTO getBookById(Long id) throws ResourceNotFoundException
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_BOOK_BY_ID, String.format("id: %d", id));

            return this.bookService.getBookById(id);
        }
        catch (Error | RuntimeException | ResourceNotFoundException ex)
        {
            traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_BOOK_BY_ID, ex);
            throw ex;
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // createBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public ResponseUri createBook(BookParams bookParams)
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_CREATE_BOOK, bookParams.toString());

            long newUserId = this.bookService.createBook(bookParams);

            URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(newUserId).toUri();

            return new ResponseUri().location(location.toString());
        }
        catch (Error | RuntimeException ex)
        {
            traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_CREATE_BOOK, ex);
            throw ex;
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // updateBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void updateBook(Long id, BookParams bookParams) throws ResourceNotFoundException
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_UPDATE_BOOK,
                    String.format("id: %d, bookParams: %s", id, bookParams.toString()));

            this.bookService.updateBook(id, bookParams);
        }
        catch (Error | RuntimeException | ResourceNotFoundException ex)
        {
            traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_UPDATE_BOOK, ex);
            throw ex;
        }
    }


    //------------------------------------------------------------------------------------------------------------------
    // deleteBook
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public void deleteBook(Long id) throws ResourceNotFoundException
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_DELETE_BOOK, String.format("id: %d", id));

            this.bookService.deleteBook(id);
        }
        catch (Error | RuntimeException | ResourceNotFoundException ex)
        {
            traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_DELETE_BOOK, ex);
            throw ex;
        }
    }


    @Override
    protected String getSubsystemName()
    {
        return Constants.SUBSYSTEM_NAME;
    }
}
