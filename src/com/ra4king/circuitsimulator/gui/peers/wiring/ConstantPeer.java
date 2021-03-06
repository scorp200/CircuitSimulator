package com.ra4king.circuitsimulator.gui.peers.wiring;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.wiring.Constant;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class ConstantPeer extends ComponentPeer<Constant> {
	private static final Property<Integer> VALUE = new Property<>("Value", Properties.INTEGER_VALIDATOR, 0);
	
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Wiring", "Constant"),
		                     new Image(ConstantPeer.class.getResourceAsStream("/resources/Constant.png")),
		                     new Properties());
	}
	
	private final WireValue value;
	
	public ConstantPeer(Properties props, int x, int y) {
		super(x, y, 0, 0);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(Properties.DIRECTION);
		properties.ensureProperty(Properties.BITSIZE);
		properties.ensureProperty(VALUE);
		properties.mergeIfExists(props);
		
		Constant constant = new Constant(properties.getValue(Properties.LABEL),
		                                 properties.getValue(Properties.BITSIZE),
		                                 properties.getValue(VALUE));
		setWidth(Math.max(2, Math.min(8, constant.getBitSize())));
		setHeight((int)Math.round((1 + (constant.getBitSize() - 1) / 8) * 1.5));
		
		value = WireValue.of(constant.getValue(), constant.getBitSize());
		
		List<PortConnection> connections = new ArrayList<>();
		switch(properties.getValue(Properties.DIRECTION)) {
			case EAST:
				connections.add(new PortConnection(this, constant.getPort(0), getWidth(), getHeight() / 2));
				break;
			case WEST:
				connections.add(new PortConnection(this, constant.getPort(0), 0, getHeight() / 2));
				break;
			case NORTH:
				connections.add(new PortConnection(this, constant.getPort(0), getWidth() / 2, 0));
				break;
			case SOUTH:
				connections.add(new PortConnection(this, constant.getPort(0), getWidth() / 2, getHeight()));
				break;
		}
		
		init(constant, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		if(!getComponent().getName().isEmpty()) {
			Bounds bounds = GuiUtils.getBounds(graphics.getFont(), getComponent().getName());
			graphics.setStroke(Color.BLACK);
			graphics.strokeText(getComponent().getName(), getScreenX() - bounds.getWidth() - 5,
			                    getScreenY() + (getScreenHeight() + bounds.getHeight()) * 0.4);
		}
		
		graphics.setFont(Font.font("monospace", 16));
		graphics.setFill(Color.GRAY);
		graphics.setStroke(Color.GRAY);
		
		graphics.fillRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 10, 10);
		graphics.strokeRoundRect(getScreenX(), getScreenY(), getScreenWidth(), getScreenHeight(), 10, 10);
		
		if(value.getBitSize() > 1) {
			graphics.setStroke(Color.BLACK);
		} else {
			GuiUtils.setBitColor(graphics, value.getBit(0));
		}
		
		String string = value.toString();
		for(int i = 0, row = 1; i < string.length(); row++) {
			String sub = string.substring(i, i + Math.min(8, string.length() - i));
			i += sub.length();
			graphics.strokeText(sub, getScreenX() + 2, getScreenY() + 14 * row);
		}
	}
}
