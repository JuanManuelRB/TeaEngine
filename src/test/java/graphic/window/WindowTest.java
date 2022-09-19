package graphic.window;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WindowTest {
//    Window window = Window.builder().withTitle("Test").withWidth(200).build();

    @Test
    void builder() {
        var builder = Window.builder();

        Assertions.assertNotNull(builder);
        
        builder.title("Title").withDimensions(300, 300).withPosition(100, 100);


        try (var window = builder.build()){
            Assertions.assertEquals("Titulo", window.getTitle());
            Assertions.assertEquals(300, window.getWidth());
            Assertions.assertEquals(300, window.getHeight());

            Assertions.assertEquals(0, window.getPosX());
            Assertions.assertEquals(0, window.getPosY());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }



    }

    @Test
    void getContext() {
        try (Window window = Window.builder().build()) {
            // Assert there is no value
            Assertions.assertEquals(0, window.getContext());

            window.create();
            Assertions.assertNotEquals(0, window.getContext());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create() {
    }

    @Test
    void getTitle() {
    }

    @Test
    void close() {
    }

    @Test
    void closing() {
    }

    @Test
    void getPosX() {
    }

    @Test
    void getPosY() {
    }

    @Test
    void getWidth() {
    }

    @Test
    void getHeight() {
    }
}