package com.atguigu.java.ai.langchain4j;

import com.atguigu.java.ai.langchain4j.assistant.MemoryChatAssistant;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.WanxImageModel;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LLMTest {
    /**
     * gpt-4o-mini语言模型接入测试
     */
    @Test
    public void testGPTDemo() {
        OpenAiChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .apiKey("sk-25f2d2288d6e4996ab151acc6ccafd13")
                .modelName("deepseek-chat") //设置模型名称
                .build();
        String answer = model.chat("你好");
        System.out.println(answer);
    }

    @Test
    public void testSpringBoot() {
        // 临时创建DeepSeek模型实例
        OpenAiChatModel deepSeekModel = OpenAiChatModel.builder()
                .baseUrl("https://api.deepseek.com/v1")
                .apiKey("sk-25f2d2288d6e4996ab151acc6ccafd13")
                .modelName("deepseek-chat")
                .build();

        String answer = deepSeekModel.chat("你好");
        System.out.println("DeepSeek回复: " + answer);
    }


    @Autowired
    private OpenAiChatModel openAiChatModel;
    @Test
    public void testSpringBoot2() {
        String answer = openAiChatModel.chat("你好");
        System.out.println(answer);
    }

    @Autowired
    private QwenChatModel qwenChatModel;
    @Test
    public void testDashScopeQwen() {

        String answer = qwenChatModel.chat("你好");

        System.out.println(answer);
    }

    @Test
    public void testDashScopeWanx(){
        WanxImageModel wanxImageModel = WanxImageModel.builder()
                .modelName("wanx2.1-t2i-plus")
                .apiKey(System.getenv("DASH_SCOPE_API_KEY"))
                .build();
        Response<Image> response = wanxImageModel.generate("奇幻森林精灵：在一片弥漫着轻柔薄雾的古老森林深处，阳光透过茂密枝叶洒下金色光斑。一位身材娇小、长着透明薄翼的精灵少女站在一朵硕大的蘑菇上。她 有着海藻般的绿色长发，发间点缀着蓝色的小花，皮肤泛着珍珠般的微光。身上穿着由翠绿树叶和白色藤蔓编织而成的 连衣裙，手中捧着一颗散发着柔和光芒的水晶球，周围环绕着五彩斑斓的蝴蝶，脚下是铺满苔藓的地面，蘑菇和蕨类植 物丛生，营造出神秘而梦幻的氛围。");
                System.out.println(response.content().url());
    }

    @Autowired
    private MemoryChatAssistant memoryChatAssistant;
    @Test
    public void testUserMessage() {
        String answer = memoryChatAssistant.chat("我是环环");
        System.out.println(answer);
    }

}

