package com.generator.email_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.generator.email_generator.model.EmailRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class EmailGeneratorService {

    private final WebClient webClient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailGeneratorService(WebClient.Builder webClientBuilder){
        this.webClient=webClientBuilder.build();
    }

    public String generateEmailReply(EmailRequest emailRequest){
        //Build prompt
        String prompt=buildPrompt(emailRequest);

        //Craft a request
        Map<String,Object> requestBody=Map.of(
                "contents",new Object[]{
                      Map.of(  "parts",new Object[]{
                               Map.of( "text",prompt)
                })
                }
        );

        //Do request and get response
        String response=webClient.post()
                .uri(geminiApiUrl+geminiApiKey)
                .header("Content-Type","application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        //Extract Response and return
        return extractResponse(response);
    }

    private String extractResponse(String response) {
        try{
            ObjectMapper mapper=new ObjectMapper();
            JsonNode jsonNode=mapper.readTree(response);
            return jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        }catch(Exception e){
            return "Error processing request"+e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("Generate a Professional Email reply for the following content. Please don't generate subject line ");

        if(emailRequest.getTone()!=null && emailRequest.getTone().isEmpty()){
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone");
        }

        prompt.append("\nOriginal email: \n").append(emailRequest.getEmailContent());
        return prompt.toString();
    }
}
