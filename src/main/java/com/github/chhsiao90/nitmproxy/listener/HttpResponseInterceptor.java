package com.github.chhsiao90.nitmproxy.listener;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.github.chhsiao90.nitmproxy.ConnectionContext;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpObject;

import java.util.List;


public class HttpResponseInterceptor implements NitmProxyListener {

    @Override
    public List<HttpObject> onHttp1Response(ConnectionContext connectionContext, HttpObject data) {
        if ("szhzjkm.hangzhou.gov.cn:9090".equalsIgnoreCase(connectionContext.getServerAddr().toString())) {
            if (data instanceof DefaultHttpContent) {
                DefaultHttpContent defaultHttpContent = (DefaultHttpContent) data;
                String packetStr = defaultHttpContent.content().toString(UTF_8);
                if (packetStr.contains("\"newPcrStatus\"")) {
                    packetStr = packetStr.replaceAll("\"newPcrStatus\":\"\\d+?\"", "\"newPcrStatus\":\"2\"");
                    return ImmutableList.of(new DefaultHttpContent(Unpooled.wrappedBuffer(packetStr.getBytes(UTF_8))));
                }
            }
            return ImmutableList.of(data);
        } else {
            return ImmutableList.of(data);
        }

    }
}