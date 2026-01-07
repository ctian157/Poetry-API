package com.example.Poetry_API.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.Poetry_API.model.Poem;

import java.util.List;
import java.util.Optional;

//this interface allows method inheritance from JpaRepository like save(), findById() etc.
@Repository
public interface PoemRepository extends JpaRepository<Poem, Integer> {

    //all CRUD methods from DataAccessService are already included in JpaRepository

    //additional custom methods:

    //For getting poems of a given language
    List<Poem> findByLanguage(String language);
    //generates 'SELECT * FROM poem WHERE language = ?'

    //For adding poems
    boolean existsByTitleAndContent(String title, String content);
    //generates 'SELECT COUNT(*) > 0 FROM poem WHERE title = ? AND content = ?'

    //For updating poems
    boolean existsByTitleAndContentAndIdNot(String title, String content, int id);
    //generates 'SELECT COUNT(*) > 0 FROM poem WHERE title = ? AND content = ? AND id<>?'

    //for health check; keep Supabase DB alive
    Optional<Poem> findFirstBy();
    //generates SELECT * FROM poems LIMIT 1;
}