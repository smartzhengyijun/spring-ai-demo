package com.cachezheng.ai.springaidemo.controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.FileOutputStream;
import java.util.List;

@RestController
@RequestMapping("/ai")
class AIController {

    @Autowired
    ChatModel openAiChatModel;

    @Autowired
    ImageModel openAiImageModel;

    @Autowired
    OpenAiAudioSpeechModel openAiAudioSpeechModel;

    @Autowired
    OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    /**
     * 基础chat
     * @param message
     * @return
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    String chat(@RequestParam(value = "message") String message) {
        return this.openAiChatModel.call(new Prompt(List.of(new SystemMessage("你是特朗普，请以他的口吻回答问题"),new UserMessage(message))
        )).getResult().getOutput().getText();
    }

    /**
     * 流式响应
     * @param message
     * @return
     */
    @RequestMapping(value = "/stream", produces = "text/html;charset=UTF-8")
    Flux<String> stream(@RequestParam(value = "message") String message) {
        Flux<ChatResponse> flux = this.openAiChatModel.stream(new Prompt(List.of(new SystemMessage("你是特朗普，请以他的口吻回答问题"), new UserMessage(message))
        ));
        return flux.map(chatResponse -> chatResponse.getResult().getOutput().getText());
    }

    /**
     * 文生图
     * @param message
     * @return
     */
    @RequestMapping(value = "/text2image", produces = "text/html;charset=UTF-8")
    String text2image(@RequestParam(value = "message") String message) {
        String result = this.openAiImageModel.call(new ImagePrompt(message,
                OpenAiImageOptions.builder()
                        .model(OpenAiImageApi.DEFAULT_IMAGE_MODEL)
                        .build()
        )).getResult().getOutput().getUrl();
        //此处可添加图片下载逻辑，讲图片存储到对应的路径下
        return result;
    }

    /**
     * 文生语音
     * @param message
     * @return
     */
    @RequestMapping(value = "/text2audio", produces = "text/html;charset=UTF-8")
    public String text2audio(@RequestParam(value = "message") String message) {
        SpeechResponse response = openAiAudioSpeechModel.call(new SpeechPrompt(message,
                OpenAiAudioSpeechOptions.builder().model("tts-1")
                        .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
                        .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                        .speed(1.0f)
                        .build()));
        byte[] bytes = response.getResult().getOutput();
        //改成自己的路径即可
        writeBytesToFile(bytes, "");
        return "SUCCESS";
    }

    /**
     * 语音转文本 可思考结合上述文生语音如何实现语音聊天？？？
     * @param filePath
     * @return
     */
    @RequestMapping(value = "/audio2text", produces = "text/html;charset=UTF-8")
    public String audio2text(@RequestParam(value = "filePath") String filePath) {
        filePath = "/test.mp3";
        AudioTranscriptionResponse response = this.openAiAudioTranscriptionModel.call(
                new AudioTranscriptionPrompt(new ClassPathResource(filePath),
                        OpenAiAudioTranscriptionOptions.builder().model("whisper-1")
                                .language("zh")
                                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                                .temperature(0f)
                                .build()));
        return response.getResult().getOutput();

    }

    /**
     * 多模态 文本+图片
     * @param message
     * @return 文本内容
     */
    @RequestMapping(value = "/multimodal/textWithImage", produces = "text/html;charset=UTF-8")
    public String textWithImage(@RequestParam(value = "message") String message) {

        //视觉 图片输入
        Resource imageResource = new ClassPathResource("/test.png");
        UserMessage imageMessage = new UserMessage(message,
                new Media(MimeTypeUtils.IMAGE_PNG, imageResource));
        ChatResponse imageResponse = openAiChatModel.call(new Prompt(imageMessage,
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O.getValue()).build()));
        return imageResponse.getResult().getOutput().getText();
    }

    /**
     * 多模态 文本+音频
     * @param message
     * @return 文本内容
     */
    @RequestMapping(value = "/multimodal/textWithAudio", produces = "text/html;charset=UTF-8")
    public String textWithAudio(@RequestParam(value = "message") String message) {

        //音频 音频输入
        Resource audioResource = new ClassPathResource("/test.mp3");
        var audioMessage = new UserMessage(message,
                List.of(new Media(MimeTypeUtils.parseMimeType("audio/mp3"), audioResource)));
        ChatResponse audioResponse = openAiChatModel.call(new Prompt(List.of(audioMessage),
                OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW).build()));

        return audioResponse.getResult().getOutput().getText();
    }


    /**
     * 输入文本，输出文本及音频
     * @param message
     * @return
     */
    @RequestMapping(value = "/multimodal/textWithOutputAudio", produces = "text/html;charset=UTF-8")
    public String textWithOutputAudio(@RequestParam(value = "message") String message) {

        var userMessage = new UserMessage("Tell me joke about Spring Framework");
        ChatResponse response = openAiChatModel.call(new Prompt(List.of(userMessage),
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW)
                        .outputModalities(List.of("text", "audio"))
                        .outputAudio(new OpenAiApi.ChatCompletionRequest.AudioParameters(OpenAiApi.ChatCompletionRequest.AudioParameters.Voice.ALLOY, OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat.WAV))
                        .build()));
        String text = response.getResult().getOutput().getText(); // audio transcript
        byte[] waveAudio = response.getResult().getOutput().getMedia().get(0).getDataAsByteArray();

        return "";
    }

    public static void writeBytesToFile(byte[] bytes, String filePath) {
        try {
            FileOutputStream fos = new FileOutputStream(filePath + "/test.mp3");
            fos.write(bytes);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
