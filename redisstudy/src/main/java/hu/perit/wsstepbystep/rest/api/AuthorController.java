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

package hu.perit.wsstepbystep.rest.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import hu.perit.wsstepbystep.rest.model.AuthorWithBooksDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import hu.perit.spvitamin.core.took.Took;
import hu.perit.spvitamin.spring.logging.AbstractInterfaceLogger;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import hu.perit.wsstepbystep.businesslogic.api.BookstoreService;
import hu.perit.wsstepbystep.config.Constants;

@RestController
public class AuthorController extends AbstractInterfaceLogger implements AuthorApi
{

    private final AuthorizationService authorizationService;
    private final BookstoreService bookstoreService;

    protected AuthorController(HttpServletRequest httpRequest, AuthorizationService authorizationService, BookstoreService bookstoreService)
    {
        super(httpRequest);
        this.authorizationService = authorizationService;
        this.bookstoreService = bookstoreService;
    }


    //------------------------------------------------------------------------------------------------------------------
    // getAllAuthors()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public List<AuthorWithBooksDTO> getAllAuthors()
    {
        UserDetails user = this.authorizationService.getAuthenticatedUser();
        try (Took took = new Took())
        {
            this.traceIn(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_ALL_AUTHORS, "");

            return this.bookstoreService.getAllAuthors();
        }
        catch (Error | RuntimeException ex)
        {
            this.traceOut(null, user.getUsername(), getMyMethodName(), Constants.EVENT_ID_GET_ALL_AUTHORS, ex);
            throw ex;
        }
    }

    @Override
    protected String getSubsystemName()
    {
        return Constants.SUBSYSTEM_NAME;
    }

}
