import com.bid.car.client.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class TestFile {

    @Test
    public void test() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/application.properties");
        byte[] bytes = StreamUtils.read(inputStream);
        Assert.assertNotNull(bytes);
        Assert.assertNotEquals(bytes.length, 0);
    }
}
