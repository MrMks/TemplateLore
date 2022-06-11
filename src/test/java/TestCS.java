import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;

public class TestCS {
    @Test
    public void test() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("test.yml");
        InputStreamReader ir = new InputStreamReader(is);

        YamlConfiguration yc = YamlConfiguration.loadConfiguration(ir);
        yc.getConfigurationSection("key").get("list");
    }
}
