package com.atguigu.java.ai.langchain4j.Tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

@Component
public class CalculatorTools {
    @Tool(name = "加法", value = "对两个数字进行加法运算,并返回结果")
    double sum(
            @ToolMemoryId int memoryId,
            @P(value = "加数1", required = true) double a,
            @P(value = "加数2", required = true) double b
    ) {
        System.out.println("调用加法运算 memoryId"+memoryId);
        return a + b;
    }

    @Tool(name = "平方根", value = "对两个数字进行平方根运算,并返回结果")
    double squareRoot(@ToolMemoryId int memoryId,double x) {
        System.out.println("调用平方根运算 memoryId"+memoryId);
        return Math.sqrt(x);
    }
}