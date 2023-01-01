package cn.thymechen.xiaoming.plugin;

import cn.chuanwise.xiaoming.plugin.JavaPlugin;
import cn.thymechen.xiaoming.plugin.interactors.PixivInteractor;
import cn.thymechen.xiaoming.plugin.settings.PixivSetting;
import cn.thymechen.xiaoming.plugin.utils.HttpUtil;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PixivHelper extends JavaPlugin {

    @Getter
    boolean connected = false;
    @Getter
    PixivSetting setting;

    @Override
    public void onLoad() {
        getDataFolder().mkdir();
        setting = xiaoMingBot.getFileLoader().loadOrSupply(PixivSetting.class, new File(getDataFolder(), "config.json"), PixivSetting::new);
    }

    @Override
    public void onEnable() {
        xiaoMingBot.getInteractorManager().registerInteractors(new PixivInteractor(), this);

//        xiaoMingBot.getEventManager().registerListeners();

        xiaoMingBot.getScheduler().run(() -> {
            try {
                HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/");
                getLogger().info("成功连接到 Pixiv");
                connected = true;
            } catch (IOException e) {
                getLogger().error("无法连接至 Pixiv，请检查网络");
            }
        });
    }

//    public static void main(String[] args) throws IOException {
//        Map<String, String> parameters = new HashMap<>();
//        parameters.put("word", "白丝");
//        parameters.put("order", "date_d");
//        System.out.println(HttpUtil.get("https://www.pixiv.net/ajax/search/artworks/白丝", parameters));
//    }
}
