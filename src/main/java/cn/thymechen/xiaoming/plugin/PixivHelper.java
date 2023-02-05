package cn.thymechen.xiaoming.plugin;

import cn.chuanwise.xiaoming.plugin.JavaPlugin;
import cn.thymechen.xiaoming.plugin.data.CronData;
import cn.thymechen.xiaoming.plugin.data.rank.Content;
import cn.thymechen.xiaoming.plugin.data.rank.RankBean;
import cn.thymechen.xiaoming.plugin.interactors.PixivInteractor;
import cn.thymechen.xiaoming.plugin.settings.PixivSetting;
import cn.thymechen.xiaoming.plugin.utils.ForwardMessageBuilder;
import cn.thymechen.xiaoming.plugin.utils.HttpUtil;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import net.mamoe.mirai.message.data.ForwardMessage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@SuppressWarnings("ResultOfMethodCallIgnored")
public class PixivHelper extends JavaPlugin {

    boolean connected = false;
    PixivSetting settings;

    Map<String, String> pixivHeaders = new HashMap<>();
    Map<String, String> imageHeaders = new HashMap<>();


    @Getter(AccessLevel.NONE)
    private CronData cronData;

    @Override
    @SuppressWarnings("all")
    public void onLoad() {
        getDataFolder().mkdir();

        settings = xiaoMingBot.getFileLoader().loadOrSupply(PixivSetting.class, new File(getDataFolder(), "config.json"), PixivSetting::new);

        comments();

        xiaoMingBot.getFileSaver().readyToSave(settings);
    }

    @Override
    @SuppressWarnings("all")
    public void onEnable() {
        xiaoMingBot.getInteractorManager().registerInteractors(new PixivInteractor(), this);

        xiaoMingBot.getScheduler().run(() -> {
            try {
                HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/");
                getLogger().info("成功连接到 Pixiv");
                connected = true;
            } catch (Exception e) {
                getLogger().error("无法连接至 Pixiv，请检查网络");
            }
        });

        pixivHeaders.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        pixivHeaders.put("cookie", settings.getCookie());
        pixivHeaders.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36");

        imageHeaders.put("referer", "https://www.pixiv.net");
    }

    private void push() {
        cronData = new CronData();
        String[] time = settings.getRank().getPush().getTime().split("[:：]");
        try {
            cronData.parseCron("0 " + time[1] + time[0] + " * * *");
        } catch (Exception e) {
            getLogger().error("无法解析 Cron 表达式! " + e);
        }

        xiaoMingBot.getScheduler().runAtFixedRate(1000, () -> {
            try {
                String jsonStr = HttpUtil.get("https://www.pixiv.net/ranking.php?mode=daily&p=1&format=json", pixivHeaders);
                if (cronData.shouldInvoke()) {
                    RankBean rank = new Gson().fromJson(jsonStr, RankBean.class);
                    List<Content> contents = rank.getContents();

                    ForwardMessageBuilder builder = new ForwardMessageBuilder();
                    builder.add(xiaoMingBot.getCode(), (int) (System.currentTimeMillis() / 1000), xiaoMingBot.getMiraiBot().getNick(),
                            rank.getDate().substring(0, 4) + "年" +
                                    (rank.getDate().charAt(4) == '0' ? rank.getDate().substring(5, 6) : rank.getDate().substring(4, 6)) + "月" +
                                    rank.getDate().substring(6, 8)  + "日" + "的排行榜");

                    for (Content content : contents) {

                    }

                    ForwardMessage forwardMessage = builder.build();
                    settings.getRank().getPush().getGroup().forEach(
                            code -> xiaoMingBot.getContactManager().getGroupContact(code)
                                    .ifPresent(
                                            contact -> contact.sendMessage(forwardMessage)
                                    )
                    );
                }
            } catch (Exception e) {
                getLogger().error("", e);
            }
        });
    }

    private void comments() {
        // search.artworkDescription 的注释
        settings.getSearch().get_artworkDescription().clear();
        settings.getSearch().get_artworkDescription().put("可以用变量如下", "");
        settings.getSearch().get_artworkDescription().put("{artwork_author_name}", "作者名");
        settings.getSearch().get_artworkDescription().put("{artwork_author_url}", "作者首页");
        settings.getSearch().get_artworkDescription().put("{artwork_tags}", "作品标签");
        settings.getSearch().get_artworkDescription().put("{artwork_url}", "作品详情页");
        settings.getSearch().get_artworkDescription().put("{artwork_image_url}", "图片链接（无法直连）");
        settings.getSearch().get_artworkDescription().put("{artwork_image_proxy}", "图片代理链接");
        settings.getSearch().get_artworkDescription().put("{artwork_pid}", "作品 pid");
        settings.getSearch().get_artworkDescription().put("{artwork_title}", "作品标题");
        settings.getSearch().get_artworkDescription().put("{artwork_description}", "作品简介");

        // rank.artworkDescription 的注释
        settings.getRank().get_artworkDescription().clear();
        settings.getRank().get_artworkDescription().put("可以用变量如下", "");
        settings.getRank().get_artworkDescription().put("{artwork_author_name}", "作者名");
        settings.getRank().get_artworkDescription().put("{artwork_author_url}", "作者首页");
        settings.getRank().get_artworkDescription().put("{artwork_tags}", "作品标签");
        settings.getRank().get_artworkDescription().put("{artwork_url}", "作品详情页");
        settings.getRank().get_artworkDescription().put("{artwork_image_url}", "图片链接（无法直连）");
        settings.getRank().get_artworkDescription().put("{artwork_image_proxy}", "图片代理链接");
        settings.getRank().get_artworkDescription().put("{artwork_pid}", "作品 PID");
        settings.getRank().get_artworkDescription().put("{artwork_title}", "作品标题");
        settings.getRank().get_artworkDescription().put("{artwork_description}", "作品简介");
        settings.getRank().get_artworkDescription().put("", "");
        settings.getRank().get_artworkDescription().put("{artwork_rating_count}", "收藏量");
    }
}
