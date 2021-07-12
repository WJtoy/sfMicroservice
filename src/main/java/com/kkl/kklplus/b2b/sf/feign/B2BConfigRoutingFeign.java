package com.kkl.kklplus.b2b.sf.feign;

import com.kkl.kklplus.b2b.sf.fallback.B2BConfigRoutingFeignFallbackFactory;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bconfig.sd.B2BConfigRouting;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * B2BCenter微服务接口调用
 */
@FeignClient(name = "kklplus-b2b-config", fallbackFactory = B2BConfigRoutingFeignFallbackFactory.class)
public interface B2BConfigRoutingFeign {

    @PostMapping("/b2bConfigRouting/getSystemId")
    MSResponse<Integer> getSystemId(B2BConfigRouting configRouting);

    @PostMapping("/b2bConfigRouting/getSystemIdList")
    MSResponse<Map<String,Integer>> getSystemIdList(@RequestBody List<B2BConfigRouting> configRoutings);
}
