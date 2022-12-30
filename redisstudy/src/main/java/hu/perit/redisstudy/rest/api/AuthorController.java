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
import hu.perit.redisstudy.rest.model.AuthorWithBooksDTO;
import hu.perit.redisstudy.service.api.AuthorService;
import hu.perit.spvitamin.spring.restmethodlogger.LoggedRestMethod;
import hu.perit.spvitamin.spring.security.auth.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorController implements AuthorApi
{
    private final AuthorizationService authorizationService;
    private final AuthorService authorService;


    //------------------------------------------------------------------------------------------------------------------
    // getAllAuthors()
    //------------------------------------------------------------------------------------------------------------------
    @Override
    @LoggedRestMethod(eventId = Constants.EVENT_ID_GET_ALL_AUTHORS, subsystem = Constants.SUBSYSTEM_NAME)
    public List<AuthorWithBooksDTO> getAllAuthors()
    {
        return this.authorService.getAllAuthors();
    }
}
