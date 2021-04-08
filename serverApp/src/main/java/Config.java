import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Config {

    private static  Properties props;

    public static void init() {

        props = new Properties();

        InputStream is = null;
        try {
            is = new FileInputStream(Config.class.getResource("/settings.properties").getFile());
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String storagePath = Paths.get("serverApp/storage").toAbsolutePath().toString() + "/";
        props.put("storagePath", storagePath);

    }

    public static Properties getProps() {
        return props;
    }
}
