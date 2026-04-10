package com.wei.config;

import com.wei.service.ToolService;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class AiConfig {

    public interface Assistant {
        String chatLanguageModel(@MemoryId int memoryId,@UserMessage String msg);
        TokenStream streamLanguageModel(@MemoryId int memoryId,@UserMessage String msg);
        //定义角色
        @SystemMessage("""
        您是“12345”铁路公司的客户聊天支持代理。请以友好、乐于助人且愉快的方式来回复。
        您正在通过在线聊天系统与客户互动。
        在提供有关预订或取消预订的信息之前，您必须始终从用户处获取以下信息:预订号、客户姓名。
        请讲中文。
        今天的日期是 {{current_date}}.
        """)
        TokenStream stream(@UserMessage String userMessage,
                           @V("current_date") String currentDate);
    }
    @Bean
    public EmbeddingStore embeddingStore() {
        return new InMemoryEmbeddingStore();
    }
//    将terms-of-service.txt文件中的文本片段向量化并存储在embeddingStore中
    @Bean
    CommandLineRunner ingestTermOfServiceToVectorStore(QwenEmbeddingModel qwenEmbeddingModel, EmbeddingStore embeddingStore)
            throws URISyntaxException {
        Path documentPath = Paths.get(getClass().getClassLoader()
                .getResource("rag/terms-of-service.txt").toURI());

        return args->{
            TextDocumentParser textDocumentParser = new TextDocumentParser();
            Document document = FileSystemDocumentLoader.loadDocument(documentPath, textDocumentParser);
            DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(200, 50);
            List<TextSegment> segments = splitter.split(document);
            // 4. 向量化所有文本片段
            List<Embedding> embeddings = qwenEmbeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings,segments);
        };

    }

    @Bean
    public Assistant getAssistant(ChatLanguageModel chatLanguageModel, StreamingChatLanguageModel streamLanguageModel, ToolService toolService,
                                  EmbeddingStore embeddingStore, QwenEmbeddingModel qwenEmbeddingModel){
//        ChatMemory chatMemory= MessageWindowChatMemory.withMaxMessages(10);
        // 内容检索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5) // 最相似的5个结果
                .minScore(0.6) // 只找相似度在0.6以上的内容
                .build();
        Assistant assistant= AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .streamingChatLanguageModel(streamLanguageModel)
                .tools(toolService)
                .contentRetriever(contentRetriever)
                .chatMemoryProvider(memoryId->
                        MessageWindowChatMemory.builder().maxMessages(10) // 每个对话最多保存10条消息
                                .id(memoryId)  // 设置记忆ID（区分不同会话）
                                .build())
//                .chatMemory(chatMemory)
                .build();
        return assistant;
    }
}
