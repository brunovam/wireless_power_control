package projects.topologyControl1.nodeDefinitions.malicious;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface IAttack {
	
	public void doAttack(Node node, Message message);
}
