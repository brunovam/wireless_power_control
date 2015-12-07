package projects.topologyControl1.nodeDefinitions.malicious.decorator;

import projects.topologyControl1.nodeDefinitions.malicious.IAttack;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class AttackDecorator implements IAttack  {
	
	private IAttack attack;
	
	public AttackDecorator(IAttack attack){
		this.attack = attack;
	}

	public void doAttack(Node node, Message message) {
		attack.doAttack(node,message);

	}

}
