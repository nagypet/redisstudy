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

package hu.perit.redisstudy.db.postgres.table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

/**
 * @author Peter Nagy
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "book", schema = "bookstore")
public class BookEntity extends BaseEntity
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id", nullable = false, columnDefinition = "bigserial")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "pages")
    private Integer pages;

    @Column(name = "date_issued")
    private LocalDate dateIssued;

    // The owning side of the many-to-many relationship
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "bookxauthor", schema = "bookstore", // 
            joinColumns = {@JoinColumn(name = "book_id")}, //
            inverseJoinColumns = {@JoinColumn(name = "author_id")} //
    )
    // Important to have a Set here! Hibernate will generate a composite primary key only when using a Set.
    private Set<AuthorEntity> authorEntities;
}
