package com.cachezheng.ai.springaidemo.controller;

import com.cachezheng.ai.springaidemo.dto.Person;
import com.cachezheng.ai.springaidemo.tool.DateTimeTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fc")
public class FCController {

    @Autowired
    ChatModel openAiChatModel;

    /**
     * 结构化输出
     * @param message
     * @return
     */
    @RequestMapping(value = "/structOutput", produces = "text/html;charset=UTF-8")
    public String structOutput (@RequestParam("message") String message) {
        BeanOutputConverter<Person> outputConverter = new BeanOutputConverter<>(Person.class);
        String jsonSchema = outputConverter.getJsonSchema();
        Prompt prompt = new Prompt(message,
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
                        .build());

        ChatResponse response = this.openAiChatModel.call(prompt);
        String text = response.getResult().getOutput().getText();

        Person person = outputConverter.convert(text);
        return person.toString();
    }

    @RequestMapping(value = "/toolCalling", produces = "text/html;charset=UTF-8")
    public String toolCalling (@RequestParam("message") String message) {

        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools());
        Prompt prompt = new Prompt(message,
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .toolCallbacks(dateTimeTools)
                        .build());
        ChatResponse response = this.openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
