package com.ra4king.circuitsimulator.gui;

import static com.ra4king.circuitsimulator.gui.Properties.Direction.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.Properties.Direction;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Port.Link;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.WireValue.State;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Roi Atalla
 */
public class GuiUtils {
	public static final int BLOCK_SIZE = 10;
	
	public static int getCircuitCoord(double a) {
		return ((int)Math.round(a) + BLOCK_SIZE / 2) / BLOCK_SIZE;
	}
	
	public static int getScreenCircuitCoord(double a) {
		return getCircuitCoord(a) * BLOCK_SIZE;
	}
	
	public static Bounds getBounds(Font font, String string) {
		Text text = new Text(string);
		text.setFont(font);
		return text.getLayoutBounds();
	}
	
	public interface Drawable {
		void draw(int x, int y, int width, int height);
	}
	
	public static void drawShape(Drawable drawable, GuiElement element) {
		drawable.draw(element.getScreenX(), element.getScreenY(), element.getScreenWidth(), element.getScreenHeight());
	}
	
	public static void setBitColor(GraphicsContext graphics, CircuitState circuitState, LinkWires linkWires) {
		if(linkWires.isLinkValid()) {
			Link link = linkWires.getLink();
			if(link != null) {
				if(circuitState.isShortCircuited(link)) {
					graphics.setStroke(Color.RED);
					graphics.setFill(Color.RED);
				} else {
					setBitColor(graphics, circuitState.getMergedValue(link));
				}
			} else {
				setBitColor(graphics, new WireValue(1));
			}
		} else {
			graphics.setStroke(Color.ORANGE);
			graphics.setFill(Color.ORANGE);
		}
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value) {
		setBitColor(graphics, value, Color.BLACK);
	}
	
	public static void setBitColor(GraphicsContext graphics, WireValue value, Color defaultColor) {
		if(value.getBitSize() == 1) {
			setBitColor(graphics, value.getBit(0));
		} else {
			graphics.setStroke(defaultColor);
			graphics.setFill(defaultColor);
		}
	}
	
	public static void setBitColor(GraphicsContext graphics, State bitState) {
		switch(bitState) {
			case ONE:
				graphics.setStroke(Color.GREEN.brighter());
				graphics.setFill(Color.GREEN.brighter());
				break;
			case ZERO:
				graphics.setStroke(Color.GREEN.darker());
				graphics.setFill(Color.GREEN.darker());
				break;
			case X:
				graphics.setStroke(Color.BLUE.brighter());
				graphics.setFill(Color.BLUE.brighter());
				break;
		}
	}
	
	public static PortConnection rotatePortCCW(PortConnection connection, boolean useWidth) {
		int x = connection.getXOffset();
		int y = connection.getYOffset();
		int width = useWidth ? connection.getParent().getWidth() : connection.getParent().getHeight();
		
		return new PortConnection(connection.getParent(),
		                          connection.getPort(),
		                          connection.getName(),
		                          y, width - x);
	}
	
	public static List<PortConnection> rotatePorts(List<PortConnection> connections,
	                                               Direction source,
	                                               Direction destination) {
		List<Direction> order = Arrays.asList(Direction.EAST, NORTH, Direction.WEST, Direction.SOUTH);
		
		Stream<PortConnection> stream = connections.stream();
		
		int index = order.indexOf(source);
		boolean useWidth = true;
		while(order.get(index++ % order.size()) != destination) {
			boolean temp = useWidth;
			stream = stream.map(port -> rotatePortCCW(port, temp));
			useWidth = !useWidth;
		}
		
		return stream.collect(Collectors.toList());
	}
	
	public static void rotateElement(GuiElement element, Direction source, Direction destination) {
		List<Direction> order = Arrays.asList(Direction.EAST, NORTH, Direction.WEST, Direction.SOUTH);
		
		int index = order.indexOf(source);
		while(order.get(index++ % order.size()) != destination) {
			int width = element.getWidth();
			int height = element.getHeight();
			element.setWidth(height);
			element.setHeight(width);
		}
	}
	
	public static void drawLabel(ComponentPeer<?> component, GraphicsContext graphics) {
		int x = component.getScreenX();
		int y = component.getScreenY();
		int width = component.getScreenWidth();
		
		if(!component.getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), component.getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(component.getComponent().getName(), x + (width - bounds.getWidth()) * 0.5, y - 5);
		}
	}
	
	/**
	 * Source orientation is assumed EAST
	 */
	public static void rotateGraphics(ComponentPeer<?> component, GraphicsContext graphics, Direction direction) {
		int x = component.getScreenX();
		int y = component.getScreenY();
		int width = component.getScreenWidth();
		int height = component.getScreenHeight();
		
		graphics.translate(x + width * 0.5, y + height * 0.5);
		switch(direction) {
			case NORTH:
				graphics.rotate(270);
				graphics.translate(-x - height * 0.5, -y - width * 0.5);
				break;
			case SOUTH:
				graphics.rotate(90);
				graphics.translate(-x - height * 0.5, -y - width * 0.5);
				break;
			case WEST:
				graphics.rotate(180);
			default:
				graphics.translate(-x - width * 0.5, -y - height * 0.5);
		}
	}
}
