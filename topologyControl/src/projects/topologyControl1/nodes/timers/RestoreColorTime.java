package projects.topologyControl1.nodes.timers;

import java.awt.Color;
import projects.topologyControl1.nodeDefinitions.BasicNode;
import sinalgo.nodes.timers.Timer;

public class RestoreColorTime extends Timer {

	@Override
	public void fire() {
		BasicNode n = (BasicNode) node;
		Color color = n.getMyColor();
		n.setColor(color);
	}

}
