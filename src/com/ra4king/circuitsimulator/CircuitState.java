package com.ra4king.circuitsimulator;

import java.util.HashMap;

import com.ra4king.circuitsimulator.Port.Link;
import com.ra4king.circuitsimulator.utils.Utils;

public class CircuitState {
	private final Circuit circuit;
	private final HashMap<Component, Object> componentProperties;
	private final HashMap<Link, LinkState> linkStates;
	
	public CircuitState(Circuit circuit) {
		this.circuit = circuit;
		componentProperties = new HashMap<>();
		linkStates = new HashMap<>();
		
		circuit.getCircuitStates().add(this);
	}
	
	public Object getComponentProperty(Component component) {
		return componentProperties.get(component);
	}
	
	public void putComponentProperty(Component component, Object property) {
		componentProperties.put(component, property);
	}
	
	public WireValue getValue(Port port) {
		return getValue(port.getLink());
	}
	
	public WireValue getValue(Link link) {
		return get(link).getValue();
	}
	
	private LinkState get(Link link) {
		if(!linkStates.containsKey(link)) {
			if(link.getCircuit() != circuit) {
				throw new IllegalArgumentException("Link not from this circuit.");
			}
			
			LinkState linkState = new LinkState(link);
			linkStates.put(link, linkState);
			return linkState;
		}
		
		return linkStates.get(link);
	}
	
	void link(Link link1, Link link2) {
		get(link1).link(get(link2));
	}
	
	void unlink(Link link, Port port) {
		get(link).unlink(port);
	}
	
	void propagateSignal(Port port) {
		LinkState linkState = get(port.getLink());
		
		WireValue newValue = new WireValue(port.getLink().getBitSize());
		linkState.getParticipantValues().entrySet().forEach(entry -> {
			Utils.ensureCompatible(port, newValue, entry.getValue());
			newValue.merge(entry.getValue());
		});
		
		if(!newValue.equals(linkState.value)) {
			linkState.getValue().set(newValue);
			linkState.getParticipantValues().keySet().stream().filter(participantPort -> !participantPort.equals(port))
					.forEach(participantPort -> participantPort.component.valueChanged(this, linkState.value, participantPort.portIndex));
		}
	}
	
	public synchronized void pushValue(Port port, WireValue value) {
		LinkState linkState = get(port.getLink());
		
		Utils.ensureBitSize(this, value, linkState.value.getBitSize());
		
		WireValue currentValue = linkState.participantValues.get(port);
		if(!value.equals(currentValue)) {
			currentValue.set(value);
			circuit.getSimulator().valueChanged(port, this);
		}
	}
	
	private class LinkState {
		private final Link link;
		private final HashMap<Port, WireValue> participantValues;
		private final WireValue value;
		
		LinkState(Link link) {
			this.link = link;
			
			participantValues = new HashMap<>();
			value = new WireValue(link.getBitSize());
			
			link.getParticipants().forEach(port -> participantValues.put(port, new WireValue(link.getBitSize())));
		}
		
		WireValue getValue() {
			return value;
		}
		
		HashMap<Port, WireValue> getParticipantValues() {
			return participantValues;
		}
		
		void link(LinkState other) {
			if(this == other) return;
			
			Utils.ensureCompatible(this, value, other.value);
			
			WireValue newValue = new WireValue(value);
			newValue.merge(other.value);
			
			if(!newValue.equals(value)) {
				value.set(newValue);
				participantValues.keySet().stream()
						.forEach(port -> port.component.valueChanged(CircuitState.this, newValue, port.portIndex));
			}
			
			if(!newValue.equals(other.value)) {
				other.participantValues.keySet().stream()
						.forEach(port -> port.component.valueChanged(CircuitState.this, newValue, port.portIndex));
			}
			
			participantValues.putAll(other.participantValues);
			linkStates.remove(other.link);
		}
		
		void unlink(Port port) {
			if(!participantValues.containsKey(port)) return;
			
			WireValue value = participantValues.remove(port);
			get(port.getLink()).participantValues.put(port, value);
		}
	}
}