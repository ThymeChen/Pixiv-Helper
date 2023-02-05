package cn.thymechen.xiaoming.plugin.data.rank;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Content {
    private String title;
    private String date;
    private List<String> tags;
    private String url;
    @SerializedName("illust_type")
    private String illustType;
    @SerializedName("illust_book_style")
    private String illustBookStyle;
    @SerializedName("illust_page_count")
    private String illustPageCount;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("profile_img")
    private String profileImg;
    @SerializedName("illust_content_type")
    private IllustContentType illustContentType;
    @SerializedName("illust_series")
    private Object illustSeries;
    @SerializedName("illust_id")
    private long illustId;
    private int width;
    private int height;
    @SerializedName("user_id")
    private long userId;
    private int rank;
    @SerializedName("yes_rank")
    private int yesRank;
    @SerializedName("rating_count")
    private int ratingCount;
    @SerializedName("view_count")
    private long viewCount;
    @SerializedName("illust_upload_timestamp")
    private long illustUploadTimestamp;
    private String attr;
    @SerializedName("is_bookmarked")
    private boolean isBookmarked;
    private boolean bookmarkable;

    @Data
    public static class IllustSeries {
        @SerializedName("illust_series_id")
        private String illustSeriesId;
        @SerializedName("illust_series_user_id")
        private String illustSeriesUserId;
        @SerializedName("illust_series_title")
        private String illustSeriesTitle;
        @SerializedName("illust_series_caption")
        private String illustSeriesCaption;
        @SerializedName("illust_series_content_count")
        private String illustSeriesContentCount;
        @SerializedName("illust_series_create_datetime")
        private Date illustSeriesCreateDatetime;
        @SerializedName("illust_series_content_illust_id")
        private String illustSeriesContentIllustId;
        @SerializedName("illust_series_content_order")
        private String illustSeriesContentOrder;
        @SerializedName("page_url")
        private String pageUrl;
    }
    @Data
    public static class IllustContentType {
        private int sexual;
        private boolean lo;
        private boolean grotesque;
        private boolean violent;
        private boolean homosexual;
        private boolean drug;
        private boolean thoughts;
        private boolean antisocial;
        private boolean religion;
        private boolean original;
        private boolean furry;
        private boolean bl;
        private boolean yuri;
    }
}
