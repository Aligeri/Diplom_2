package yandex.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserOrdersResponse {
    private boolean success;
    private List<OrderList> orders;
    private int total;
    private int totalToday;
}