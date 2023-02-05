package cn.thymechen.xiaoming.plugin.interactors;

import cn.chuanwise.xiaoming.annotation.Filter;
import cn.chuanwise.xiaoming.annotation.FilterParameter;
import cn.chuanwise.xiaoming.annotation.Required;
import cn.chuanwise.xiaoming.interactor.SimpleInteractors;
import cn.chuanwise.xiaoming.user.XiaoMingUser;
import cn.thymechen.xiaoming.plugin.PixivHelper;
import cn.thymechen.xiaoming.plugin.data.rank.Content;
import cn.thymechen.xiaoming.plugin.data.rank.RankBean;
import cn.thymechen.xiaoming.plugin.data.search.ArtworkData;
import cn.thymechen.xiaoming.plugin.data.search.SearchBean;
import cn.thymechen.xiaoming.plugin.settings.PixivSetting;
import cn.thymechen.xiaoming.plugin.utils.ForwardMessageBuilder;
import cn.thymechen.xiaoming.plugin.utils.HttpUtil;
import cn.thymechen.xiaoming.plugin.utils.ImageUtil;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.PlainText;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PixivInteractor extends SimpleInteractors<PixivHelper> {

    boolean connected;
    PixivSetting settings;

    Map<String, String> pixivHeaders;
    Map<String, String> imageHeaders;

    @Override
    public void onRegister() {
        connected = plugin.isConnected();
        settings = plugin.getSettings();

        pixivHeaders = plugin.getPixivHeaders();
        imageHeaders = plugin.getImageHeaders();

        xiaoMingBot.getScheduler().runAtFixedRate(TimeUnit.SECONDS.toMillis(10), () -> {
            try {
                HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/");
                connected = true;
            } catch (Exception ignored) {
                connected = false;
            }
        });
    }

    @Filter("(搜索|搜|search)(图片|图|image) {标签}")
    @Required("pixiv.user.search")
    public void search(XiaoMingUser<?> user, @FilterParameter("标签") String tag) {
        if (!connected) {
            user.sendMessage("无法连接至 Pixiv");
            return;
        }

        user.sendMessage("正在搜索，该过程可能需要几分钟，请耐心等待...");
        xiaoMingBot.getScheduler().run(() -> {
            Map<String, String> args = new HashMap<>();

            args.put("word", tag);
            args.put("order", "popular_d");
            args.put("mode", "all");
            args.put("p", "1");
            args.put("type", "all");
            args.put("lang", "zh");

            String jsonStr;
            try {
                jsonStr = HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/" + tag, pixivHeaders, args);
            } catch (Exception e) {
                user.sendMessage("搜索时出现异常\n" + e);
                getLogger().error(e.toString(), e);
                return;
            }

            Gson gson = new Gson();
            SearchBean searchBean;
            try {
                searchBean = gson.fromJson(jsonStr, SearchBean.class);
            } catch (Exception e) {
                user.sendMessage("无法找到请求的页面");
                return;
            }

            List<ArtworkData> results = searchBean.getBody().getIllustManga().getData();
            Map<String, String> parameter = new HashMap<>();

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder();
            for (int index = 0; index < 30 && index < results.size(); index++) {
                ArtworkData result = results.get(index);

                try {
                    String illust = HttpUtil.get("https://www.pixiv.net/ajax/illust/" + result.getId());
                    Map<?, ?> illustInfo = gson.fromJson(illust, Map.class);

                    if (illustInfo.get("error").equals(false)) {
                        Map<?, ?> body = (Map<?, ?>) illustInfo.get("body");
                        Map<?, ?> urls = (Map<?, ?>) body.get("urls");
                        getLogger().debug(urls.toString());

                        Image image = ImageUtil.getMiraiImage(HttpUtil.getAsStream(Objects.toString(urls.get("original")), imageHeaders, new HashMap<>()),
                                xiaoMingBot.getContactManager().getBotPrivateContact().getMiraiContact());

                        // 设置变量
                        parameter.put("artwork_author_name" , result.getUserName());
                        parameter.put("artwork_author_url"  , "https://www.pixiv.net/users/" + result.getUserId());
                        parameter.put("artwork_tags"        , result.getTags().toString());
                        parameter.put("artwork_url"         , "https://www.pixiv.net/artworks/" + result.getId());
                        parameter.put("artwork_image_url"   , Objects.toString(urls.get("original")));
                        parameter.put("artwork_image_proxy" , Objects.toString(urls.get("original")).replaceAll("i\\.pximg\\.net", settings.getProxy()));
                        parameter.put("artwork_pid"         , result.getId());
                        parameter.put("artwork_title"       , result.getTitle());
                        parameter.put("artwork_description" , Objects.toString(body.get("description")));

                        // 初始化模板
                        StringSubstitutor stringSubstitutor = new StringSubstitutor(parameter);
                        stringSubstitutor.setVariablePrefix("{");
                        stringSubstitutor.setVariableSuffix("}");

                        if (index == 0) {
                            forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                    "ヾ( ﾟ▽ﾟ)ノ图片找到啦~");
                        }
                        forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                stringSubstitutor.replace(settings.getSearch().getArtworkDescription()));
                        forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                image != null ? image : new PlainText("【无法加载的图片】"));

                        parameter.clear();
                    }

                    Thread.sleep(500);
                } catch (Exception e) {
                    getLogger().error(e.toString(), e);
                }
            }

            user.sendMessage(forwardMessageBuilder.build());
        });
    }

    @Filter("(排行榜|rank)")
    @Required("pixiv.user.rank")
    private void rank(XiaoMingUser<?> user) {
        rank(user, "daily");
    }

    @Filter("(排行榜|rank) {榜单}")
    @Required("pixiv.user.rank")
    public void rank(XiaoMingUser<?> user, @FilterParameter("榜单") String period) {
        if (!connected) {
            user.sendMessage("无法连接至 Pixiv");
            return;
        }

        Map<String, String> args = new HashMap<>();
        period = period.trim();
        switch (period) {
            case "daily":
            case "日榜":
                args.put("mode", "daily");
                break;
            case "weekly":
            case "周榜":
                args.put("mode", "weekly");
                break;
            case "monthly":
            case "月榜":
                args.put("mode", "monthly");
                break;
            case "rookie":
            case "新人":
                args.put("mode", "rookie");
                break;
            case "daily_ai":
            case "AI":
            case "ai":
                args.put("mode", "daily_ai");
                break;
            case "original":
            case "原创":
                args.put("mode", "original");
                break;
            case "male":
            case "男性":
            case "受男性欢迎":
                args.put("mode", "male");
                break;
            case "female":
            case "女性":
            case "受女性欢迎":
                args.put("mode", "female");
                break;
            default:
                user.sendMessage("不正确的参数 : " + period);
                return;
        }
        args.put("p", "1");
        args.put("format", "json");

        user.sendMessage("正在查询，请耐心等待...");
        xiaoMingBot.getScheduler().run(() -> {
            String jsonStr;
            try {
                jsonStr = HttpUtil.get("https://www.pixiv.net/ranking.php", pixivHeaders, args);
            } catch (Exception e) {
                user.sendMessage("无法获取排行榜：\n" + e);
                getLogger().error(e.toString(), e);
                return;
            }

            Gson gson = new Gson();
            RankBean rank;
            try {
                rank = gson.fromJson(jsonStr, RankBean.class);
            } catch (JsonSyntaxException e) {
                user.sendMessage("无法获取排行榜：\n" + e);
                getLogger().error(e.toString(), e);
                return;
            }
            List<Content> contents = rank.getContents();

            Map<String, String> parameter = new HashMap<>();
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder();

            forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                    rank.getDate().substring(0, 4) + "年" +
                    (rank.getDate().charAt(4) == '0' ? rank.getDate().substring(5, 6) : rank.getDate().substring(4, 6)) + "月" +
                    rank.getDate().substring(6, 8)  + "日" + "的排行榜");

            for (int index = 0; index < 10 && index < contents.size(); index++) {
                Content content = contents.get(index);

                try {
                    String illust = HttpUtil.get("https://www.pixiv.net/ajax/illust/" + content.getIllustId(), pixivHeaders, new HashMap<>());
                    Map<?, ?> illustInfo = gson.fromJson(illust, Map.class);

                    if (illustInfo.get("error").equals(false)) {
                        Map<?, ?> body = (Map<?, ?>) illustInfo.get("body");
                        Map<?, ?> urls = (Map<?, ?>) body.get("urls");
                        getLogger().debug(urls.toString());

                        Image image = ImageUtil.getMiraiImage(HttpUtil.getAsStream(Objects.toString(urls.get("original")), imageHeaders, new HashMap<>()),
                                xiaoMingBot.getContactManager().getBotPrivateContact().getMiraiContact());

                        // 设置变量
                        parameter.put("artwork_author_name" , content.getUserName());
                        parameter.put("artwork_author_url"  , "https://www.pixiv.net/users/" + content.getUserId());
                        parameter.put("artwork_tags"        , content.getTags().toString());
                        parameter.put("artwork_url"         , "https://www.pixiv.net/artworks/" + content.getIllustId());
                        parameter.put("artwork_image_url"   , Objects.toString(urls.get("original")));
                        parameter.put("artwork_image_proxy" , Objects.toString(urls.get("original")).replaceAll("i\\.pximg\\.net", settings.getProxy()));
                        parameter.put("artwork_pid"         , String.valueOf(content.getIllustId()));
                        parameter.put("artwork_title"       , content.getTitle());
                        parameter.put("artwork_description" , Objects.toString(body.get("description")));

                        parameter.put("artwork_rating_count", String.valueOf(content.getRatingCount()));

                        // 初始化模板
                        StringSubstitutor stringSubstitutor = new StringSubstitutor(parameter);
                        stringSubstitutor.setVariablePrefix("{");
                        stringSubstitutor.setVariableSuffix("}");

                        forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                stringSubstitutor.replace(settings.getRank().getArtworkDescription()));
                        forwardMessageBuilder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                                image != null ? image : new PlainText("【无法加载的图片】"));

                        parameter.clear();

                        Thread.sleep(500);
                    }
                } catch (Exception e) {
                    getLogger().error(e.toString(), e);
                }
            }

            user.sendMessage(forwardMessageBuilder.build());
        });
    }
}
