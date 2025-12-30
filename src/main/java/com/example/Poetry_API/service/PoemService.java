package com.example.Poetry_API.service;

import com.example.Poetry_API.dao.DataAccessService;
import com.example.Poetry_API.model.Poem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;


import java.util.List;
import java.util.Map;
import java.util.Optional;

//handles application logic, calls DAO
@Service
public class PoemService {

    private final DataAccessService dataAccessService ;

    //constructor
    @Autowired //so you don't have to manually create a new dataAccessService class instance (use bean instead)
    public PoemService(DataAccessService dataAccessService) {
        this.dataAccessService = dataAccessService;
    }

    //method
    public Poem addPoem (Poem poem) {

        //if for some reason URL does not pass in a language, set default
        if (poem.getLanguage() == null || poem.getLanguage().isBlank()) {
            poem.setLanguage("en");
        }

        //insertPoem already returns an int
        return dataAccessService.insertPoem(poem);
    }

    public List<Poem> getAllPoems (){
        return dataAccessService.selectAllPoems();
    }

    public List<Poem> getPoemByLanguage(String language) {
        return dataAccessService.getPoemByLanguage(language);
    }

    public Optional<Poem> getPoemById(int id) {
        return dataAccessService.selectPoemById(id);
    }

    public int deletePoemById (int id) {
        return dataAccessService.deletePoemById(id);
    }

    public Poem updatePoemById(int id, Poem poem) {
        return dataAccessService.updatePoemById(id, poem);
    }


    //reuse the restTemplate connection
    private final RestTemplate restTemplate = new RestTemplate();
    //define apiKey
    @Value("${apiKey}")
    private String apiKey;


    private String callTranslationAPI(String content){
        //Calling Gemini API here
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;

        //immutable map
        Map<String, Object> prompt = Map.of(
           "contents", List.of(
                   Map.of("parts", List.of(
                           Map.of("text", "Translate the following Chinese poem into English." +
                                   "Do not translate word for word. Instead, preserve the poetic rhythm, imagery, and emotional nuances." +
                                   "The translation should read like an English poem, keeping its lyrical quality and flow." +
                                   "Do not include the title, author, or any commentary. \nPoem:" +
                                   content)
                           )
                   )
            )
        );

        //create the http request 'entity' by attaching headers to prompt
        HttpHeaders headers = new HttpHeaders();
        //turn Java Map into JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        //package prompt and headers to send to API
        //note we don't need to specify type for headers because HttpHeaders is a concrete class but prompt is not
        HttpEntity<Map<String, Object>> entity = new HttpEntity<Map<String, Object>>(prompt, headers);


        //send http request and deserialize response as Java Map (raw type)
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        return extractTranslation(response);
    }


    //note the typecasting from raw Object into desired type
    private String extractTranslation(ResponseEntity<Map> response){
        Map<String, Object> body = response.getBody();
        if (body == null) return null;

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) body.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null || content.isEmpty()) return null;

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) return null;

        String text = (String) parts.get(0).get("text");

        return text;

    }

    public Poem translatePoem(int id) {

        Optional<Poem> poemToTranslate = dataAccessService.selectPoemById(id);

        if (poemToTranslate.isEmpty()) {
            return null;
        }

        //return Poem if Optional exists
        Poem selectedPoem = poemToTranslate.get();

        // Only call translation API if translation is missing
        if (selectedPoem.getTranslation() == null || selectedPoem.getTranslation().isEmpty()) {
            String translatedText = callTranslationAPI(selectedPoem.getContent());
            selectedPoem.setTranslation(translatedText);
            dataAccessService.updatePoemById(id, selectedPoem);
        }

        return selectedPoem;
    }


    public boolean isDBAlive() {
        return dataAccessService.isDBAlive();
    }
}
