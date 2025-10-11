package website.lihan.temu;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import website.lihan.temu.device.Keyboard;
import website.lihan.temu.device.VGA;

public class EmulatorGUI {
  private static boolean started = false;

  private Keyboard keyboard;
  private VGA vga;
  private Stage stage;

  public void connect(Keyboard keyboard) {
    this.keyboard = keyboard;
  }

  public void connect(VGA vga) {
    this.vga = vga;
  }

  public void show() {
    Runnable initFunc =
        () -> {
          Platform.runLater(
              () -> {
                if (stage == null) {
                  createStage();
                }
                stage.show();
              });
        };
    if (started) {
      initFunc.run();
    } else {
      Platform.startup(initFunc);
      started = true;
    }
  }

  private void createStage() {
    StackPane root = new StackPane();

    if (vga != null) {
      ImageView imageView = new ImageView(vga.getImage());
      imageView.setPreserveRatio(true);
      imageView.setSmooth(false);
      imageView.setFitWidth(800);
      imageView.setFitHeight(600);
      root.getChildren().add(imageView);
    } else {
      root.getChildren().add(new Label("VGA not connected"));
    }

    Scene scene = new Scene(root, 800, 600);

    if (keyboard != null) {
      scene.setOnKeyPressed(this::handleKeyPressed);
      scene.setOnKeyReleased(this::handleKeyReleased);
    }

    stage = new Stage();
    stage.setTitle("Temu Emulator");
    stage.setScene(scene);
  }

  private void handleKeyPressed(KeyEvent event) {
    if (keyboard != null) {
      keyboard.sendKey(event.getCode(), true);
    }
    event.consume();
  }

  private void handleKeyReleased(KeyEvent event) {
    if (keyboard != null) {
      keyboard.sendKey(event.getCode(), false);
    }
    event.consume();
  }
}
