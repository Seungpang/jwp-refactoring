package kitchenpos.order.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

public class Order {
    private Long id;
    private Long orderTableId;
    private String orderStatus;
    private LocalDateTime orderedTime;
    private List<OrderLineItem> orderLineItems;

    private Order(final Long id, final Long orderTableId, final String orderStatus, final LocalDateTime orderedTime,
                 final List<OrderLineItem> orderLineItems) {
        this.id = id;
        this.orderTableId = orderTableId;
        this.orderStatus = orderStatus;
        this.orderedTime = orderedTime;
        this.orderLineItems = orderLineItems;
    }

    public static Order of(final Long id, final Long orderTableId, final String orderStatus, final LocalDateTime orderedTime,
                           final List<OrderLineItem> orderLineItems) {
        validateOrderLineItems(orderLineItems);
        return new Order(id, orderTableId, orderStatus, orderedTime, orderLineItems);
    }

    private static void validateOrderLineItems(final List<OrderLineItem> orderLineItems) {
        if (CollectionUtils.isEmpty(orderLineItems)) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }
    }

    public static Order of(final Long orderTableId, final String orderStatus, final LocalDateTime orderedTime,
                           final List<OrderLineItem> orderLineItems) {
        return of(null, orderTableId, orderStatus, orderedTime, orderLineItems);
    }

    public static Order of(final Long orderTableId, final List<OrderLineItem> orderLineItems) {
        return of(null, orderTableId, OrderStatus.COOKING.name(), LocalDateTime.now(), orderLineItems);
    }

    public static Order toEntity(final Long id, final Long orderTableId, final String orderStatus, final LocalDateTime orderedTime) {
        return new Order(id, orderTableId, orderStatus, orderedTime, null);
    }

    public void checkActualOrderLineItems(final Long size) {
        if (orderLineItems.size() != size) {
            throw new IllegalArgumentException("실제 메뉴와 일치하지 않는 메뉴가 존재합니다!");
        }
    }

    public List<Long> getMenuIds() {
        return orderLineItems.stream()
                .map(OrderLineItem::getMenuId)
                .collect(Collectors.toList());
    }

    public void changeOrderStatus(final OrderStatus orderStatus) {
        if (Objects.equals(OrderStatus.COMPLETION.name(), this.orderStatus)) {
            throw new IllegalArgumentException("주문의 상태가 계산 완료일 때 상태를 변경할 수 없습니다.");
        }
        this.orderStatus = orderStatus.name();
    }

    public boolean unableUngroup() {
        return orderStatus.equals(OrderStatus.COOKING) || orderStatus.equals(OrderStatus.MEAL);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderTableId() {
        return orderTableId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public LocalDateTime getOrderedTime() {
        return orderedTime;
    }

    public List<OrderLineItem> getOrderLineItems() {
        return orderLineItems;
    }
}