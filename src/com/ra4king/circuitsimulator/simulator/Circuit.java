package com.ra4king.circuitsimulator.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Roi Atalla
 */
public class Circuit {
	private Simulator simulator;
	
	private Set<Component> components;
	private Set<CircuitState> states;
	private CircuitState topLevelState;
	
	private List<CircuitChangeListener> listeners = new ArrayList<>();
	
	public Circuit(Simulator simulator) {
		this.simulator = simulator;
		simulator.addCircuit(this);
		
		components = new HashSet<>();
		states = new HashSet<>();
		
		topLevelState = new CircuitState(this);
	}
	
	public <T extends Component> T addComponent(T component) {
		if(component.getCircuit() == this) {
			return component;
		}
		
		if(component.getCircuit() != null) {
			throw new IllegalArgumentException("Component already belongs to a circuit.");
		}
		
		component.setCircuit(this);
		components.add(component);
		states.forEach(state -> component.init(state, state.getComponentProperty(component)));
		
		listeners.forEach(listener -> listener.circuitChanged(this, component, true));
		
		return component;
	}
	
	public <T extends Component> void updateComponent(T oldComponent, T newComponent, Runnable inBetween) {
		states.forEach(state -> state.ensureUnlinked(oldComponent));
		
		components.remove(oldComponent);
		states.forEach(oldComponent::uninit);
		oldComponent.setCircuit(null);
		
		listeners.forEach(listener -> listener.circuitChanged(this, oldComponent, false));
		
		inBetween.run();
		
		newComponent.setCircuit(this);
		components.add(newComponent);
		states.forEach(state -> newComponent.init(state, state.getComponentProperty(oldComponent)));
		
		listeners.forEach(listener -> listener.circuitChanged(this, newComponent, true));
	}
	
	public void removeComponent(Component component) {
		states.forEach(state -> state.ensureUnlinked(component));
		
		components.remove(component);
		states.forEach(component::uninit);
		component.setCircuit(null);
		
		listeners.forEach(listener -> listener.circuitChanged(this, component, false));
	}
	
	public Set<Component> getComponents() {
		return Collections.unmodifiableSet(components);
	}
	
	public void clearComponents() {
		new HashSet<>(components).forEach(this::removeComponent);
	}
	
	public Simulator getSimulator() {
		return simulator;
	}
	
	public CircuitState getTopLevelState() {
		return topLevelState;
	}
	
	public Set<CircuitState> getCircuitStates() {
		return states;
	}
	
	public void addListener(CircuitChangeListener listener) {
		listeners.add(listener);
	}
	
	public List<CircuitChangeListener> getListeners() {
		return listeners;
	}
	
	public void removeListener(CircuitChangeListener listener) {
		listeners.remove(listener);
	}
	
	public interface CircuitChangeListener {
		void circuitChanged(Circuit circuit, Component component, boolean added);
	}
}
