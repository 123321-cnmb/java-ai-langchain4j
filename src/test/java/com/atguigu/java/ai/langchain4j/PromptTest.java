package com.atguigu.java.ai.langchain4j;

import com.atguigu.java.ai.langchain4j.assistant.SeparateChatAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PromptTest {
@Autowired
private SeparateChatAssistant separateChatAssistant;
@Test
public void testSystemMessage() {
    String answer = separateChatAssistant.chat(5,"你吃了吗");
    System.out.println(answer);
}

    @Test
    public void testUserInfo() {
        String answer = separateChatAssistant.chat3(6, "我是谁，我多大了", "翠花", 18);
        System.out.println(answer);
    }
}