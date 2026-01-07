package com.example.Poetry_API.dao;

import com.example.Poetry_API.model.Poem;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//DAO talks directly to database
@Repository
public class DataAccessService {

    private final PoemRepository poemRepository;

    //constructor
    public DataAccessService(PoemRepository poemRepository) {
        this.poemRepository = poemRepository;
    }


    //methods
    public Poem insertPoem (Poem poem) {

        //if any mandatory fields are empty, Spring @NotBlank already checks from the start, so
        //that validation is not necessary here

        // Reject duplicates
        if (poemRepository.existsByTitleAndContent(poem.getTitle(), poem.getContent())) {
            System.out.println("Poem already exists!");
            return null;
        }

        //Save the poem
        Poem savedPoem = poemRepository.save(poem);
        return savedPoem; // return newly added poem
    }


    public List<Poem> selectAllPoems() {
        return poemRepository.findAll();
    }


    public List<Poem> getPoemByLanguage(String language) {
        return poemRepository.findByLanguage(language);
    }

    public Optional<Poem> selectPoemById (int id) {
        return poemRepository.findById(id);
    }


    public int deletePoemById (int id) {
        Optional<Poem> result = selectPoemById(id);
        if(result.isEmpty()) {
            return 0;
        }

        poemRepository.deleteById(id);
        return 1;
    }


    public Poem updatePoemById (int id, Poem poem) {
        //find the poem in database
        Optional<Poem> poemToUpdate = poemRepository.findById(id);

        //if poem doesn't exist
        if (poemToUpdate.isEmpty()) {
            return null;
        }

        //if updated title or content is a duplicate of another poem in database
        if (poemRepository.existsByTitleAndContentAndIdNot(poem.getTitle(),poem.getContent(),id)) {
            return null;
        }

        //if any mandatory fields are empty, Spring @NotBlank already checks from the start, so
        //that validation is not necessary here

        //obtain Java object if optional exists
        Poem selectedPoem = poemToUpdate.get();

        //now update the poem
        selectedPoem.setTitle(poem.getTitle());
        selectedPoem.setPoet(poem.getPoet());
        selectedPoem.setPoetEn(poem.getPoetEn());
        selectedPoem.setDynasty(poem.getDynasty());
        selectedPoem.setContent(poem.getContent());
        selectedPoem.setTranslation(poem.getTranslation());

        poemRepository.save(selectedPoem);
        return selectedPoem;
    }

    public boolean isDBAlive() {
        return poemRepository.findFirstBy().isPresent();
    }

}