package com.wei;

import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LangchainDemoApplicationTests {

    @Test
    void contextLoads() {
        OpenAiChatModel model= OpenAiChatModel.builder()
                .baseUrl("http://langchain4j.dev/demo/openai/v1")
                .apiKey("demo")
                .modelName("gpt-4o-mini")
                .build();
        String chat = model.chat("你好，请告诉我今天的热点新闻");
        System.out.println(chat);
    }

    @Test
    void test2() {
        OpenAiChatModel model= OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .apiKey("sk-6455ad9637a64d86b3ac1152721ade9c")
                .modelName("deepseek-chat")
                .build();
        String chat = model.chat("你好，请告诉我今天的热点新闻");
        System.out.println(chat);
    }

    @Test
    void test3() {
        WanxImageModel model1= WanxImageModel.builder()
                .modelName("wanx2.1-t2i-plus")
                .apiKey("sk-8a3a4e8c3eb74300bb07e1bda84815a4")
                .build();
        Response<Image> response=model1.generate("美女");
        System.out.println(response);
        System.out.println(response.content().url());
    }

}
