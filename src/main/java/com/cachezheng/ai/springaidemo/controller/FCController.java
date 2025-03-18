package com.cachezheng.ai.springaidemo.controller;

import com.cachezheng.ai.springaidemo.dto.Person;
import com.cachezheng.ai.springaidemo.tool.DateTimeTools;
import com.cachezheng.ai.springaidemo.tool.WeatherTools;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/fc")
public class FCController {

    @Autowired
    ChatModel openAiChatModel;

    /**
     * 结构化输出
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/structOutput", produces = "text/html;charset=UTF-8")
    public String structOutput(@RequestParam("message") String message) {
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

    /**
     * function calling
     * eg：请问北京现在几点以及天气怎么样？
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/toolCalling", produces = "text/html;charset=UTF-8")
    public String toolCalling(@RequestParam("message") String message) {

        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools(), new WeatherTools());
        Prompt prompt = new Prompt(message,
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .toolCallbacks(dateTimeTools)
                        .build());
        ChatResponse response = this.openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    /**
     * think -> act
     * @param message
     * @return
     */
    @RequestMapping(value = "/toolCallingByStep", produces = "text/html;charset=UTF-8")
    public String toolCallingByStep(@RequestParam("message") String message) {

        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();
        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTimeTools(), new WeatherTools());
        ChatOptions chatOptions = OpenAiChatOptions.builder()
                .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                .toolCallbacks(dateTimeTools)
                .toolChoice("required")
//                .internalToolExecutionEnabled(false)
                .build();
        Prompt prompt = new Prompt(message, chatOptions);
        ChatResponse response = this.openAiChatModel.call(prompt);
        if (response.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, response);

            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);

            response = this.openAiChatModel.call(prompt);
        }
        String result = response.getResult().getOutput().getText();
        return result;
    }
}
