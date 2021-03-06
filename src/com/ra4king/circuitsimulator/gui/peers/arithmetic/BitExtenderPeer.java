package com.ra4king.circuitsimulator.gui.peers.arithmetic;

import java.util.ArrayList;
import java.util.List;

import com.ra4king.circuitsimulator.gui.ComponentManager.ComponentManagerInterface;
import com.ra4king.circuitsimulator.gui.ComponentPeer;
import com.ra4king.circuitsimulator.gui.Connection.PortConnection;
import com.ra4king.circuitsimulator.gui.GuiUtils;
import com.ra4king.circuitsimulator.gui.Properties;
import com.ra4king.circuitsimulator.gui.Properties.Property;
import com.ra4king.circuitsimulator.gui.Properties.PropertyListValidator;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.components.arithmetic.BitExtender;
import com.ra4king.circuitsimulator.simulator.components.arithmetic.BitExtender.ExtensionType;

import javafx.geometry.Bounds;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class BitExtenderPeer extends ComponentPeer<BitExtender> {
	public static void installComponent(ComponentManagerInterface manager) {
		manager.addComponent(new Pair<>("Arithmetic", "Bit Extender"),
		                     new Image(AdderPeer.class.getResourceAsStream("/resources/BitExtender.png")),
		                     new Properties());
	}
	
	public BitExtenderPeer(Properties props, int x, int y) {
		super(x, y, 4, 4);
		
		Properties properties = new Properties();
		properties.ensureProperty(Properties.LABEL);
		properties.ensureProperty(new Property<>("Input Bitsize", Properties.BITSIZE.validator, 1));
		properties.ensureProperty(new Property<>("Output Bitsize", Properties.BITSIZE.validator, 1));
		properties.ensureProperty(new Property<>("Extension Type",
		                                         new PropertyListValidator<>(new ExtensionType[] {
				                                         ExtensionType.ZERO,
				                                         ExtensionType.ONE,
				                                         ExtensionType.SIGN
		                                         }),
		                                         ExtensionType.ZERO));
		properties.mergeIfExists(props);
		
		BitExtender extender = new BitExtender(properties.getValue(Properties.LABEL),
		                                       properties.getValue("Input Bitsize"),
		                                       properties.getValue("Output Bitsize"),
		                                       properties.getValue("Extension Type"));
		
		List<PortConnection> connections = new ArrayList<>();
		connections.add(new PortConnection(this, extender.getPort(BitExtender.PORT_INPUT), "Input", 0, 2));
		connections.add(new PortConnection(this, extender.getPort(BitExtender.PORT_OUTPUT), "Output", getWidth(), 2));
		
		init(extender, properties, connections);
	}
	
	@Override
	public void paint(GraphicsContext graphics, CircuitState circuitState) {
		graphics.setStroke(Color.BLACK);
		graphics.setFill(Color.WHITE);
		GuiUtils.drawShape(graphics::fillRect, this);
		GuiUtils.drawShape(graphics::strokeRect, this);
		
		graphics.setFont(new Font("Monospace", 12));
		graphics.strokeText(String.valueOf(getComponent().getInputBitSize()),
		                    getScreenX() + 3, getScreenY() + getScreenHeight() * 0.5 + 5);
		
		
		String outputString = String.valueOf(getComponent().getOutputBitSize());
		Bounds outputBounds = GuiUtils.getBounds(graphics.getFont(), outputString);
		
		graphics.strokeText(outputString,
		                    getScreenX() + getScreenWidth() - outputBounds.getWidth() - 3,
		                    getScreenY() + getScreenHeight() * 0.5 + 5);
		
		String typeString = "";
		switch(getComponent().getExtensionType()) {
			case ZERO:
				typeString = "0";
				break;
			case ONE:
				typeString = "1";
				break;
			case SIGN:
				typeString = "sign";
				break;
		}
		
		Bounds typeBounds = GuiUtils.getBounds(graphics.getFont(), typeString);
		graphics.strokeText(typeString,
		                    getScreenX() + getScreenWidth() * 0.5 - typeBounds.getWidth() * 0.5,
		                    getScreenY() + 12);
	}
}
