package com.cachezheng.ai.springaidemo.controller;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.ai.transformer.SummaryMetadataEnricher.SummaryType;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
public class RAGController {

    @Autowired
    private ChatModel ollamaChatModel;

    @Autowired
    private ChatModel openAiChatModel;

    @Autowired
    private VectorStore milvusVectorStore;

    @Autowired
    EmbeddingModel ollamaEmbeddingModel;

    @RequestMapping(value = "/add")
    public String add(@RequestParam(value = "filePath") String filePath) {
        TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("test.txt"));
        milvusVectorStore.add(reader.get());
        return "SUCCESS";
    }

    @RequestMapping(value = "/query")
    public String query(@RequestParam(value = "message") String message) {
        List<Document> documents = milvusVectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build());
        String text = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));
        Prompt prompt = new Prompt(List.of(new SystemMessage(text), new UserMessage(message)));
        ChatResponse response = this.openAiChatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    /**
     * 利用大语言模型拆分文档-关键字拆分
     * @param documents
     * @return
     */
    private List<Document> splitDocumentByKeyword(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(this.openAiChatModel, 5);
        return enricher.apply(documents);
    }

    /**
     * 利用大语言模型拆分文档-概要
     * @param documents
     * @return
     */
    private List<Document> splitDocumentBySummary(List<Document> documents) {
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(this.openAiChatModel, List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT));
        return enricher.apply(documents);
    }

    /**
     * 长度拆分
     * @param documents
     * @return
     */
    private List<Document> splitDocumentByTokenText(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter(100,20,5,100,true);
        return splitter.apply(documents);
    }


    @RequestMapping(value = "/split")
    public void testSplit(){
        TikaDocumentReader reader = new TikaDocumentReader(new ClassPathResource("test.txt"));
        List<Document> documents = reader.get();
//        System.out.println(splitDocumentByKeyword(documents));
//        System.out.println(splitDocumentBySummary(documents));
        System.out.println(splitDocumentByTokenText(documents));

    }





}
