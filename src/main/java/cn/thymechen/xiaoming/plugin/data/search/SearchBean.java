package cn.thymechen.xiaoming.plugin.data.search;

import lombok.Data;

@Data
public class SearchBean {
    private boolean error;
    private String message;
    private SearchBody body;
}
