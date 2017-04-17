package model.view.controller;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class JFXView extends Application{



	@Override
	public void start(Stage primaryStage) throws Exception {

		initUI(primaryStage);

	}

	private void initUI(Stage primaryStage){
		Group group = new Group();

		Scene scene = new Scene(group, 400, 400);

		scene.setFill(Color.AQUAMARINE);

		primaryStage.setTitle("Here we gooooooo");

		TitledPane titlePane1 = new TitledPane("Password", new Button("Click me"));
		titlePane1.setCollapsible(false);

		group.getChildren().add(titlePane1);

		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);

	}

}
