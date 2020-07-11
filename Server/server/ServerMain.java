package server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ServerMain extends Application{
	//define your offsets here
    private double xOffset = 0;
    private double yOffset = 0;
    
	@Override
	public void start(Stage primaryStage) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ServerUI.fxml"));
			Parent root = fxmlLoader.load();
	        primaryStage.initStyle(StageStyle.TRANSPARENT);

	        ServerController Control = (ServerController) fxmlLoader.getController();
	        //grab your root here
	        root.setOnMousePressed(event -> {
	            xOffset = event.getSceneX();
	            yOffset = event.getSceneY();
	        });

	        //move around here
	        root.setOnMouseDragged(event -> {
	            primaryStage.setX(event.getScreenX() - xOffset);
	            primaryStage.setY(event.getScreenY() - yOffset);
	        });
	        Scene scene = new Scene(root);
	        //set transparent
	        scene.setFill(Color.TRANSPARENT);
	        primaryStage.setScene(scene);
	        primaryStage.show();
	        

		} catch(Exception e) {
			e.printStackTrace();
		}
		}
	
	public static void main(String[] args) {
		launch(args);
	}
}
