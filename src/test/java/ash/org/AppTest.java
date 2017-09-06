package ash.org;


import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class AppTest {

    @Test
    public void testDefaultApp() throws IOException {
        App.main(new String[]{});
    }

    @Test
    public void testTomblomfield() throws IOException {
        App.main(new String[]{"http://tomblomfield.com/"});
    }

    @Test
    public void testApp(){
        assertNotNull(new App());
    }
}
