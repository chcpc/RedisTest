package org.example.redisproject.controller;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.redisproject.entity.W3UserInfo;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.api.map.MapLoader;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.MarshallingCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private RedissonClient redissonClient;

    @GetMapping
    public Object get() throws InterruptedException {
        LocalCachedMapOptions<String, W3UserInfo> options = LocalCachedMapOptions.<String, W3UserInfo>defaults()
                .cacheProvider(LocalCachedMapOptions.CacheProvider.CAFFEINE)
                .storeMode(LocalCachedMapOptions.StoreMode.LOCALCACHE_REDIS)
                .maxIdle(10, TimeUnit.SECONDS)
                .loader(new MapLoader<String, W3UserInfo>() {
                    @Override
                    public W3UserInfo load(String key) {
                        W3UserInfo w3UserInfo = new W3UserInfo();
                        w3UserInfo.setUuid("ZZ");
                        return w3UserInfo;
                    }

                    @Override
                    public Iterable<String> loadAllKeys() {
                        return null;
                    }
                })
                .cacheSize(1000);
        RLocalCachedMap<String, W3UserInfo> localCachedMap =
                redissonClient.getLocalCachedMap("testzz",
                        new CompositeCodec(StringCodec.INSTANCE,new JsonJacksonCodec(), new JsonJacksonCodec()), options);
        Map<String, W3UserInfo> cacheMap = new HashMap<>(100);
        List<W3UserInfo> w3UserInfos = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            W3UserInfo w3UserInfo = new W3UserInfo();
            w3UserInfo.setUuid("uuid~" + i);
            w3UserInfo.setEmail(i + "@huawei.com~");
            w3UserInfo.setUid("00000000" + i);
            w3UserInfo.setUcn("wxdwjaodj 00000000" + i);
            w3UserInfos.add(w3UserInfo);
            cacheMap.put("uuid~" + i, w3UserInfo);
//            cacheMap.put("uuid~" + i, "uuid~" + i);
        }
        String s = JSON.toJSONString(w3UserInfos);
        System.out.println(s);
        System.out.println(s.getBytes().length);
        localCachedMap.putAll(cacheMap);
//        RLock rLock = redissonClient.getLock("key");
//        rLock.lock();
//        Thread.sleep(10000);
//        rLock.unlock();
        localCachedMap.get("SSD");
        System.out.println(System.currentTimeMillis());
        Map<String, W3UserInfo> all = localCachedMap.getAll(cacheMap.keySet());
        System.out.println(System.currentTimeMillis());
        return all;
    }

    public static void main(String[] args) {
        System.out.println("uuid~wx2SadAwdH==".getBytes().length);
        System.out.println("qiubinyu 30034565".getBytes().length);
    }
}
