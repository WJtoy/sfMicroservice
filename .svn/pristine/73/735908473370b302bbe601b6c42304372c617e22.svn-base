package com.kkl.kklplus.b2b.sf.mapper;

import com.github.pagehelper.Page;
import com.kkl.kklplus.b2b.sf.entity.OrderInfo;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderSearchModel;
import com.kkl.kklplus.entity.b2bcenter.sd.B2BOrderTransferResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 工单操作
 * @author cxj
 */
@Mapper
public interface OrderInfoMapper {

    /**
     * 分页查询工单
     * @param orderSearchModel
     * @return
     */
    Page<OrderInfo> getList(B2BOrderSearchModel orderSearchModel);

    /**
     * 转单更新
     * @param orderInfo
     * @return
     */
    Integer updateTransferResult(OrderInfo orderInfo);

    /**
     * 保存工单
     * @param newOrderInfo
     * @return
     */
    Integer insert(OrderInfo newOrderInfo);

    /**
     * 根据查询编码获取信息
     * @param taskCode
     * @return
     */
    OrderInfo findOrderByTaskCode(@Param("taskCode")String taskCode);

    /**
     * 手动取消工单
     * @param transferResult
     * @return
     */
    Integer cancelledOrder(B2BOrderTransferResult transferResult);

    /**
     * 根据ID获取工单对应的处理状态
     * @param ids
     * @return
     */
    List<OrderInfo> findOrdersProcessFlagByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ID获取对应工单号
     * @param id
     * @return
     */
    String findOrderNoById(@Param("id")Long id);

    /**
     * B2B主动取消工单
     * @param remark
     * @param id
     * @param processFlag
     * @param updateDt
     * @return
     */
    Integer cancelOrderFormB2B(@Param("processComment") String remark,
                            @Param("id") Long id,
                            @Param("processFlag") int processFlag,
                            @Param("updateDt") Long updateDt);

    Integer getSaleChannel(@Param("orderSource") String orderSource);

    /**
     *
     * @param orderId
     * @return
     */
    OrderInfo findOrderById(Long orderId);

    List<OrderInfo> getNoConversionAndSystemIdOrder();

    void updateOrderSystemId(@Param("id") Long id, @Param("systemId") Integer systemId);
}
