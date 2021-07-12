package com.kkl.kklplus.b2b.sf.fallback;

import com.kkl.kklplus.b2b.sf.feign.B2BConfigRoutingFeign;
import com.kkl.kklplus.common.exception.MSErrorCode;
import com.kkl.kklplus.common.response.MSResponse;
import com.kkl.kklplus.entity.b2bconfig.sd.B2BConfigRouting;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class B2BConfigRoutingFeignFallbackFactory implements FallbackFactory<B2BConfigRoutingFeign> {
    @Override
    public B2BConfigRoutingFeign create(Throwable throwable) {
        return new B2BConfigRoutingFeign() {

            @Override
            public MSResponse<Integer> getSystemId(B2BConfigRouting configRouting) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }

            @Override
            public MSResponse<Map<String, Integer>> getSystemIdList(List<B2BConfigRouting> configRoutings) {
                return new MSResponse<>(MSErrorCode.FALLBACK_FAILURE);
            }
        };
    }
}
