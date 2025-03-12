package com.cachezheng.ai.springaidemo.controller;


import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ollama")
public class OllamaController {


    @Autowired
    ChatModel ollamaChatModel;

    @RequestMapping(value = "/chat")
    String ollamaChat(@RequestParam(value = "message") String message) {
        return this.ollamaChatModel.call(message);
    }

    @RequestMapping(value = "/stream" ,produces = "text/html;charset=UTF-8")
    Flux<String> ollamaStream(@RequestParam(value = "message") String message) {
        Flux<String> flux = this.ollamaChatModel.stream(message);
        return flux;
    }
}
