package projects.topologyControl1.nodes.nodeImplementations;

import java.awt.Color;

import projects.topologyControl1.nodeDefinitions.BasicNode;
import projects.topologyControl1.nodeDefinitions.malicious.IAttack;
import projects.topologyControl1.nodeDefinitions.malicious.decorator.ExhaustionAttack;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ExhaustionNode extends BasicNode implements IAttack {
	
	@Override
	public void init() {
		setMyColor(Color.DARK_GRAY);
		super.init();
	}
	
	@Override
	public void postStep() {
		IAttack attack = new ExhaustionAttack(this);
		attack.doAttack(this, null);
	}
	
	public void doAttack(Node node, Message message) {
		
	}
	
	
}
