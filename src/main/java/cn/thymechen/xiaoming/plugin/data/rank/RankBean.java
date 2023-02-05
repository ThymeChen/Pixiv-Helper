package cn.thymechen.xiaoming.plugin.data.rank;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class RankBean {
    private List<Content> contents;
    private String mode;
    private String content;
    private int page;
    private Object prev;
    private Object next;
    private String date;
    @SerializedName("prev_date")
    private String prevDate;
    @SerializedName("next_date")
    private boolean nextDate;
    @SerializedName("rank_total")
    private int rankTotal;
}
