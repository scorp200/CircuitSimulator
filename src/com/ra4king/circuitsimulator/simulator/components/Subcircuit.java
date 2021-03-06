package com.ra4king.circuitsimulator.simulator.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ra4king.circuitsimulator.simulator.Circuit;
import com.ra4king.circuitsimulator.simulator.CircuitState;
import com.ra4king.circuitsimulator.simulator.Component;
import com.ra4king.circuitsimulator.simulator.Port;
import com.ra4king.circuitsimulator.simulator.WireValue;
import com.ra4king.circuitsimulator.simulator.components.wiring.Pin;
import com.ra4king.circuitsimulator.simulator.components.wiring.Pin.PinChangeListener;

import javafx.util.Pair;

/**
 * @author Roi Atalla
 */
public class Subcircuit extends Component {
	private Circuit subcircuit;
	private List<Pin> pins;
	private Map<CircuitState, PinChangeListener> pinListeners;
	
	public Subcircuit(String name, Circuit subcircuit) {
		this(name, subcircuit, getCircuitPins(subcircuit));
	}
	
	private Subcircuit(String name, Circuit subcircuit, List<Pin> pins) {
		super(name, setupPortBits(pins));
		
		this.subcircuit = subcircuit;
		this.pins = pins;
		pinListeners = new HashMap<>();
	}
	
	private static List<Pin> getCircuitPins(Circuit circuit) {
		return circuit.getComponents().stream()
		              .filter(component -> component instanceof Pin).map(component -> (Pin)component)
		              .collect(Collectors.toList());
	}
	
	private static int[] setupPortBits(List<Pin> pins) {
		int[] portBits = new int[pins.size()];
		for(int i = 0; i < portBits.length; i++) {
			portBits[i] = pins.get(i).getBitSize();
		}
		return portBits;
	}
	
	public List<Pin> getPins() {
		return pins;
	}
	
	public Circuit getSubcircuit() {
		return subcircuit;
	}
	
	private void checkCircuitLoop(Circuit circuit) {
		if(circuit == getCircuit()) {
			throw new IllegalArgumentException("Subcircuit loop detected.");
		}
		
		for(Component component : circuit.getComponents()) {
			if(component != this && component instanceof Subcircuit) {
				Subcircuit subcircuit = (Subcircuit)component;
				checkCircuitLoop(subcircuit.getSubcircuit());
			}
		}
	}
	
	@Override
	public void setCircuit(Circuit circuit) {
		super.setCircuit(circuit);
		
		checkCircuitLoop(subcircuit);
	}
	
	@Override
	public void init(CircuitState circuitState, Object lastProperty) {
		CircuitState subcircuitState = new CircuitState(subcircuit);
		circuitState.putComponentProperty(this, subcircuitState);
		
		for(int i = 0; i < pins.size(); i++) {
			Port port = getPort(i);
			Pair<CircuitState, PinChangeListener> pair =
					new Pair<>(subcircuitState, (pin, state, value) -> circuitState.pushValue(port, value));
			pinListeners.put(pair.getKey(), pair.getValue());
			
			pins.get(i).addChangeListener(pair);
		}
		
		for(Component component : subcircuit.getComponents()) {
			component.init(subcircuitState,
			               lastProperty == null ? null : ((CircuitState)lastProperty).getComponentProperty(component));
		}
	}
	
	public CircuitState getSubcircuitState(CircuitState parentState) {
		return (CircuitState)parentState.getComponentProperty(this);
	}
	
	@Override
	public void uninit(CircuitState circuitState) {
		CircuitState subcircuitState = (CircuitState)circuitState.getComponentProperty(this);
		subcircuit.getComponents().forEach(component -> component.uninit(subcircuitState));
		subcircuit.getCircuitStates().remove(subcircuitState);
		if(pinListeners.containsKey(circuitState)) {
			Pair<CircuitState, PinChangeListener> pair = new Pair<>(subcircuitState, pinListeners.get(circuitState));
			pins.forEach(pin -> pin.removeChangeListener(pair));
		}
	}
	
	public Port getPort(Pin pin) {
		int index = pins.indexOf(pin);
		if(index == -1) {
			return null;
		}
		return getPort(index);
	}
	
	@Override
	public void valueChanged(CircuitState state, WireValue value, int portIndex) {
		CircuitState subcircuitState = (CircuitState)state.getComponentProperty(this);
		subcircuitState.pushValue(pins.get(portIndex).getPort(0), value);
	}
}
