package com.wei.controller;

import com.wei.config.AiConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    ChatLanguageModel chatLanguageModel;
    @Autowired
    AiConfig.Assistant assistant;

    @RequestMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你是谁") String msg) {
        return chatLanguageModel.chat(msg);
    }

    @RequestMapping(value="/memory-chat",produces = "text/stream;charset=utf-8")
    public Flux<String> memoryChat(@RequestParam(defaultValue = "我是谁") String msg, HttpServletResponse response) {
//        TokenStream stream= assistant.streamLanguageModel(msg);
        // 调用助手服务的流式接口，获取TokenStream对象
        TokenStream stream = assistant.stream(msg, LocalDate.now().toString());
//        内层使用方法链调用
        return Flux.create(sink ->{
            stream.onPartialResponse(s -> sink.next(s))
                    .onCompleteResponse(s -> sink.complete())
                    .onError(e -> sink.error(e))
                    .start();
        });
    }
}
