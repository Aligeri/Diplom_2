package yandex.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderList {
    private List<String> ingredients;
    private String _id;
    private String status;
    private String name;
    private int number;
    private String createdAt;
    private String updatedAt;
}