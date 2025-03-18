package com.cachezheng.ai.springaidemo.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mcp")
public class MCPController {

    @Autowired
    private ToolCallbackProvider asyncToolCallbackProvider;

    @Autowired
    private ChatModel openAiChatModel;

    @RequestMapping(value = "/test")
    public String test(@RequestParam(value = "message") String message) {
        FunctionCallback[] tools = asyncToolCallbackProvider.getToolCallbacks();
        Prompt prompt = new Prompt(message,
                OpenAiChatOptions.builder()
//                        .model(OpenAiApi.ChatModel.GPT_4_O_MINI)
                        .toolCallbacks(tools)
                        .build());
        ChatResponse response = this.openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }




}
