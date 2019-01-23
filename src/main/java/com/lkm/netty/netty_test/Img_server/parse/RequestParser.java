package com.lkm.netty.netty_test.Img_server.parse;

import com.lkm.netty.netty_test.Img_server.exception.MethodNotSupportedException;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ^^^^^^^^^^^^^^^^^^^^|^^^^^^^^^^^^^^^^^^^^^
 * ^^^^^^^^^^^^^^^|    |    | ^^^^^^^^^^^^^^^人以群分
 * ^^^^^^^^^^^^^)_)  )_)  )_)^^^^^^^^^^^^^和为组织
 * ^^^^^^^^^^^^)___))___))___)/^^^^^^^^^聚于思想
 * ^^^^^^^^^^^)____)____)_____)//^^^^^获于感悟
 * ^^^^^^^^^_____|____|____|____///__
 * ---------/                   /---------
 * ^^^^^ ^^^^^ ^^^^  ^^^^^^^  ^^^^^    ^^^
 * 独学而无友 则孤陋而寡闻
 *
 * @Auto lkm
 * @Date 2019/1/15 16:31
 * @Tip HTTP请求参数解析
 * @Version 1.0
 **/
public class RequestParser {
    private static RequestParser parser;
    private static HttpObject httpObject;

    private RequestParser(){}

    public static RequestParser newInstances(){
        if (parser==null){
            synchronized (RequestParser.class){
                if (parser==null){
                    parser = new RequestParser();
                }
            }
        }
        return parser;
    }

    /**
     * 构造一个解析器
     *
     * @param httpObject
     */
    public RequestParser set(HttpObject httpObject) {
        parser.httpObject = httpObject;
        return parser;
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws IOException
     */
    public Map<String, String> parseGet() throws MethodNotSupportedException, IOException {
        Map<String, String> parmMap = new HashMap<>();
        HttpRequest request = (HttpRequest) httpObject;
        // 是GET请求
        QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
        decoder.parameters().entrySet().forEach(entry -> {
            // entry.getValue()是一个List, 只取第一个元素
            parmMap.put(entry.getKey(), entry.getValue().get(0));
        });
        return parmMap;
    }

    /**
     * 解析请求参数
     *
     * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
     * @throws IOException
     */
    public Map<String, String> parsePost(HttpPostRequestDecoder decoder) throws MethodNotSupportedException, IOException {
        Map<String, String> parmMap = new HashMap<>();
/*        HttpContent httpContent = (HttpContent) httpObject;
        decoder.offer(httpContent);*/
        List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
        for (InterfaceHttpData parm : parmList) {
            if (!"Attribute".equals(parm.getHttpDataType().toString()))continue;
            Attribute data = (Attribute) parm;
            parmMap.put(data.getName(), data.getValue());
        }
        return parmMap;
    }
}
